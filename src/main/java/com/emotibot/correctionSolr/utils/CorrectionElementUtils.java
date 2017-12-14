package com.emotibot.correctionSolr.utils;

import com.emotibot.correction.utils.PinyinUtils;
import com.emotibot.correction.utils.SegementUtils;
import com.emotibot.correctionSolr.element.CorrectionElement;
import com.emotibot.correctionSolr.element.DatabaseType;

public class CorrectionElementUtils
{
    
    public static CorrectionElement getCorrectionElement(String text, String id, DatabaseType type)
    {
        switch(type)
        {
        case SINGLE_WORD_DATABASE:
            return getSingleWordElement(text, id);
        case WORD_DATABASE:
            return getWordElement(text, id);
        case WORD_SYN_DATABASE:
            return getWordElement(text, id);
        case PINYING_WORD_DATABASE:
            return getPinyinElement(text, id);
        case PINYING2_WORD_DATABASE:
            return getPinyin2Element(text, id);
        default:
            return null;
        }
    }
    
    private static CorrectionElement getSingleWordElement(String text, String id)
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
        return singleWordEle;
    }
    
    private static CorrectionElement getWordElement(String text, String id)
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
        return wordEle;
    }
    
    private static CorrectionElement getPinyinElement(String text, String id)
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
        return pinyingEle;
    }
    
    private static CorrectionElement getPinyin2Element(String text, String id)
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
        return pinying2Ele;
    }
}
