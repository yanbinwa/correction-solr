package com.emotibot.correctionSolr.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.element.CorrectionElement;
import com.emotibot.correctionSolr.element.DatabaseType;
import com.emotibot.correctionSolr.element.QueryElement;
import com.emotibot.correctionSolr.element.ResultElement;
import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.utils.StringUtils;

/**
 * 
 * 需要保存当前的片库，如果有新增，则只要增量替换，如果有删除，则需要全量替换
 * 
 * @author emotibot
 *
 */
public class SolrUtils
{
    private static Logger logger = Logger.getLogger(SolrUtils.class);
    
    private static String solr_url = ConfigManager.INSTANCE.getPropertyString(Constants.SOLR_URL_KEY);
    private static HttpSolrClient solrClient = null; 
    public static List<DatabaseType> databaseType = null;
            
    private static ReentrantLock lock = new ReentrantLock();
    private static Map<String, Set<String>> movieNamesMap = new HashMap<String, Set<String>>();
    
    static
    {
        initDatabaseType();
        buildSolrClient();
        deleteAllData();
    }
    
    private static void initDatabaseType()
    {
        databaseType = new ArrayList<DatabaseType>();
        databaseType.add(DatabaseType.SINGLE_WORD_DATABASE);
        databaseType.add(DatabaseType.WORD_DATABASE);
        boolean enableHomonym = ConfigManager.INSTANCE.getPropertyBoolean(Constants.ENABLE_HOMONYM_CORRECTION_KEY);
        if (enableHomonym)
        {
            databaseType.add(DatabaseType.PINYING_WORD_DATABASE);
            databaseType.add(DatabaseType.PINYING2_WORD_DATABASE);
        }
        boolean enableSynonym = ConfigManager.INSTANCE.getPropertyBoolean(Constants.ENABLE_SYNONYM_CORRECTION_KEY);
        if (enableSynonym)
        {
            databaseType.add(DatabaseType.WORD_SYN_DATABASE);
        }
    }
    
    private static void buildSolrClient()
    {
        solrClient = new HttpSolrClient(solr_url);
        solrClient.setConnectionTimeout(Constants.SOLR_CONNECTION_TIMEOUT);
        solrClient.setDefaultMaxConnectionsPerHost(Constants.SOLR_MAX_CONNECTION);
        solrClient.setMaxTotalConnections(Constants.SOLR_MAX_TOTAL_CONNECTION);
        solrClient.setSoTimeout(Constants.SOLR_SO_TIMEOUT);
    }
    
