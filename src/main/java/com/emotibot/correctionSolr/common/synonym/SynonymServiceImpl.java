package com.emotibot.correctionSolr.common.synonym;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.emotibot.configclient.ConfigClientOptions;
import com.emotibot.configclient.ConfigResponseCallback;
import com.emotibot.configclient.ConsulConfigClient;
import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.utils.Base64CoderUtils;
import com.emotibot.correctionSolr.utils.ConsoleUtils;
import com.emotibot.correctionSolr.utils.HttpUtils;
import com.emotibot.correctionSolr.utils.SolrUtils;
import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.utils.JsonUtils;
import com.google.gson.JsonObject;

/**
 * 监听consul特定kv，如果有改变，读取其内容，这里只是针对特定的appid
 * 
 * @author emotibot
 *
 */
public class SynonymServiceImpl implements SynonymService
{
    private static Logger logger = Logger.getLogger(SynonymServiceImpl.class);
    
    private boolean isRun = false;
    private ReentrantLock lock = new ReentrantLock();
    
    private String consulServiceURL = null;
    private String consulKeyPrefix = null;
    private boolean isRunLocal = false;
    private Map<String, String> appidToVersionMap = new HashMap<String, String>();
    
    private ConsulConfigClient consulConfigClient = null;
    private ConfigResponseCallback callback = new ConsulCallback();
    
    private int round = 0;
    
    public SynonymServiceImpl()
    {
        init();
    }
    
    @Override
    public void start()
    {
        if (!isRun)
        {
            isRun = true;
            registerConsul(consulServiceURL, consulKeyPrefix, callback);
            logger.info("SynonymService is started");
        }
        else
        {
            logger.warn("SynonymService has already running");
        }
    }

    @Override
    public void stop()
    {
        if(isRun)
        {
            isRun = false;
            if (consulConfigClient != null)
            {
                consulConfigClient.interrupt();
                consulConfigClient = null;
            }
            logger.info("SynonymService is stopped");
        }
        else
        {
            logger.warn("SynonymService is not running yet");
        }
    }
    
    private void init()
    {
        consulServiceURL = ConfigManager.INSTANCE.getPropertyString(Constants.CONSUL_SERVICE_URL_KEY);
        consulKeyPrefix = ConfigManager.INSTANCE.getPropertyString(Constants.CONSUL_KEY_PREFIX_KEY);
        isRunLocal = ConfigManager.INSTANCE.getPropertyBoolean(Constants.RUN_ON_LOCAL_KEY);
    }
    
    private boolean registerConsul(String consulServiceURL, String consulKeyPrefix, ConfigResponseCallback callback)
    {
        try 
        {
            ConfigClientOptions options = new ConfigClientOptions();
            options.setRecurse(true);
            options.setInterval(Constants.CONSUL_INTERVAL_TIME);
            options.setWait(Constants.CONSUL_WAIT_TIME);

            String hostAndPort = HttpUtils.getHostAndPort(consulServiceURL);
            consulConfigClient = new ConsulConfigClient(hostAndPort, consulKeyPrefix, callback, options);
            consulConfigClient.start();
            consulConfigClient.join(Constants.CONSUL_JOIN_TIME);
            return true;
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return false;
        }
    }
    
