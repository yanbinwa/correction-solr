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
import com.emotibot.middleware.utils.StringUtils;
import com.emotibot.middleware.utils.UrlUtils;

import au.com.bytecode.opencsv.CSVReader;

public class CorrectionCommandServiceImplTest
{

    public static final String correctionFile = 
            "/Users/emotibot/Documents/workspace/other/correction-solr/file/指令纠错测试.csv";
    public static final String service_url = "http://172.16.101.61:9100/correction/getCorrectionCommand?text=";
    
    @Test
    public void test()
    {
        long startTime = System.currentTimeMillis();
        test1();
        long endTime = System.currentTimeMillis();
        System.out.println("用时：[" + (endTime - startTime) + "ms]");
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
                String correctSentence = ss[1].trim();
                String errorSentence = ss[0].trim();
                String tag = ss[2].trim();
                if (StringUtils.isEmpty(correctSentence) || StringUtils.isEmpty(errorSentence))
                {
                    continue;
                }
                HttpRequest request = new HttpRequest(service_url + UrlUtils.urlEncode(errorSentence), null, HttpRequestType.GET);
                HttpResponse response = HttpUtils.call(request, 10000);
                String result = response.getResponse();
                if (!isCorrected(result, correctSentence) && tag.equals("0"))
                {
                    System.out.println("correct: " + correctSentence + "; error: " + errorSentence + "; result: " + result);
                }
                else if (isCorrected(result, correctSentence) && !tag.equals("0"))
                {
                    System.out.println("correct: " + correctSentence + "; error: " + errorSentence + "; result: " + result);
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

    private boolean isCorrected(String result, String correctSentence)
    {
        result = result.replace("[", "");
        result = result.replace("]", "");
        result = result.replace("\"", "");
        String[] names = result.split(",");
        for (String name : names)
        {
            if (name.trim().equals(correctSentence))
            {
                return true;
            }
        }
        return false;
    }
}
