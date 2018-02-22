package com.emotibot.correctionSolr.utils;

import com.emotibot.correction.utils.PinyinUtils;
import com.emotibot.correction.utils.SegementUtils;
import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.element.CorrectionElement;
import com.emotibot.correctionSolr.element.DatabaseType;

public class CorrectionElementUtils
{
    
    public static CorrectionElement getCorrectionElement(String appid, String line, String id, DatabaseType type)
    {
        String[] elements = line.split(Constants.LINE_FIELD_SPLIT);
        if (elements.length < 2)
        {
            return null;
        }
        String field = elements[0].trim();
        String text = elements[1].trim();
        CorrectionElement correctionElement = new CorrectionElement();
        correctionElement.setId(id);
        correctionElement.setAppid(appid);
        correctionElement.setField(field);
        switch(type)
        {
        case SINGLE_WORD_DATABASE:
            getSingleWordElement(correctionElement, text);
            break;
        case WORD_DATABASE:
            getWordElement(correctionElement, text);
            break;
        case WORD_SYN_DATABASE:
            getSynWordElement(correctionElement, text);
            break;
        case PINYING_WORD_DATABASE:
            getPinyinElement(correctionElement, text);
            break;
        case PINYING2_WORD_DATABASE:
            getPinyin2Element(correctionElement, text);
            break;
        default:
            return null;
        }
        return correctionElement;
    }
    
    private static void getSingleWordElement(CorrectionElement correctionElement, String text)
    {
        char[] str2char = text.toCharArray();
        String singleWordStr = "";
        for (int t = 0; t < str2char.length; t++)
        {
            singleWordStr += String.valueOf(str2char[t]) + " ";
        }
        correctionElement.setSentence(singleWordStr.trim());
        correctionElement.setSentence_syn(singleWordStr.trim());
        correctionElement.setSentence_original(text);
        correctionElement.setDatabase(DatabaseType.SINGLE_WORD_DATABASE.name());
    }
    
    private static void getWordElement(CorrectionElement correctionElement, String text)
    {
        String[] words = SegementUtils.segementString(text);
        String wordsStr = "";
        for (int t = 0; t < words.length; t++)
        {
            wordsStr += String.valueOf(words[t]) + " ";
        }
        correctionElement.setSentence(wordsStr.trim());
        correctionElement.setSentence_syn(wordsStr.trim());
        correctionElement.setSentence_original(text);
        correctionElement.setDatabase(DatabaseType.WORD_DATABASE.name());
    }
    
    private static void getSynWordElement(CorrectionElement correctionElement, String text)
    {
        String[] words = SegementUtils.segementString(text);
        String wordsStr = "";
        for (int t = 0; t < words.length; t++)
        {
            wordsStr += String.valueOf(words[t]) + " ";
        }
        correctionElement.setSentence(wordsStr.trim());
        correctionElement.setSentence_syn(wordsStr.trim());
        correctionElement.setSentence_original(text);
        correctionElement.setDatabase(DatabaseType.WORD_SYN_DATABASE.name());
    }
    
    private static void getPinyinElement(CorrectionElement correctionElement, String text)
    {
        String pinyins = PinyinUtils.getPinyin(text);
        String[] pinyinArray = pinyins.split(PinyinUtils.PINYIN_SPLIT);
        String pinyinStr = "";
        for (int t = 0; t < pinyinArray.length; t++)
        {
            pinyinStr += String.valueOf(pinyinArray[t]) + " ";
        }
        correctionElement.setSentence(pinyinStr.trim());
        correctionElement.setSentence_syn(pinyinStr.trim());
        correctionElement.setSentence_original(text);
        correctionElement.setDatabase(DatabaseType.PINYING_WORD_DATABASE.name());
    }
    
    private static void getPinyin2Element(CorrectionElement correctionElement, String text)
    {
        String pinyin2s = PinyinUtils.getPinyin2(text);
        String[] pinyin2Array = pinyin2s.split(PinyinUtils.PINYIN_SPLIT);
        String pinyin2Str = "";
        for (int t = 0; t < pinyin2Array.length; t++)
        {
            pinyin2Str += String.valueOf(pinyin2Array[t]) + " ";
        }
        correctionElement.setSentence(pinyin2Str.trim());
        correctionElement.setSentence_syn(pinyin2Str.trim());
        correctionElement.setSentence_original(text);
        correctionElement.setDatabase(DatabaseType.PINYING2_WORD_DATABASE.name());
    }
}
