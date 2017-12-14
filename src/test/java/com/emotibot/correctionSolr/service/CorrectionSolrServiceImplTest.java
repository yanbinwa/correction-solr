package com.emotibot.correctionSolr.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.HttpResponse;
import com.emotibot.middleware.utils.HttpUtils;

import au.com.bytecode.opencsv.CSVReader;

public class CorrectionSolrServiceImplTest
{
    public static final String correctionFile = "/Users/emotibot/Documents/workspace/other/correction-solr/file/自动测试2.csv";

    public static final String service_url = "http://localhost:9100/correction/postCorrectionName";
    
    public static int totalCount = 0;
    public static int errorTotalCount = 0;
    
    @Test
    public void test()
    {
        long startTime = System.currentTimeMillis();
        test1();
        long endTime = System.currentTimeMillis();
        System.out.println("用时：[" + (endTime - startTime) + "ms]");
        System.out.println("totalCount: " + totalCount + "; errorCount: " + errorTotalCount + "; errorRate: " + (errorTotalCount / (double)totalCount));
    }

    private void test1()
    {
        File file = new File(correctionFile);  
        FileReader fReader = null;
        CSVReader csvReader = null; 
        try
        {
            fReader = new FileReader(file);
            csvReader = new CSVReader(fReader);
            List<String[]> list = csvReader.readAll();
            for (String[] ss : list)
            {
                totalCount ++;
                String correctSentence = ss[0];
                String errorSentence = ss[1];
                HttpRequest request = new HttpRequest(service_url, errorSentence, HttpRequestType.POST);
                //HttpRequest request = new HttpRequest(service_url, correctSentence, HttpRequestType.POST);
                HttpResponse response = HttpUtils.call(request, 10000);
                String result = response.getResponse();
                if (!result.trim().contains(correctSentence))
                {
                    System.out.println("correct: " + correctSentence + "; error: " + errorSentence + "; result: " + result);
                    //System.out.println(correctSentence);
                    errorTotalCount ++;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                csvReader.close();
                fReader.close();
            } 
            catch (IOException e)
            {

            }
        }        
    }
}
