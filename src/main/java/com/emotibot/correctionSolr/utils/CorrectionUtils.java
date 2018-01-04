package com.emotibot.correctionSolr.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emotibot.correction.utils.SegementUtils;
import com.emotibot.correctionSolr.element.StopWordElement;
import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.utils.StringUtils;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

/**
 * 在提取可能片名时需要将stopword出现的位置标注出来，其前后的可能词也标注出来，如果其为开始，可以加入tag，start，如果其为结束，也可以加入tag，finished
 * 
 * 结构是
 * 
 * commonStartWordCountMap<String, Element>
 * commonEndWordCountMap<String, Element>
 * 
 * Element: 
 * 
 * Set<String> beforeSet
 * Set<String> endSet
 * 
 * 在对于用户输入的语句中
 * 
 * 记录当前词的before和end，如果是一开始，before是startTag，如果是结尾，end是stopTag，如果后面的词不在commonStartWordCountMap中，则after为end tag
 * 
 * 如果其before和end均满足，则认为其为stopword，如果不满足，退出
 * 
 * 接下来从end出发来找
 * 
 * @author emotibot
 *
 */
public class CorrectionUtils
{
    private static final String SPLIT_WORD = "XXX";
    private static final String commonSentenceTargetFilePath = ConfigManager.INSTANCE.getPropertyString(com.emotibot.correction.constants.Constants.COMMON_SENTENCE_FILE_PATH);
    @SuppressWarnings("unused")
    private static int totalCommonTokensCount = 0;
    private static Map<String, Integer> commonWordCountMap = new HashMap<String, Integer>();
    
    private static Map<String, StopWordElement> commonStartWordCountMap = new HashMap<String, StopWordElement>();
    private static Map<String, StopWordElement> commonEndWordCountMap = new HashMap<String, StopWordElement>();
    
    private static String[] pronouns = {"我", "你", "他", "她", "它"};
    
    static 
    {
        initCommonWordCountMap();
        initCommonBeginAndEndWordMap();
    }
    
