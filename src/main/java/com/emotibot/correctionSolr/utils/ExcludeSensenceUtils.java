package com.emotibot.correctionSolr.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.emotibot.middleware.utils.FileUtils;

/**
 * 判断用户输入是否为常出现的无意图说法语句，如果是，不进入纠错
 * 
 * @author emotibot
 *
 */
public class ExcludeSensenceUtils
{
    private static Set<String> excludeSentenceSet = new HashSet<String>();
    private static ReentrantLock lock = new ReentrantLock();
    
    static
    {
        loadExcludeSentenceFromFile();
    }
    
    private static void loadExcludeSentenceFromFile()
    {
        String filePath = "file/excludeSentence.txt";
        List<String> lines = FileUtils.readFile(filePath);
        if (lines == null)
        {
            return;
        }
        Set<String> excludeSentenceSetTmp = new HashSet<String>(lines);
        excludeSentenceSet = excludeSentenceSetTmp;
    }
    
    public static void updateExcludeSentence(String[] lines)
    {
        lock.lock();
        try
        {
            Set<String> excludeSentenceSetTmp = new HashSet<String>();
            for (String line : lines)
            {
                String[] elements = line.trim().split("\t");
                if (elements.length < 2)
                {
                    continue;
                }

                String levelInfo = elements[0];
                if (MyConstants.level_infos.contains(levelInfo))
                {
                    String word = elements[1];
                    excludeSentenceSetTmp.add(word.trim());
                }
            }
            excludeSentenceSet = excludeSentenceSetTmp;
        }
        finally
        {
            lock.unlock();
        }
    }
    
    public static boolean isExcludeSentence(String sentence)
    {
        return excludeSentenceSet.contains(sentence);
    }
    
    public static void test()
    {
        
    }
    
    static class MyConstants
    {
        private static String EXCLUDE_INFO = "专有词库>长虹>非同义词>纠错目录";
        private static String[] LEVEL_INFOS = {EXCLUDE_INFO};
        public static Set<String> level_infos = new HashSet<String>();
        static
        {
            for (String level_info : LEVEL_INFOS)
            {
                level_infos.add(level_info);
            }
        }
    }
}
