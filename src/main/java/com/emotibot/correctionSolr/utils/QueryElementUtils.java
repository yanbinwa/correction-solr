package com.emotibot.correctionSolr.utils;

import com.emotibot.correction.utils.PinyinUtils;
import com.emotibot.correction.utils.SegementUtils;
import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.element.DatabaseType;
import com.emotibot.correctionSolr.element.QueryElement;

public class QueryElementUtils
{
    public static QueryElement getQueryElement(String text, DatabaseType type)
    {
        QueryElement queryElement = new QueryElement();
        queryElement.setFl("*,score");
        queryElement.setFq("database:" + type.name());
        queryElement.setDatabase(type);
        String q = null;
        switch(type)
        {
        case SINGLE_WORD_DATABASE:
            q = getSingleWordQ(text);
            break;
        case WORD_DATABASE:
            q = getWordQ(text);
            break;
        case WORD_SYN_DATABASE:
            q= getWordQ(text);
            queryElement.setDefType("dismax");
            queryElement.setQf(Constants.SOLR_SENTENCE_SYN_FIELD);
            //queryElement.setFq(null);
            break;
        case PINYING_WORD_DATABASE:
            q = getPinyinQ(text);
            break;
        case PINYING2_WORD_DATABASE:
            q = getPinyin2Q(text);
            break;
        default:
            q = "*:*";
            break;
        }
        queryElement.setQ(q);
        return queryElement;
    }
    
    private static String getSingleWordQ(String text)
    {
        char[] str2char = text.toCharArray();
        String singleWordStr = "";
        for (int t = 0; t < str2char.length; t++)
        {
            singleWordStr += String.valueOf(str2char[t]) + " ";
        }
        return singleWordStr.trim();
    }
    
    private static String getWordQ(String text)
    {
        String[] words = SegementUtils.segementString(text);
        String wordsStr = "";
        for (int t = 0; t < words.length; t++)
        {
            wordsStr += String.valueOf(words[t]) + " ";
        }
        return wordsStr.trim();
    }
    
    private static String getPinyinQ(String text)
    {
        String pinyins = PinyinUtils.getPinyin(text);
        String[] pinyinArray = pinyins.split(PinyinUtils.PINYIN_SPLIT);
        String pinyinStr = "";
        for (int t = 0; t < pinyinArray.length; t++)
        {
            pinyinStr += String.valueOf(pinyinArray[t]) + " ";
        }
        return pinyinStr.trim();
    }
    
    private static String getPinyin2Q(String text)
    {
        String pinyin2s = PinyinUtils.getPinyin2(text);
        String[] pinyin2Array = pinyin2s.split(PinyinUtils.PINYIN_SPLIT);
        String pinyin2Str = "";
        for (int t = 0; t < pinyin2Array.length; t++)
        {
            pinyin2Str += String.valueOf(pinyin2Array[t]) + " ";
        }
        return pinyin2Str.trim();
    }
}