    private static void initCommonWordCountMap()
    {
        if (StringUtils.isEmpty(commonSentenceTargetFilePath))
        {
            return;
        }
        BufferedReader br = null;
        try
        {
            Map<String, Integer> commonWordCountMapTmp = new HashMap<String, Integer>();
            File targetFile = new File(commonSentenceTargetFilePath);
            br = new BufferedReader(new FileReader(targetFile));
            
            String wordsline = null;
            while ((wordsline = br.readLine()) != null)
            {
                List<Term> termList = StandardTokenizer.segment(wordsline);
                
                String[] words = new String[termList.size()];
                for (int i = 0; i < termList.size(); i ++)
                {
                    words[i] = termList.get(i).word;
                }
                for (int i = 0; i < words.length; i++)
                {
                    int wordCount = commonWordCountMapTmp.get(words[i]) == null ? 0 : commonWordCountMapTmp.get(words[i]);
                    commonWordCountMapTmp.put(words[i], wordCount + 1);
                    totalCommonTokensCount += 1;
                    
                    if (words.length > 1 && i < words.length - 1)
                    {
                        StringBuffer wordStrBuf = new StringBuffer();
                        wordStrBuf.append(words[i]).append(words[i + 1]);
                        int wordStrCount = commonWordCountMapTmp.get(wordStrBuf.toString()) == null 
                                                             ? 0 : commonWordCountMapTmp.get(wordStrBuf.toString());
                        commonWordCountMapTmp.put(wordStrBuf.toString(), wordStrCount + 1);
                        totalCommonTokensCount += 1;
                    }
                }
            }
            commonWordCountMap = commonWordCountMapTmp;
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                } 
                catch (IOException e)
                {
                    //Do nothing
                }
            }
        }
    }
    
    
    /**
     * 加入token
     * 
     */
    private static void initCommonBeginAndEndWordMap()
    {
        if (StringUtils.isEmpty(commonSentenceTargetFilePath))
        {
            return;
        }
        BufferedReader br = null;
        try
        {
            Map<String, StopWordElement> commonStartWordCountMapTmp = new HashMap<String, StopWordElement>();
            Map<String, StopWordElement> commonStopWordCountMapTmp = new HashMap<String, StopWordElement>();
            File targetFile = new File(commonSentenceTargetFilePath);
            br = new BufferedReader(new FileReader(targetFile));
            String wordsline = null;
            while ((wordsline = br.readLine()) != null)
            {
                List<Term> termList = StandardTokenizer.segment(wordsline);                
                List<String> startWords = new ArrayList<String>();
                List<String> endWords = new ArrayList<String>();
                boolean isStart = true;
                for (int i = 0; i < termList.size(); i ++)
                {
                    String word = termList.get(i).word;
                    if (word.equals(SPLIT_WORD))
                    {
                        isStart = false;
                        continue;
                    }
                    if (isStart)
                    {
                        startWords.add(word);
                    }
                    else
                    {
                        endWords.add(word);
                    }
                }
                initCommonBeginAndEndWordMap(commonStartWordCountMapTmp, startWords);
                initCommonBeginAndEndWordMap(commonStopWordCountMapTmp, endWords);
            }
            commonStartWordCountMap = commonStartWordCountMapTmp;
            commonEndWordCountMap = commonStopWordCountMapTmp;
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                } 
                catch (IOException e)
                {
                    //Do nothing
                }
            }
        }
    }
    
    private static void initCommonBeginAndEndWordMap(Map<String, StopWordElement> commonWordCountMap, List<String> words)
    {
        if (words == null || words.isEmpty())
        {
            return;
        }
        if (words.size() == 1)
        {
            addBeginAndEndWordToMap(commonWordCountMap, words.get(0), StopWordElement.BEGIN_WORD, StopWordElement.END_WORD);
            return;
        }
        String beforeWord = StopWordElement.BEGIN_WORD;
        String afterWord = null;
        for (int i = 0; i < words.size(); i ++)
        {
            if (i == words.size() - 1)
            {
                afterWord = StopWordElement.END_WORD;
            }
            else
            {
                afterWord = words.get(i + 1);
            }
            String word = words.get(i);
            addBeginAndEndWordToMap(commonWordCountMap, word, beforeWord, afterWord);
            beforeWord = word;
        }
    }
    
    private static void addBeginAndEndWordToMap(Map<String, StopWordElement> commonWordCountMap, String word, String beforeWord, String afterWord)
    {
        StopWordElement element = commonWordCountMap.get(word);
        if (element == null)
        {
            element = new StopWordElement();
            element.setWord(word);
            commonWordCountMap.put(word, element);
        }
        element.addBeforeWord(beforeWord);
        element.addAfterWord(afterWord);
    }
    
    public static String getLikelyCorrection(String sentence)
    {
        String[] tokens = SegementUtils.segementString(sentence);
        if (tokens == null)
        {
            return sentence;
        }
        boolean[] tagList = new boolean[tokens.length];
        for (int i = 0; i < tagList.length; i ++)
        {
            tagList[i] = true;
        }
        for (int i = 0; i < tokens.length; i ++)
        {
            if (commonWordCountMap.containsKey(tokens[i]))
            {
                tagList[i] = false;
            }
            else
            {
                break;
            }
        }

        for (int i = tokens.length - 1; i >= 0; i --)
        {
            if (commonWordCountMap.containsKey(tokens[i]))
            {
                tagList[i] = false;
            }
            else
            {
                break;
            }
        }
        
        String ret = "";
        for (int i = 0; i < tagList.length; i ++)
        {
            if (tagList[i])
            {
                ret += tokens[i];
            }
        }
        return ret;
    }
    
    /**
     * 再做一个优化，就是如果该词在之前出现过，就认为是片名了
     * 
     * 排除可不可以，能不能，看看的case
     * 
     * @param sentence
     * @return
     */
    public static String getLikelyCorrection1(String sentence)
    {
        String[] tokens = SegementUtils.segementString(sentence);
        if (tokens == null)
        {
            return sentence;
        }
        boolean[] tagList = new boolean[tokens.length];
        for (int i = 0; i < tagList.length; i ++)
        {
            tagList[i] = true;
        }
        
        Set<String> stopWordSet = new HashSet<String>();
        for (int i = 0; i < tokens.length; i ++)
        {
            if (commonStartWordCountMap.containsKey(tokens[i]) && !stopWordSet.contains(tokens[i]))
            {
                tagList[i] = false;
                stopWordSet.add(tokens[i]);
            }
            else
            {
                break;
            }
        }

        for (int i = tokens.length - 1; i >= 0; i --)
        {
            if (commonEndWordCountMap.containsKey(tokens[i]) && !stopWordSet.contains(tokens[i]))
            {
                tagList[i] = false;
                stopWordSet.add(tokens[i]);
            }
            else
            {
                break;
            }
        }
        
        String ret = "";
        for (int i = 0; i < tagList.length; i ++)
        {
            if (tagList[i])
            {
                ret += tokens[i];
            }
        }
        return ret;
    }
    
    /**
     * 再做一个优化，就是如果该词在之前出现过，就认为是片名了
     * 
     * 排除可不可以，能不能，看看的case
     * 
     * 有些代词会对片名有影响：我的前半生，我的三妈两爸
     * 
     * 当遇到代词时，判断代词左右的词是否为常用的匹配，例如“我的”就不是模板，所以可以优化，这里只对代词处理
     * 
     * @param sentence
     * @return
     */
    public static String getLikelyCorrection2(String sentence)
    {
        String[] tokens = SegementUtils.segementString(sentence);
        if (tokens == null)
        {
            return sentence;
        }
        boolean[] tagList = new boolean[tokens.length];
        for (int i = 0; i < tagList.length; i ++)
        {
            tagList[i] = true;
        }
        
        //这里是判断语句开始的
        Set<String> stopWordSet = new HashSet<String>();
        String beforeWord = StopWordElement.BEGIN_WORD;
        String afterWord = null;
        for (int i = 0; i < tokens.length; i ++)
        {
            String word = tokens[i];
            if (isPronoun(word))
            {
                if (i == tokens.length - 1 || !commonStartWordCountMap.containsKey(tokens[i + 1]))
                {
                    afterWord = StopWordElement.END_WORD;
                }
                else
                {
                    afterWord = tokens[i + 1];
                }
                
                StopWordElement element = commonStartWordCountMap.get(word);
                if (element == null)
                {
                    break;
                }
                if (element.isBeforeWord(beforeWord) && element.isAfterWord(afterWord) && !stopWordSet.contains(word))
                {
                    tagList[i] = false;
                    beforeWord = word;
                    stopWordSet.add(word);
                }
                else
                {
                    break;
                }
            }
            else
            {
                if (commonStartWordCountMap.containsKey(tokens[i]) && !stopWordSet.contains(tokens[i]))
                {
                    tagList[i] = false;
                    stopWordSet.add(tokens[i]);
                }
                else
                {
                    break;
                }
            }
        }

        afterWord = StopWordElement.END_WORD;
        beforeWord = null;
        for (int i = tokens.length - 1; i >= 0; i --)
        {
            String word = tokens[i];
            if (isPronoun(word))
            {
                if (i == 0 || !commonEndWordCountMap.containsKey(tokens[i - 1]))
                {
                    beforeWord = StopWordElement.BEGIN_WORD;
                }
                else
                {
                    beforeWord = tokens[i - 1];
                }
                StopWordElement element = commonEndWordCountMap.get(word);
                if (element == null)
                {
                    break;
                }
                if (element.isBeforeWord(beforeWord) && element.isAfterWord(afterWord) && !stopWordSet.contains(word))
                {
                    tagList[i] = false;
                    afterWord = word;
                    stopWordSet.add(word);
                }
                else
                {
                    break;
                }
            }
            else
            {
                if (commonEndWordCountMap.containsKey(tokens[i]) && !stopWordSet.contains(tokens[i]))
                {
                    tagList[i] = false;
                    stopWordSet.add(tokens[i]);
                }
                else
                {
                    break;
                }
            }
        }
        
        String ret = "";
        for (int i = 0; i < tagList.length; i ++)
        {
            if (tagList[i])
            {
                ret += tokens[i];
            }
        }
        return ret;
    }
    
    private static boolean isPronoun(String word)
    {
        if (StringUtils.isEmpty(word)) 
        {
            return false;
        }
        for (int i = 0; i < pronouns.length; i ++)
        {
            if (pronouns[i].equals(word.trim()))
            {
                return true;
            }
        }
        return false;
    }
}
