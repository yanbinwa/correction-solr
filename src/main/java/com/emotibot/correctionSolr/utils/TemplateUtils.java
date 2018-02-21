package com.emotibot.correctionSolr.utils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.emotibot.correction.utils.EditDistanceUtils;
import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.HttpResponse;
import com.emotibot.middleware.utils.HttpUtils;
import com.emotibot.middleware.utils.JsonUtils;
import com.emotibot.middleware.utils.StringUtils;
import com.emotibot.middleware.utils.UrlUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TemplateUtils
{
    public static final String TEMPLATE_TAG = "XXX";
    public static final String PERSON_TAG = "[Person]";
    public static final String SEASON_TAG = "[Season]";
    public static final String VIDEO_TAG = "[Video]";
    public static final String MUSIC_TAG = "[Music]";
    //public static final String[] SPECIAL_TAGS = {PERSON_TAG, SEASON_TAG, VIDEO_TAG, MUSIC_TAG};
    public static final String[] SPECIAL_TAGS = {VIDEO_TAG};
    public static final String NAME_ENTITY_PERSON_TAG = "<START:PER>";
    public static final String NAME_ENTITY_MOVIE_TAG = "<START:MOVIE>";
    public static final String NAME_ENTITY_MUSIC_TAG = "<START:MUSIC>";
    //public static final String[] NAME_ENTITY_TAGS = {NAME_ENTITY_PERSON_TAG, SEASON_TAG, NAME_ENTITY_MOVIE_TAG, NAME_ENTITY_MUSIC_TAG};
    public static final String[] NAME_ENTITY_TAGS = {NAME_ENTITY_MOVIE_TAG};
    public static final String NAME_ENTITY_END_TAG = "<END>";
    
    private static final String SEASON_PATTERN = "((第)?(((\\d+))|([一,二,三,四,五,六,七,八,九,十]+))集)|(第(((\\d+))|([一,二,三,四,五,六,七,八,九,十]+))(部|期|季))";
    private static Pattern pattern = Pattern.compile(SEASON_PATTERN);
    
    private static Map<String, String> templateTagToNameEntityMap;
    private static final String targetTag = ConfigManager.INSTANCE.getPropertyString(Constants.TEMPLATE_TAG_KEY);
    
    static
    {
        templateTagToNameEntityMap = new HashMap<String, String>();
        for (int i = 0; i < SPECIAL_TAGS.length; i ++)
        {
            templateTagToNameEntityMap.put(SPECIAL_TAGS[i], NAME_ENTITY_TAGS[i]);
        }
    }
    
    public static List<String> fetchSeasonStr(String sentence)
    {
        Matcher matcher = pattern.matcher(sentence);
        if (!matcher.find())
        {
            return null;
        }
        int start = matcher.start();
        int end = matcher.end();
        List<String> ret = new ArrayList<String>();
        ret.add(sentence.substring(start, end));
        return ret;
    }
    
    //1. 获取nameEntity
    //2. 获取第几部等
    //3. 获取演员名
    public static String adjustSentence(String sentence, Map<String, List<String>> specicalTagMap)
    {
        if (StringUtils.isEmpty(targetTag))
        {
            return sentence;
        }
        String nameEntity = NluUtils.getNameEntitis(sentence);
        if (StringUtils.isEmpty(nameEntity))
        {
            return sentence;
        }
        for (String tag : SPECIAL_TAGS)
        {
            if (tag.equals(targetTag))
            {
                continue;
            }
            List<String> entities = null;
            if (tag.equals(SEASON_TAG))
            {
                entities = fetchSeasonStr(sentence);
            }
            else
            {
                entities = NluUtils.fetchNameEntitiesByTag(nameEntity, templateTagToNameEntityMap.get(tag));
            }
            if (entities == null || entities.isEmpty())
            {
                continue;
            }
            List<String> entityList = specicalTagMap.get(tag);
            if (entityList == null)
            {
                entityList = new ArrayList<String>();
                specicalTagMap.put(tag, entityList);
            }
            for(String entity : entities)
            {
                entityList.add(entity);
            }
        }
        for (Map.Entry<String, List<String>> entry : specicalTagMap.entrySet())
        {
            String tag = entry.getKey();
            for (String value : entry.getValue())
            {
                sentence = StringUtils.replaceFirst(sentence, value, tag);
            }
        }
        return sentence;
    }
    
    public static boolean isValidTemplate(String template, Map<String, List<String>> specicalTagMap)
    {
        for (Map.Entry<String, List<String>> entry : specicalTagMap.entrySet())
        {
            String tag = entry.getKey();
            int count = StringUtils.appearNumber(template, tag);
            if (count != entry.getValue().size())
            {
                return false;
            }
        }
        for (String tag : SPECIAL_TAGS)
        {
            if (template.indexOf(tag) >= 0 && !specicalTagMap.containsKey(tag))
            {
                return false;
            }
        }
        return true;
    }
    
    public static String adjustTemplate(String template, Map<String, List<String>> specicalTagMap)
    {
        for (Map.Entry<String, List<String>> entry : specicalTagMap.entrySet())
        {
            String tag = entry.getKey();
            for (String value : entry.getValue())
            {
                template = StringUtils.replaceFirst(template, tag, value);
            }
        }
        return template;
    }
    
    /**
     * TODO: 对于模板的分数进行梯度下降排序
     * @param sentence
     * @return
     */
    public static List<String> fetchTemplates(String sentence)
    {
        String url = ConfigManager.INSTANCE.getPropertyString(Constants.FETCH_TEMPLATE_URL_KEY)
                + UrlUtils.urlEncode(sentence);
        HttpRequest request = new HttpRequest(url, null, HttpRequestType.GET);
        try
        {
            HttpResponse response = HttpUtils.call(request, 1000);
            int stateCode = response.getStateCode();
            if (stateCode != HttpURLConnection.HTTP_OK)
            {
                return null;
            }
            String responseStr = response.getResponse();
            JsonObject jsonObject = (JsonObject) JsonUtils.getObject(responseStr, JsonObject.class);
            if (jsonObject == null)
            {
                return null;
            }
            List<String> ret = new ArrayList<String>();
            for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
            {
                double rate = entry.getValue().getAsDouble();
                if (rate > Constants.TEMPLATE_RATE_1_THRESHOLD)
                {
                    ret.add(entry.getKey());
                }
            }
            return ret;
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    /**
     * 计算模板到真实句子的相似度，需要考虑最大匹配的分数
     * 
     * 要考虑“我要看XXX”和“我要看XXX第一季”，如果输入是"我要看大话西游第二季"，尽量匹配到“我要看XXX第一季”
     * @param sentence
     * @return
     */
    public static double getDistanceOfSentenceAndTemplate(String sentence, String template)
    {
        int sentenceLen = sentence.length();
        int templateLen = template.length();
        
        if (sentenceLen <= (templateLen - TEMPLATE_TAG.length()))
        {
            return Double.MAX_VALUE;
        }
        int index = template.indexOf(TEMPLATE_TAG);
        if (index < 0)
        {
            return Double.MAX_VALUE;
        }
        String newSentence1 = sentence.substring(0, index);
        String newSentence2 = sentence.substring(sentenceLen - (templateLen - (index + TEMPLATE_TAG.length())));
        String newTemplate1 = template.substring(0, index);
        String newTemplate2 = template.substring(index + TEMPLATE_TAG.length());
        double distance = EditDistanceUtils.getEditDistance(newSentence1, newTemplate1) + EditDistanceUtils.getEditDistance(newSentence2, newTemplate2);
        distance = distance - (templateLen - TEMPLATE_TAG.length()) * Constants.TEMPLATE_DIFF_RATE;
        if (distance <= Constants.TEMPLATE_RATE_2_THRESHOLD)
        {
            return distance;
        }
        else
        {
            return Double.MAX_VALUE;
        }
    }
}
