package com.emotibot.correctionSolr.utils;

import com.emotibot.correction.utils.PinyinUtils;
import com.emotibot.correction.utils.SegementUtils;
import com.emotibot.correctionSolr.element.CorrectionElement;
import com.emotibot.correctionSolr.element.DatabaseType;

public class CorrectionElementUtils
{
    
    public static CorrectionElement getCorrectionElement(String appid, String text, String id, DatabaseType type)
    {
        switch(type)
        {
        case SINGLE_WORD_DATABASE:
            return getSingleWordElement(appid, text, id);
        case WORD_DATABASE:
            return getWordElement(appid, text, id);
        case WORD_SYN_DATABASE:
            return getSynWordElement(appid, text, id);
        case PINYING_WORD_DATABASE:
            return getPinyinElement(appid, text, id);
        case PINYING2_WORD_DATABASE:
            return getPinyin2Element(appid, text, id);
        default:
            return null;
        }
    }
    
    private static CorrectionElement getSingleWordElement(String appid, String text, String id)
    {
        char[] str2char = text.toCharArray();
        String singleWordStr = "";
        for (int t = 0; t < str2char.length; t++)
        {
            singleWordStr += String.valueOf(str2char[t]) + " ";
        }
        CorrectionElement singleWordEle = new CorrectionElement();
        singleWordEle.setId(id);
        singleWordEle.setSentence(singleWordStr.trim());
        singleWordEle.setSentence_syn(singleWordStr.trim());
        singleWordEle.setSentence_original(text);
        singleWordEle.setDatabase(DatabaseType.SINGLE_WORD_DATABASE.name());
        singleWordEle.setAppid(appid);
        return singleWordEle;
    }
    
    private static CorrectionElement getWordElement(String appid, String text, String id)
    {
        String[] words = SegementUtils.segementString(text);
        String wordsStr = "";
        for (int t = 0; t < words.length; t++)
        {
            wordsStr += String.valueOf(words[t]) + " ";
        }
        CorrectionElement wordEle = new CorrectionElement();
        wordEle.setId(id);
        wordEle.setSentence(wordsStr.trim());
        wordEle.setSentence_syn(wordsStr.trim());
        wordEle.setSentence_original(text);
        wordEle.setDatabase(DatabaseType.WORD_DATABASE.name());
        wordEle.setAppid(appid);
        return wordEle;
    }
    
    private static CorrectionElement getSynWordElement(String appid, String text, String id)
    {
        String[] words = SegementUtils.segementString(text);
        String wordsStr = "";
        for (int t = 0; t < words.length; t++)
        {
            wordsStr += String.valueOf(words[t]) + " ";
        }
        CorrectionElement wordEle = new CorrectionElement();
        wordEle.setId(id);
        wordEle.setSentence(wordsStr.trim());
        wordEle.setSentence_syn(wordsStr.trim());
        wordEle.setSentence_original(text);
        wordEle.setDatabase(DatabaseType.WORD_SYN_DATABASE.name());
        wordEle.setAppid(appid);
        return wordEle;
    }
    
    private static CorrectionElement getPinyinElement(String appid, String text, String id)
    {
        String pinyins = PinyinUtils.getPinyin(text);
        String[] pinyinArray = pinyins.split(PinyinUtils.PINYIN_SPLIT);
        String pinyinStr = "";
        for (int t = 0; t < pinyinArray.length; t++)
        {
            pinyinStr += String.valueOf(pinyinArray[t]) + " ";
        }
        CorrectionElement pinyingEle = new CorrectionElement();
        pinyingEle.setId(id);
        pinyingEle.setSentence(pinyinStr.trim());
        pinyingEle.setSentence_syn(pinyinStr.trim());
        pinyingEle.setSentence_original(text);
        pinyingEle.setDatabase(DatabaseType.PINYING_WORD_DATABASE.name());
        pinyingEle.setAppid(appid);
        return pinyingEle;
    }
    
    private static CorrectionElement getPinyin2Element(String appid, String text, String id)
    {
        String pinyin2s = PinyinUtils.getPinyin2(text);
        String[] pinyin2Array = pinyin2s.split(PinyinUtils.PINYIN_SPLIT);
        String pinyin2Str = "";
        for (int t = 0; t < pinyin2Array.length; t++)
        {
            pinyin2Str += String.valueOf(pinyin2Array[t]) + " ";
        }
        CorrectionElement pinying2Ele = new CorrectionElement();
        pinying2Ele.setId(id);
        pinying2Ele.setSentence(pinyin2Str.trim());
        pinying2Ele.setSentence_syn(pinyin2Str.trim());
        pinying2Ele.setSentence_original(text);
        pinying2Ele.setDatabase(DatabaseType.PINYING2_WORD_DATABASE.name());
        pinying2Ele.setAppid(appid);
        return pinying2Ele;
    }
}
