package com.emotibot.correctionSolr.changhong;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.HttpResponse;
import com.emotibot.middleware.utils.FileUtils;
import com.emotibot.middleware.utils.HttpUtils;
import com.emotibot.middleware.utils.JsonUtils;
import com.emotibot.middleware.utils.StringUtils;
import com.emotibot.middleware.utils.UrlUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CorrectionTest
{
    public static final String correctionFile = "file/长虹日志3000.xlsx";
    public static final String correctionOutputFile = "file/长虹日志_output.xlsx";
    public static final String correctionUrl = "http://localhost:9100/correction/getCorrectionName?text=";
    
    @Test
    public void test()
    {
        List<List<String>> contents = FileUtils.readFileFromXlsx(correctionFile);
        List<TestInfo> testInfoList = new ArrayList<TestInfo>();
        for (List<String> content : contents)
        {
            if (!content.get(2).equals("{}"))
            {
                continue;
            }
            TestInfo info = new TestInfo();
            info.setDomain(content.get(1));
            info.setSemantic(content.get(2));
            info.setText(content.get(3));
            info.setIntent(content.get(4));
            testInfoList.add(info);
        }
        for(TestInfo testInfo : testInfoList)
        {
            HttpRequest request = new HttpRequest(correctionUrl + UrlUtils.urlEncode(testInfo.getText()), null, HttpRequestType.GET);
            HttpResponse response = HttpUtils.call(request, 10000);
            String result = response.getResponse();
            JsonObject jsonObject = (JsonObject) JsonUtils.getObject(result, JsonObject.class);
            if (!jsonObject.has("likely_names"))
            {
                continue;
            }
            JsonArray likelyNames = jsonObject.get("likely_names").getAsJsonArray();
            if (likelyNames.size() == 0)
            {
                continue;
            }
            testInfo.setCorrection(likelyNames.get(0).getAsString());
        }
        List<List<String>> outputs = new ArrayList<List<String>>();
        for (TestInfo testInfo : testInfoList)
        {
            List<String> output = new ArrayList<String>();
            output.add(testInfo.getDomain());
            output.add(testInfo.getIntent());
            output.add(testInfo.getText());
            if (StringUtils.isEmpty(testInfo.getCorrection()))
            {
                output.add("");
            }
            else
            {
                output.add(testInfo.getCorrection());
            }
            outputs.add(output);
        }
        FileUtils.writeLogForXls(correctionOutputFile, outputs);
    }

    class TestInfo
    {
        @SerializedName("semantic")
        @Expose
        private String semantic;
        
        @SerializedName("text")
        @Expose
        private String text;
        
        @SerializedName("correction")
        @Expose
        private String correction;
        
        @SerializedName("domain")
        @Expose
        private String domain;
        
        @SerializedName("intent")
        @Expose
        private String intent;
        
        public TestInfo()
        {
            
        }
        
        public void setSemantic(String semantic)
        {
            this.semantic = semantic;
        }
        
        public String getSemantic()
        {
            return this.semantic;
        }
        
        public void setText(String text)
        {
            this.text = text;
        }
        
        public String getText()
        {
            return this.text;
        }
        
        public void setCorrection(String correction)
        {
            this.correction = correction;
        }
        
        public String getCorrection()
        {
            return this.correction;
        }
        
        public void setDomain(String domain)
        {
            this.domain = domain;
        }
        
        public String getDomain()
        {
            return this.domain;
        }
        
        public void setIntent(String intent)
        {
            this.intent = intent;
        }
        
        public String getIntent()
        {
            return this.intent;
        }
        
        @Override
        public String toString()
        {
            return JsonUtils.getJsonStr(this);
        }
    }
}
