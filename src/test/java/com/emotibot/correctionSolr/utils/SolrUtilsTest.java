package com.emotibot.correctionSolr.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.emotibot.correction.utils.PinyinUtils;
import com.emotibot.correction.utils.SegementUtils;
import com.emotibot.correctionSolr.element.CorrectionElement;
import com.emotibot.correctionSolr.element.DatabaseType;
import com.emotibot.correctionSolr.element.QueryElement;
import com.emotibot.correctionSolr.element.ResultElement;

public class SolrUtilsTest
{
    public static final String VIDEO_NAME = "超星星学院";
    
    public static final String SINGLE_WORD_DATABASE = "singleWord";
    public static final String WORD_DATABASE = "word";
    public static final String PINYING_WORD_DATABASE = "pinying";
    public static final String PINYING2_WORD_DATABASE = "pinying2";
    
    public static final String VIDEO_NAME_SEGEMENT = "小话 西游";
    
    public static final String[] VIDEO_NAMES = {"代孕", "母亲", "我的母亲", "代孕妈妈"};
    
    public static final String APPID = "5a200ce8e6ec3a6506030e54ac3b970e";
    
    @Test
    public void test()
    {
        test6();
    }
    
    @SuppressWarnings("unused")
    private void test1()
    {
        List<CorrectionElement> addBeans = new ArrayList<CorrectionElement>();
        
        //singleWord
        char[] str2char = VIDEO_NAME.toCharArray();
        String singleWordStr = "";
        for (int t = 0; t < str2char.length; t++)
        {
            singleWordStr += String.valueOf(str2char[t]) + " ";
        }
        CorrectionElement singleWordEle = new CorrectionElement();
        singleWordEle.setId("1");
        singleWordEle.setSentence(singleWordStr.trim());
        singleWordEle.setSentence_original(VIDEO_NAME);
        singleWordEle.setDatabase(SINGLE_WORD_DATABASE);
        addBeans.add(singleWordEle);
        
        //word
        String[] words = SegementUtils.segementString(VIDEO_NAME);
        String wordsStr = "";
        for (int t = 0; t < words.length; t++)
        {
            wordsStr += String.valueOf(words[t]) + " ";
        }
        CorrectionElement wordEle = new CorrectionElement();
        wordEle.setId("2");
        wordEle.setSentence(wordsStr.trim());
        wordEle.setSentence_original(VIDEO_NAME);
        wordEle.setDatabase(WORD_DATABASE);
        addBeans.add(wordEle);
        
        //pinying
        String pinyins = PinyinUtils.getPinyin(VIDEO_NAME);
        String[] pinyinArray = pinyins.split(PinyinUtils.PINYIN_SPLIT);
        String pinyinStr = "";
        for (int t = 0; t < pinyinArray.length; t++)
        {
            pinyinStr += String.valueOf(pinyinArray[t]) + " ";
        }
        CorrectionElement pinyingEle = new CorrectionElement();
        pinyingEle.setId("3");
        pinyingEle.setSentence(pinyinStr.trim());
        pinyingEle.setSentence_original(VIDEO_NAME);
        pinyingEle.setDatabase(PINYING_WORD_DATABASE);
        addBeans.add(pinyingEle);
        
        //pinying2
        String pinyin2s = PinyinUtils.getPinyin2(VIDEO_NAME);
        String[] pinyin2Array = pinyin2s.split(PinyinUtils.PINYIN_SPLIT);
        String pinyin2Str = "";
        for (int t = 0; t < pinyin2Array.length; t++)
        {
            pinyin2Str += String.valueOf(pinyin2Array[t]) + " ";
        }
        CorrectionElement pinying2Ele = new CorrectionElement();
        pinying2Ele.setId("4");
        pinying2Ele.setSentence(pinyin2Str.trim());
        pinying2Ele.setSentence_original(VIDEO_NAME);
        pinying2Ele.setDatabase(PINYING2_WORD_DATABASE);
        addBeans.add(pinying2Ele);
        
        SolrUtils.addSolrData(addBeans);
    }
    
    @SuppressWarnings("unused")
    private void test2()
    {
        SolrUtils.deleteAllData();
    }
    
    @SuppressWarnings("unused")
    private void test3()
    {
        List<String> ret = SolrUtils.querySolrData(APPID, WORD_DATABASE, VIDEO_NAME_SEGEMENT);
        System.out.println(ret);
    }
    
    
    @SuppressWarnings("unused")
    private void test4()
    {
        SolrUtils.deleteAllData();
        List<CorrectionElement> list = new ArrayList<CorrectionElement>();
        int count = 0;
        for (String str : VIDEO_NAMES)
        {
            CorrectionElement element = CorrectionElementUtils.getCorrectionElement(APPID, str, String.valueOf(count), DatabaseType.WORD_SYN_DATABASE);
            list.add(element);
            count ++;
        }
        SolrUtils.addSolrData(list);
    }
    
    @SuppressWarnings("unused")
    private void test5()
    {
        String text = "可不可以把代孕母亲放给我看";
        QueryElement queryEle = QueryElementUtils.getQueryElement(APPID, text, DatabaseType.WORD_SYN_DATABASE);
        List<ResultElement> retEles = SolrUtils.querySolrData(queryEle, "sentence_original");
        System.out.println(retEles);
    }
    
    @SuppressWarnings("unused")
    private void test6()
    {
        SolrUtils.loadSynonymToSolrByFile();
    }
}
