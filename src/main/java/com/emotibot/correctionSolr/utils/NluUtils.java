package com.emotibot.correctionSolr.utils;

import java.util.ArrayList;
import java.util.List;

import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.constants.Constants;
import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.nlu.NLUResponse;
import com.emotibot.middleware.task.restCallTask.NLUTask;
import com.emotibot.middleware.utils.UrlUtils;

/**
 * 获取Nlu调用的返回值
 * 
 * @author emotibot
 *
 */
public class NluUtils
{
    public static String getNameEntitis(String sentence)
    {
        NLUTask task = new NLUTask();
        String params = "?f=namedEntities&appid=5a200ce8e6ec3a6506030e54ac3b970e&q=" + UrlUtils.urlEncode(sentence);
        String hostname = ConfigManager.INSTANCE.getPropertyString(MyConstants.NLU_HOST_KEY);
        String port = ConfigManager.INSTANCE.getPropertyString(MyConstants.NLU_PORT_KEY);
        String endpoint = ConfigManager.INSTANCE.getPropertyString(MyConstants.NLU_ENDPOINT_KEY);
        String url = UrlUtils.getUrl(hostname, port, endpoint, params);
        HttpRequest request = new HttpRequest(url, null, HttpRequestType.GET);
        task.setRequest(request);
        NLUResponse nluResponse = null;
        try
        {
            nluResponse = (NLUResponse) task.call();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return nluResponse.getNameEntity();
    }
    
    public static List<String> fetchNameEntitiesByTag(String nameEntity, String tag)
    {
        List<String> ret = new ArrayList<String>();
        int cursor = 0;
        int index = nameEntity.indexOf(tag, cursor);
        while(index >= 0)
        {
            int endIndex = nameEntity.indexOf(Constants.END_TAG, index);
            if (endIndex < 0)
            {
                return null;
            }
            ret.add(nameEntity.substring(index + tag.length(), endIndex));
            cursor = endIndex + Constants.END_TAG.length();
            if (cursor >= nameEntity.length())
            {
                break;
            }
            index = nameEntity.indexOf(tag, cursor);
        }
        return ret;
    }
    
    static class MyConstants
    {
        public static String NLU_HOST_KEY = "NLU_HOST";
        public static String NLU_PORT_KEY = "NLU_PORT";
        public static String NLU_ENDPOINT_KEY = "NLU_ENDPOINT";
    }
}
