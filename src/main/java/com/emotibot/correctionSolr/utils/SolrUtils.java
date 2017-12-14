package com.emotibot.correctionSolr.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class SolrUtils
{
    private static Logger logger = Logger.getLogger(SolrUtils.class);
    
    private static String solr_url = ConfigManager.INSTANCE.getPropertyString(Constants.SOLR_URL_KEY);
    private static HttpSolrClient solrClient = null; 
    public static DatabaseType[] databaseType = {DatabaseType.SINGLE_WORD_DATABASE, DatabaseType.WORD_DATABASE, DatabaseType.WORD_SYN_DATABASE, DatabaseType.PINYING_WORD_DATABASE, DatabaseType.PINYING2_WORD_DATABASE};
        
    static
    {
        buildSolrClient();
        //loadSynonymToSolr();
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
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static List<String> querySolrData(String database, String query)
    {
        List<String> ret = new ArrayList<String>();
        SolrQuery params = new SolrQuery();
        params.set("q", query);
        params.set("fq", "database:" + database);
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
        catch (SolrServerException | IOException e)
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
        if (!StringUtils.isEmpty(query.getFl()))
        {
            params.set("fl", query.getFl());
        }
        if (!StringUtils.isEmpty(query.getFq()))
        {
            params.set("fq", query.getFq());
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
                ResultElement resultElement = new ResultElement(score, result, query.getDatabase());
                ret.add(resultElement);
            }
            return ret;
        } 
        catch (SolrServerException | IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void updateSynonymFile(String[] lines)
    {
        String originalFile = ConfigManager.INSTANCE.getPropertyString(com.emotibot.correction.constants.Constants.ORIGIN_FILE_PATH);
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(originalFile);
            for (String line : lines)
            {
                String[] elements = line.trim().split("\t");
                if (elements.length < 2)
                {
                    continue;
                }

                String levelInfo = elements[0];
                if (MyConstants.level_infos.contains(levelInfo))
                {
                    String word = elements[1];
                    fw.write(word + "\r\n");
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return;
        }
        finally
        {
            try
            {
                fw.close();
            } 
            catch (IOException e)
            {
                
            }
        }
    }
    
    public static void loadSynonymToSolr()
    {
        String originalFile = ConfigManager.INSTANCE.getPropertyString(com.emotibot.correction.constants.Constants.ORIGIN_FILE_PATH);
        Set<String> synonymSet = new HashSet<String>();
        BufferedReader br = null; 
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(originalFile)));
            String line = null;
            while((line = br.readLine()) != null)
            {
                synonymSet.add(line.trim());
            }
            if (synonymSet.size() > 0)
            {
                deleteAllData();
                List<CorrectionElement> solrDataList = new ArrayList<CorrectionElement>();
                int count = 0;
                for (String synonym : synonymSet)
                {
                    for (DatabaseType type : databaseType)
                    {
                        CorrectionElement element = CorrectionElementUtils.getCorrectionElement(synonym, String.valueOf(count), type);
                        if (element != null)
                        {
                            solrDataList.add(element);
                            count ++;
                        }
                    }
                }
                addSolrData(solrDataList);
            }
            logger.info("Solr update successful");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                } 
                catch (IOException e)
                {
                    
                }
            }
        }
    }
    
    public static void test()
    {
        
    }
    
    static class MyConstants
    {
        private static String MOVIE_INFO = "专有词库>长虹>影视>电影";
        private static String TV_INFO = "专有词库>长虹>影视>电视剧";
        private static String[] LEVEL_INFOS = {MOVIE_INFO, TV_INFO};
        public static Set<String> level_infos = new HashSet<String>();
        static
        {
            for (String level_info : LEVEL_INFOS)
            {
                level_infos.add(level_info);
            }
        }
    }
}