    private void updateSynonym(Map<String, String> kvs)
    {
        lock.lock();
        try 
        {
            logger.info(String.format("一共%d个第三方词典", kvs.entrySet().size()));
            int count = 0;
            for (Map.Entry<String, String> thisEntry : kvs.entrySet()) 
            {
                String key = thisEntry.getKey();
                String value = thisEntry.getValue();
                count++;
                logger.info(String.format("检查第%d/%d个词典 key:%s, value:%s\n", count, kvs.size(), key, value));

                // 解析得到第三方条目的appid, version, url
                logger.info("步骤一：获取appid，url，version");
                String appid = getAppid(key);
                logger.debug(String.format("appid = [%s]\n", appid));
                
                if (StringUtils.isEmpty(appid))
                {
                    logger.info("appid为空");
                    return;
                }
                
                Map<String, String> valuesMap = getUrlMd5(value);
                if (valuesMap.keySet().size() < 2) 
                {
                    logger.debug(String.format("信息不全,当前信息" + valuesMap.keySet()));
                    continue;
                }

                String url = valuesMap.get(Constants.CONSUL_VALUE_JSON_KEY_URL);
                String version = valuesMap.get(Constants.CONSUL_VALUE_JSON_KEY_VERSION);

                try
                {
                    /**
                     * 如果实在本地的MAC上调试时，synonymUrl返回的host ip是172.17.0.1, 需要改为目前sever地址
                     */
                    if (isRunLocal)
                    {
                        String remoteIpOld = ConsoleUtils.getHost(url);
                        String remoteIpNew = ConsoleUtils.getHost(consulServiceURL);
                        url = url.replaceAll(remoteIpOld, remoteIpNew);
                        logger.debug("Replace the old host " + remoteIpOld + " with new one " 
                                + remoteIpNew + " for running on remote");
                    }
                    updateSynonym(appid, url, version);
                }
                catch (Exception e)
                {
                    logger.error("Fail to update dictionary with appid " + appid
                            + "; url " + url
                            + "; version " + version);
                }
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            logger.error("更新本地词典失败");
            return;
        }
        finally
        {
            lock.unlock();
        }
    }
    
    private boolean updateSynonym(String appid, String url, String version)
    {
        // 如果为设置对应的url或md5码，则不做后续处理
        if(null == url || null == version)
        {
            logger.error("url or version should not be null");
            return false;
        }
        String localVersion = appidToVersionMap.get(appid);
        if (!StringUtils.isEmpty(localVersion) && localVersion.equals(version))
        {
            logger.info(String.format("version一致，不需要更新，跳过"));
            return false;
        }
        String[] lines = getRemoteSynonymContent(url);
        
        if (lines == null || lines.length <= 0)
        {
            logger.error("fail to get the dictionaryContent");
            return false;
        }
        SolrUtils.updateSolrData(appid, lines);
        appidToVersionMap.put(appid, version);
        return true;
    }
    
    private String[] getRemoteSynonymContent(String url)
    {
        // 检查每个第三方条目的version有没有改变
        logger.info(String.format("步骤二：比较远程词典和本地词典的md5码，远程词典：%s", url));

        // 对于version修改的条目，通过url得到词典文件，检查词典文件的md5是否正确
        logger.info("步骤三：下载远程词典文件");

        byte[] out = HttpUtils.getContent(url);
        if (null == out) 
        {
            logger.info(String.format("下载文件出错，跳过"));
            return null;
        } 
        else 
        {
            logger.info("下载完成");
        }
        String content = new String(out);
        String[] lines = content.split(Constants.LINE_SPLIT_REGEX, -1);
        return lines;
    }
    
    private Map<String, String> getUrlMd5(String value) 
    {
        Map<String, String> urlMd5Map = new HashMap<String, String>();

        // 解码value
        String valueDecoded = base64Decode(value);
        if (null == valueDecoded)
        {
            return urlMd5Map;
        }

        // 解析得到json对象
        JsonObject thisEntryValueJson = (JsonObject) JsonUtils.getObject(valueDecoded, JsonObject.class);
        // 从json对象得到key和value对
        Map<String, String> valuesMap = getValuesFromJson(thisEntryValueJson);

        return valuesMap;
    }
    
    private String getAppid(String key) 
    {
        String appid = key.replace(consulKeyPrefix + "/", "");
        return appid;
    }
    
    private String base64Decode(String value) 
    {
        try 
        {
            String valueDecoded = Base64CoderUtils.decodeBase64ToString(value);
            if (null == valueDecoded || valueDecoded.isEmpty()) 
            {
                logger.error(String.format("解码后值为空:[%s]", value));
                return null;
            }
            return valueDecoded;
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            logger.error(String.format("解码失败:[%s]", value));
            return null;
        }
    }
    
    private Map<String, String> getValuesFromJson(JsonObject thisEntryValueJson) 
    {
        Map<String, String> valuesMap = new HashMap<String, String>();

        try 
        {
            String url = thisEntryValueJson.get(Constants.CONSUL_VALUE_JSON_KEY_URL).getAsString();
            logger.debug(String.format("url = [%s]\n", url));
            if (!StringUtils.isEmpty(url)) 
            {
                valuesMap.put(Constants.CONSUL_VALUE_JSON_KEY_URL, url);
            }

            String version = thisEntryValueJson.get(Constants.CONSUL_VALUE_JSON_KEY_VERSION).getAsString();
            logger.debug(String.format("version = [%s]\n", version));
            if (!StringUtils.isEmpty(version)) 
            {
                valuesMap.put(Constants.CONSUL_VALUE_JSON_KEY_VERSION, version);
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            logger.error(String.format("json格式错误: [%s]\n", thisEntryValueJson));
        }

        return valuesMap;
    }
    
    class ConsulCallback implements ConfigResponseCallback
    {
        public void onUpdate(Map<String, String> kvs)
        {
            if (null == kvs || (kvs.size() <= 0))
            {
                return;
            }
            round++;
            logger.info(String.format("第%d轮开始：%s,%s", round, "更新第三方词典", new Date()));
            updateSynonym(kvs);
            logger.info(String.format("第%d轮结束：%s,%s", round, "更新第三方词典", new Date()));
        }
    }
}