    public static void addSolrData(List<?> solrData)
    {
        try
        {
            solrClient.addBeans(solrData);
            solrClient.commit();
        } 
        catch (SolrServerException e)
        {
            buildSolrClient();
            e.printStackTrace();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static void deleteAllData(String appid)
    {
        try
        {
            solrClient.deleteByQuery("appid:" + appid);
            solrClient.commit();
        } 
        catch (SolrServerException e)
        {
            buildSolrClient();
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static void deleteAllData()
    {
        try
        {
            solrClient.deleteByQuery("*");
            solrClient.commit();
        } 
        catch (SolrServerException e)
        {
            buildSolrClient();
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static List<String> querySolrData(String appid, String database, String query)
    {
        List<String> ret = new ArrayList<String>();
        SolrQuery params = new SolrQuery();
        params.set("q", query);
        SolrQuery filterQuery = new SolrQuery();
        filterQuery.add("database:" + database);
        filterQuery.add("appid:" + appid);
        params.add(filterQuery);
        try
        {
            QueryResponse rsp = solrClient.query(params);
            SolrDocumentList docs = rsp.getResults();
            for(SolrDocument doc : docs)
            {
                String sentence_original = (String) doc.getFieldValue("sentence_original");
                ret.add(sentence_original);
            }
            return ret;
        } 
        catch (SolrServerException e)
        {
            buildSolrClient();
            e.printStackTrace();
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static List<ResultElement> querySolrData(QueryElement query, String retField)
    {
        List<ResultElement> ret = new ArrayList<ResultElement>();
        SolrQuery params = new SolrQuery();
        params.set("q", query.getQ());
        params.set("start", query.getStart());
        params.set("rows", query.getRows());
        params.addFilterQuery("appid:" + query.getAppid());
        if (!StringUtils.isEmpty(query.getFl()))
        {
            params.set("fl", query.getFl());
        }
        if (!StringUtils.isEmpty(query.getFq()))
        {
            params.addFilterQuery(query.getFq());
        }
        if (query.getFields() != null)
        {
            List<String> fields = query.getFields();
            String str = "";
            for(int i = 0; i < fields.size(); i ++)
            {
                if (i != 0)
                {
                    str += " OR ";
                }
                str += "field:" + fields.get(i);
            }
            params.addFilterQuery(str);
        }
        if (!StringUtils.isEmpty(query.getDefType()))
        {
            params.set("defType", query.getDefType());
        }
        if (!StringUtils.isEmpty(query.getQf()))
        {
            params.set("qf", query.getQf());
        }
        try
        {
            QueryResponse rsp = solrClient.query(params);
            SolrDocumentList docs = rsp.getResults();
            for(SolrDocument doc : docs)
            {
                Float score = (Float) doc.getFieldValue("score");
                String result = (String) doc.getFieldValue(retField);
                String field = (String) doc.getFieldValue("field");
                ResultElement resultElement = new ResultElement(score, result, query.getDatabase(), field);
                ret.add(resultElement);
            }
            return ret;
        } 
        catch (SolrServerException e)
        {
            buildSolrClient();
            e.printStackTrace();
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 按照appid来存数据，这里直接写入到solr，不需要通过levelInfo
     */
    public static void updateSolrData(String appid, String[] lines)
    {
        lock.lock();
        try
        {
            Set<String> movieNameSetTmp = new HashSet<String>();
            for (String line : lines)
            {
                if (!StringUtils.isEmpty(line))
                {
                    movieNameSetTmp.add(line.trim());
                }
            }
            
            Set<String> movieNameSet = movieNamesMap.get(appid);
            if (movieNameSet == null)
            {
                movieNameSet = new HashSet<String>();
            }
            
            Set<String> addSet = new HashSet<String>();
            Set<String> deleteSet = new HashSet<String>();
            
            for (String movieName : movieNameSetTmp)
            {
                if (!movieNameSet.contains(movieName))
                {
                    addSet.add(movieName);
                }
            }
            
            for (String movieName : movieNameSet)
            {
                if (!movieNameSetTmp.contains(movieName))
                {
                    deleteSet.add(movieName);
                }
            }
            
            if (addSet.size() == 0 && deleteSet.size() == 0)
            {
                logger.info("not data update");
                return;
            }
            logger.info("add set size is: " + addSet.size() + "; delete set is: " + deleteSet);
            if (deleteSet.size() == 0)
            {
                loadSynonymToSolr(appid, addSet, false);
            }
            else
            {
                loadSynonymToSolr(appid, movieNameSetTmp, true);
            }
            movieNamesMap.put(appid, movieNameSetTmp);
        }
        finally
        {
            lock.unlock();
        }
    }
    
    public static void loadSynonymToSolr(String appid, Set<String> synonymSet, boolean tag)
    {
        if (tag)
        {
            deleteAllData(appid);
        }
        List<CorrectionElement> solrDataList = new ArrayList<CorrectionElement>();
        int count = 0;
        for (String synonym : synonymSet)
        {
            for (DatabaseType type : databaseType)
            {
                CorrectionElement element = CorrectionElementUtils.getCorrectionElement(appid, synonym, String.valueOf(count), type);
                if (element != null)
                {
                    solrDataList.add(element);
                    count ++;
                }
            }
        }
        addSolrData(solrDataList);
    }
    
    public static void test()
    {
        
    }
}
