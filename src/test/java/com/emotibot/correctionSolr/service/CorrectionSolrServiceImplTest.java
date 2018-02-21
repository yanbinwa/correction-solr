package com.emotibot.correctionSolr.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.HttpResponse;
import com.emotibot.middleware.utils.HttpUtils;
import com.emotibot.middleware.utils.JsonUtils;
import com.emotibot.middleware.utils.StringUtils;
import com.emotibot.middleware.utils.UrlUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import au.com.bytecode.opencsv.CSVReader;


public class CorrectionSolrServiceImplTest
{
    public static final String correctionFile = "/Users/emotibot/Documents/workspace/other/correction-solr/file/自动纠错5.csv";
    public static final String service_url = "http://172.16.101.61:9100/correction/postCorrectionName";
    public static final String service_url1 = "http://172.16.101.61:15901/correction/v1/";
    
    public static int totalCount = 0;
    public static int errorTotalCount = 0;
    public static int emptyCount = 0;
    
    public static int LIMMIT_SIZE = 10000;
    public static int THREAD_NUM = 10;
    public static final String service_url2 = "http://localhost:9100/correction/getCorrectionName";
    public static final String outputFile = "/Users/emotibot/Documents/workspace/other/correction-solr/file/output.xlsx";
    public static final String logFile = "/Users/emotibot/Documents/workspace/other/correction-solr/file/纠错日志.csv";
    
    @Test
    public void test()
    {
        long startTime = System.currentTimeMillis();
        test4();
        long endTime = System.currentTimeMillis();
        System.out.println("用时：[" + (endTime - startTime) + "ms]");
        System.out.println("totalCount: " + totalCount + "; errorCount: " + errorTotalCount + "; emptyCount: " + emptyCount);
        System.out.println("errorRate: " + (errorTotalCount / (double)totalCount) + "; emptyRate: " + (emptyCount / (double)errorTotalCount));
    }

