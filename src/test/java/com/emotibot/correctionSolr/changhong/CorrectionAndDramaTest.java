package com.emotibot.correctionSolr.changhong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CorrectionAndDramaTest
{
    public static final String correctionFile = "file/长虹日志3000.xlsx";
    //public static final String correctionFile = "file/长虹日志选.xlsx";
    public static final String correctionOutputFile = "file/长虹日志_output.xlsx";
    public static final String correctionUrl = "http://localhost:9100/correction/getCorrectionName?text=";
    public static final String correctionLikelyUrl = "http://localhost:9100/correction/getLikelyName?text=";
    public static final String dramaUrl = "http://localhost:9101/drama/getName?text=";
    
    public static final String[] KEEP_TAGS = {"actor", "director", "type", "category", "tag", "station"};
    public static final String[] PERSON_PROFIXS = {"导演的", "演的", "的"};
    
    private static Set<String> keepTagSet;
    
    
    @Test
    public void test()
    {
        keepTagSet = new HashSet<String>();
        for (String tag : KEEP_TAGS)
        {
            keepTagSet.add(tag);
        }
        test2();
    }
    
    public void test1()
    {
        List<List<String>> contents = FileUtils.readFileFromXlsx(correctionFile);
        List<TestInfo> testInfoList = new ArrayList<TestInfo>();
        for (List<String> content : contents)
        {
            if (content.get(2).equals("{}"))
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
            String sentence = testInfo.getText();
            
            //获取可能结果
            String likelyName = getLikelyName(sentence);
            if (!StringUtils.isEmpty(likelyName))
            {
                sentence = likelyName;
            }
            
            //获取纠错片名
            String correctionName = getCorrectionName(sentence);
            if (!StringUtils.isEmpty(correctionName))
            {
                testInfo.setCorrection(correctionName);
            }

            //通过剧情进行搜索
            String drama = getDramaName(sentence);
            if (!StringUtils.isEmpty(drama))
            {
                testInfo.setCorrection(drama);
            }
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
    
    /**
     * 模拟parser的请求
     * 
     * 1. 如果无意图，走纠错
     * 
     * 2. 如有有意图，并且为求视频意图
     * 
     * 如果解析出actor，director，type，category，tag, station 去掉这些（对于actor和director，需要将其后跟着的"演的，导演的，的"去掉后）
     * 如果剩下一些内容，且长度超过一定长度，可以进行纠错，如果纠错失败，可以进行剧情纠错
     * 
     * 如果只解析出其他属性的词，剔除后还有一定长度，可以进行纠错，如果失败， 整句进行剧情纠错
     * 
     * 将结果输出
     * 
     */
    public void test2()
    {
        List<List<String>> contents = FileUtils.readFileFromXlsx(correctionFile);
        List<TestInfo> testInfoList = new ArrayList<TestInfo>();
        for (List<String> content : contents)
        {
            if ((content.get(1).equals("NULL") && content.get(4).equals("NULL")) || (content.get(1).equals("QUERY") && content.get(4).equals("VIDEO") && !content.get(2).contains("name")))
            {
                TestInfo info = new TestInfo();
                info.setDomain(content.get(1));
                info.setSemantic(content.get(2));
                info.setText(content.get(3));
                info.setOrgText(content.get(3));
                info.setIntent(content.get(4));
                testInfoList.add(info);
            }
        }
        for(TestInfo testInfo : testInfoList)
        {
            //获取可能结果
            String likelyName = getLikelyName(testInfo.getText());
            if (!StringUtils.isEmpty(likelyName))
            {
                testInfo.setText(likelyName);
            }
            
            //获取纠错片名
            String correctionSentence = getCorrectionSentence(testInfo);
            if (!StringUtils.isEmpty(correctionSentence))
            {
                String correctionName = getCorrectionName(correctionSentence);
                if (!StringUtils.isEmpty(correctionName))
                {
                    testInfo.setCorrection(correctionName);
                    continue;
                }

                //通过剧情进行搜索
                if (!testInfo.getDomain().equals("NULL"))
                {
                    String drama = getDramaName(testInfo.getText());
                    if (!StringUtils.isEmpty(drama))
                    {
                        testInfo.setCorrection(drama);
                    }
                }
            }
        }
        List<List<String>> outputs = new ArrayList<List<String>>();
        for (TestInfo testInfo : testInfoList)
        {
            List<String> output = new ArrayList<String>();
            output.add(testInfo.getDomain());
            output.add(testInfo.getIntent());
            output.add(testInfo.getText());
            output.add(testInfo.getOrgText());
            output.add(testInfo.getSemantic());
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
    
    private String getCorrectionName(String text)
    {
        HttpRequest request = new HttpRequest(correctionUrl + UrlUtils.urlEncode(text), null, HttpRequestType.GET);
        HttpResponse response = HttpUtils.call(request, 10000);
        String result = response.getResponse();
        JsonObject jsonObject = (JsonObject) JsonUtils.getObject(result, JsonObject.class);
        if (jsonObject.has("likely_names"))
        {
            JsonArray likelyNames = jsonObject.get("likely_names").getAsJsonArray();
            if (likelyNames.size() > 0)
            {
                return likelyNames.get(0).getAsString();
            }
        }
        return null;
    }
    
    private String getLikelyName(String text)
    {
        HttpRequest request = new HttpRequest(correctionLikelyUrl + UrlUtils.urlEncode(text), null, HttpRequestType.GET);
        HttpResponse response = HttpUtils.call(request, 10000);
        String result = response.getResponse();
        if (!StringUtils.isEmpty(result))
        {
            return result;
        }
        return null;
    }
    
    private String getDramaName(String text)
    {
        HttpRequest request = new HttpRequest(dramaUrl + UrlUtils.urlEncode(text), null, HttpRequestType.GET);
        HttpResponse response = HttpUtils.call(request, 10000);
        String result = response.getResponse();
        JsonArray videoArray = (JsonArray) JsonUtils.getObject(result, JsonArray.class);
        if (videoArray.size() != 0)
        {
            return videoArray.toString();
        }
        return null;
    }
    
    /**
     * 只有有意图才会这样做
     * 
     * @param testInfo
     * @return
     */
    private List<TagInfo> getTagInfo(TestInfo testInfo)
    {
        if (StringUtils.isEmpty(testInfo.getSemantic()))
        {
            return null;
        }
        JsonObject jsonObject = (JsonObject) JsonUtils.getObject(testInfo.getSemantic(), JsonObject.class);
        List<TagInfo> tagInfoList = new ArrayList<TagInfo>();
        //长度有长到短来匹配
        String sentence = testInfo.getText();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
        {
            if (sentence.indexOf(entry.getValue().getAsString()) >= 0)
            {
                TagInfo tagInfo = new TagInfo();
                tagInfo.setTag(entry.getKey());
                tagInfo.setTagValue(entry.getValue().getAsString());
                tagInfoList.add(tagInfo);
            }
        }
        
        Collections.sort(tagInfoList);
        for (TagInfo tagInfo : tagInfoList)
        {
            int startIndex = sentence.indexOf(tagInfo.getTagValue());
            if (startIndex < 0)
            {
                continue;
            }
            tagInfo.setStartIndex(startIndex);
            int endIndex = startIndex + tagInfo.getTagValue().length();
            if (tagInfo.getTag().equals("director") || tagInfo.getTag().equals("actor"))
            {
                String subString = sentence.substring(endIndex);
                for(String prefix : PERSON_PROFIXS)
                {
                    if (subString.startsWith(prefix))
                    {
                        endIndex += prefix.length();
                        tagInfo.setTagValue(tagInfo.getTagValue() + prefix);
                        break;
                    }
                }
            }
            tagInfo.setEndIndex(endIndex);
        }
        
        Collections.sort(tagInfoList);
        return tagInfoList;
    }
    
    /**
     * 第一步，排除所有tag后，看是否剩下一些内容
     * 
     * @param testInfo
     * @return
     */
    private String getCorrectionSentence(TestInfo testInfo)
    {
        if (testInfo.getDomain().equals("NULL"))
        {
            return testInfo.getText();
        }
        List<TagInfo> tagInfoList = getTagInfo(testInfo);
        if (tagInfoList == null || tagInfoList.isEmpty())
        {
            return testInfo.getText();
        }
        //获取除掉后的句子
        String sentence = testInfo.getText();
        int[] sentenceTags = new int[sentence.length()];
        for (int i = 0; i < sentenceTags.length; i ++)
        {
            sentenceTags[i] = 0;
        }
        boolean isContinueSpecialTag = false;
        for (int i = 0; i < tagInfoList.size(); i ++)
        {
            TagInfo tagInfo = tagInfoList.get(i);
            if (isContinueSpecialTag && keepTagSet.contains(tagInfo.getTag()))
            {
                for (int j = tagInfoList.get(i - 1).getEndIndex(); j < tagInfo.getStartIndex(); j ++)
                {
                    sentenceTags[j] = 2;
                }
            }
            if (keepTagSet.contains(tagInfo.getTag()))
            {
                for (int j = tagInfo.getStartIndex(); j < tagInfo.getEndIndex(); j ++)
                {
                    sentenceTags[j] = 2;
                }
                isContinueSpecialTag = true;
            }
            else
            {
                for (int j = tagInfo.getStartIndex(); j < tagInfo.getEndIndex(); j ++)
                {
                    sentenceTags[j] = 1;
                }
                isContinueSpecialTag = false;
            }
        }
        int leftWordNum = 0;
        for (int i = 0; i < sentenceTags.length; i ++)
        {
            if (sentenceTags[i] == 0)
            {
                leftWordNum ++;
            }
        }
        if (leftWordNum < 4)
        {
            return null;
        }
        String newSentence = "";
        for (int i = 0; i < sentenceTags.length; i ++)
        {
            if (sentenceTags[i] == 0)
            {
                newSentence += sentence.substring(i, i + 1);
            }
        }
        return newSentence;
    }

    class TagInfo implements Comparable<TagInfo>
    {
        @SerializedName("tag")
        @Expose
        private String tag;
        @SerializedName("tagValue")
        @Expose
        private String tagValue;
        @SerializedName("startIndex")
        @Expose
        private int startIndex = -1;
        @SerializedName("endIndex")
        @Expose
        private int endIndex = -1;
        
        public TagInfo()
        {
            
        }
        
        public TagInfo(String tag, String tagValue, int startIndex, int endIndex)
        {
            this.tag = tag;
            this.tagValue = tagValue;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
        
        public void setTag(String tag)
        {
            this.tag = tag;
        }
        
        public String getTag()
        {
            return this.tag;
        }
        
        public void setTagValue(String tagValue)
        {
            this.tagValue = tagValue;
        }
        
        public String getTagValue()
        {
            return this.tagValue;
        }
        
        public void setStartIndex(int startIndex)
        {
            this.startIndex = startIndex;
        }
        
        public int getStartIndex()
        {
            return this.startIndex;
        }
        
        public void setEndIndex(int endIndex)
        {
            this.endIndex = endIndex;
        }
        
        public int getEndIndex()
        {
            return this.endIndex;
        }
        
        @Override
        public int compareTo(TagInfo o)
        {
            if (startIndex > o.startIndex)
            {
                return 1;
            }
            else if (startIndex < o.startIndex)
            {
                return -1;
            }
            else
            {
                if (this.tagValue.length() > o.tagValue.length())
                {
                    return -1;
                }
                else if (this.tagValue.length() < o.tagValue.length())
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        }
        
        @Override
        public String toString()
        {
            return JsonUtils.getJsonStr(this);
        }
        
    }
    
    class TestInfo
    {
        @SerializedName("semantic")
        @Expose
        private String semantic;
        
        @SerializedName("text")
        @Expose
        private String text;
        
        @SerializedName("orgText")
        @Expose
        private String orgText;
        
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
        
        public void setOrgText(String orgText)
        {
            this.orgText = orgText;
        }
        
        public String getOrgText()
        {
            return this.orgText;
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
