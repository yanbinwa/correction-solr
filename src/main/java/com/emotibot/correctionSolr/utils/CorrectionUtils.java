package com.emotibot.correctionSolr.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emotibot.correction.utils.SegementUtils;
import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.utils.StringUtils;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

public class CorrectionUtils
{
    
    private static final String commonSentenceTargetFilePath = ConfigManager.INSTANCE.getPropertyString(com.emotibot.correction.constants.Constants.COMMON_SENTENCE_FILE_PATH);
    @SuppressWarnings("unused")
    private static int totalCommonTokensCount = 0;
    private static Map<String, Integer> commonWordCountMap = new HashMap<String, Integer>();
    
    static 
    {
        initCommonWordCountMap();
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
                    
//                    String word = words[i];
//                    for (int j = 0; j < word.length(); j ++)
//                    {
//                        String chat = String.valueOf(word.charAt(j));
//                        int wordCount1 = commonWordCountMapTmp.get(chat) == null ? 0 : commonWordCountMapTmp.get(chat);
//                        commonWordCountMapTmp.put(chat, wordCount1 + 1);
//                        totalCommonTokensCount += 1;
//                    }
                    
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
}