    @SuppressWarnings("unused")
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
                String correctSentence = ss[0].trim();
                String errorSentence = ss[1].trim();
                if (StringUtils.isEmpty(correctSentence) || StringUtils.isEmpty(errorSentence))
                {
                    continue;
                }
                totalCount ++;
                HttpRequest request = new HttpRequest(service_url, errorSentence, HttpRequestType.POST);
                HttpResponse response = HttpUtils.call(request, 10000);
                String result = response.getResponse();
                if (!isCorrected(result, correctSentence))
                {
                    System.out.println("correct: " + correctSentence + "; error: " + errorSentence + "; result: " + result);
                    errorTotalCount ++;
                    if (isEmpty(result))
                    {
                        emptyCount ++;
                    }
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
    
    /**
     * 第一版correction结果
     * 
     */
    @SuppressWarnings("unused")
    private void test2()
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
                String correctSentence = ss[1];
                String errorSentence = ss[0];
                HttpRequest request = new HttpRequest(service_url1 + UrlUtils.urlEncode(errorSentence), null, HttpRequestType.GET);
                //HttpRequest request = new HttpRequest(service_url, correctSentence, HttpRequestType.POST);
                HttpResponse response = HttpUtils.call(request, 10000);
                String result = response.getResponse();
                JsonObject obj = (JsonObject) JsonUtils.getObject(result, JsonObject.class);
                String name = obj.get("spellCheck").getAsString();
                name = UrlUtils.urlDecode(name);
                if (!isCorrected(name, correctSentence))
                {
                    System.out.println("correct: " + correctSentence + "; error: " + errorSentence + "; result: " + name);
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
    
    /**
     * 对用户日志进行纠错（多线程处理）
     * 
     * 如果得到片名，并且片名不是包含在用户的语句中的，进行标记
     * 
     * 将结果输出到文件中
     */
    @SuppressWarnings("unused")
    private void test3()
    {
        File file = new File(logFile);
        FileReader fReader = null;
        CSVReader csvReader = null; 
        try
        {
            Set<String> testSentence = new HashSet<String>();
            fReader = new FileReader(file);
            csvReader = new CSVReader(fReader);
            List<String[]> list = csvReader.readAll();
            int count = 0;
            for (String[] ss : list)
            {
                testSentence.add(ss[0]);
                if (count > LIMMIT_SIZE)
                {
                    break;
                }
                count ++;
            }
            count = 0;
            List<List<String>> sentenceBacket = new ArrayList<List<String>>(THREAD_NUM);
            for (int i = 0; i < THREAD_NUM; i ++)
            {
                sentenceBacket.add(new ArrayList<String>());
            }
            for (String sentence : testSentence)
            {
                int index = count % THREAD_NUM;
                List<String> backet = sentenceBacket.get(index);
                if (backet == null)
                {
                    backet = new ArrayList<String>();
                    sentenceBacket.add(index, backet);
                }
                backet.add(sentence);
                count ++;
            }
            List<Thread> threadList = new ArrayList<Thread>();
            List<TestTask> taskList = new ArrayList<TestTask>();
            for (int i = 0; i < THREAD_NUM; i ++)
            {
                TestTask test = new TestTask(sentenceBacket.get(i));
                Thread thread = new Thread(test);
                thread.start();
                threadList.add(thread);
                taskList.add(test);
            }
            for (Thread thread : threadList)
            {
                thread.join();
            }
            
            List<List<String>> results = new ArrayList<List<String>>();
            for (int i = 0; i < THREAD_NUM; i ++)
            {
                results.addAll(taskList.get(i).getResult());
            }
            writeXls(results, outputFile);
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
    
    /**
     * 对用户日志进行纠错（多线程处理）
     * 
     * 如果得到片名，并且片名不是包含在用户的语句中的，进行标记
     * 
     * 将结果输出到文件中
     */
    @SuppressWarnings("unused")
    private void test4()
    {
        File file = new File(correctionFile);
        FileReader fReader = null;
        CSVReader csvReader = null; 
        try
        {
            Set<String> testSentence = new HashSet<String>();
            fReader = new FileReader(file);
            csvReader = new CSVReader(fReader);
            List<String[]> list = csvReader.readAll();
            List<List<String>> rets = new ArrayList<List<String>>();
            for (String[] ss : list)
            {
                String correctSentence = ss[0].trim();
                String errorSentence = ss[1].trim();
                if (StringUtils.isEmpty(correctSentence) || StringUtils.isEmpty(errorSentence))
                {
                    continue;
                }
                totalCount ++;
                HttpRequest request = new HttpRequest(service_url, errorSentence, HttpRequestType.POST);
                HttpResponse response = HttpUtils.call(request, 10000);
                String result = response.getResponse();
                List<String> ret = new ArrayList<String>();
                ret.add(errorSentence);
                ret.add(correctSentence);
                String retName = getResult(result);
                if (StringUtils.isEmpty(retName))
                {
                    ret.add("");
                    ret.add("False");
                    errorTotalCount ++;
                    emptyCount ++;
                }
                else if (!retName.equals(correctSentence))
                {
                    ret.add(retName);
                    ret.add("False");
                    errorTotalCount ++;
                }
                else
                {
                    ret.add(retName);
                    ret.add("True");
                }
                rets.add(ret);
            }
            writeXls(rets, outputFile);
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
    
    private String getResult(String result)
    {
        JsonObject obj = (JsonObject) JsonUtils.getObject(result, JsonObject.class);
        if (obj == null || !obj.has("likely_names"))
        {
            return null;
        }
        JsonArray array = obj.get("likely_names").getAsJsonArray();
        if (array.size() == 0)
        {
            return null;
        }
        return array.get(0).getAsString();
    }
    
    private boolean isCorrected(String result, String correctSentence)
    {
        JsonObject obj = (JsonObject) JsonUtils.getObject(result, JsonObject.class);
        if (obj == null || !obj.has("likely_names"))
        {
            return false;
        }
        String resultStr = obj.get("likely_names").toString();
        resultStr = resultStr.replace("[", "");
        resultStr = resultStr.replace("]", "");
        resultStr = resultStr.replace("\"", "");
        String[] names = resultStr.split(",");
        for (String name : names)
        {
            if (name.trim().equals(correctSentence))
            {
                return true;
            }
        }
        return false;
    }
    
    private boolean isEmpty(String result)
    {
        JsonObject obj = (JsonObject) JsonUtils.getObject(result, JsonObject.class);
        if (obj == null || !obj.has("likely_names"))
        {
            return false;
        }
        String resultStr = obj.get("likely_names").toString();
        resultStr = resultStr.replace("[", "");
        resultStr = resultStr.replace("]", "");
        resultStr = resultStr.replace("\"", "");
        return "".equals(resultStr.trim());
    }
    
    private boolean writeXls(List<List<String>> cellLists, String fileName)
    {
        OutputStream os = null;
        XSSFWorkbook xssfWorkbook = null;
        try
        {
            os = new FileOutputStream(fileName);
            xssfWorkbook = new XSSFWorkbook();
            Sheet sheet = xssfWorkbook.createSheet();
            int rowCount = 0;
            for (List<String> cellList : cellLists)
            {
                Row row = sheet.createRow(rowCount);
                for (int i = 0; i < cellList.size(); i ++)
                {                    
                    Cell cell = row.createCell(i);
                    cell.setCellValue(cellList.get(i));
                }
                rowCount ++;
            }
            xssfWorkbook.write(os);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                os.close();
            }
            catch (Exception e)
            {
                
            }
        }
        return true;
    }
    
    class TestTask implements Runnable
    {
        List<String> testSentenceList = null;
        List<List<String>> testResultList = null;
        
        public TestTask(List<String> testSentenceList)
        {
            this.testSentenceList = testSentenceList;
        }
        
        @Override
        public void run()
        {
            testResultList = new ArrayList<List<String>>();
            for (String str : testSentenceList)
            {
                List<String> ret = new ArrayList<String>();
                HttpRequest request = new HttpRequest(service_url, str, HttpRequestType.POST);
                HttpResponse response = HttpUtils.call(request, 10000);
                String result = response.getResponse();
                JsonArray obj = (JsonArray) JsonUtils.getObject(result, JsonArray.class);
                String resultStr = "";
                if (obj.size() > 0)
                {
                    resultStr = obj.get(0).getAsString();
                }
                
                ret.add(str);
                ret.add(resultStr);
                if (StringUtils.isEmpty(resultStr))
                {
                    ret.add("");
                }
                else if (str.indexOf(resultStr) >= 0)
                {
                    ret.add("相同");
                }
                else
                {
                    ret.add("不同");
                }
                testResultList.add(ret);
            }
        }
        
        public List<List<String>> getResult()
        {
            return testResultList;
        }
    }
}
