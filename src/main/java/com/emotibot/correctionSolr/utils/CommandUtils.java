package com.emotibot.correctionSolr.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correction.utils.EditDistanceUtils;
import com.emotibot.correction.utils.PinyinUtils;
import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.element.CommandCompareElement;
import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.utils.FileUtils;

/**
 * 记录Command转换成拼音的Map
 * 
 * 将当前的指令写入到Solr中，以供查找（之后再说）
 * 
 * 可以多线程比对
 * 
 * 创建拼音到主词的映射，去掉前后鼻音和平翘舌音，这里是String -> List，如果有list需要在后台报错
 * 
 * @author emotibot
 *
 */
public class CommandUtils
{
    private static Logger logger = Logger.getLogger(CommandUtils.class);
    private static Map<String, SentenceElement> commandMap = new HashMap<String, SentenceElement>();
    private static Map<Integer, List<String>> lengthToCommandsMap = new HashMap<Integer, List<String>>();
    private static ReentrantLock lock = new ReentrantLock();
    private static Map<String, Set<String>> pinyinToCommandsMap = new HashMap<String, Set<String>>();
    
    static
    {
        //loadSynonymCommandFromFile();
    }
    
    public static void loadSynonymCommandFromFile()
    {
        List<String> commands = FileUtils.readFile(
                ConfigManager.INSTANCE.getPropertyString(Constants.COMMAND_FILE_PATH_KEY));
        
        updateSynonymCommand(commands);
    }
    
    public static void loadSynonymCommandFromConsul(String[] lines)
    {
        lock.lock();
        try
        {
            List<String> commandsTmp = new ArrayList<String>();
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
                    commandsTmp.add(line);
                }
            }
            updateSynonymCommand(commandsTmp);
        }
        finally
        {
            lock.unlock();
        }
    }
    
    /**
     * 读取的是专有名词>长虹>其他>指令 语音关机 李音关机
     * 
     * @param commands
     */
    public static void updateSynonymCommand(List<String> lines)
    {
        Map<String, SentenceElement> commandMapTmp = new HashMap<String, SentenceElement>();
        Map<Integer, List<String>> lengthToCommandsMapTmp = new HashMap<Integer, List<String>>();
        Map<String, Set<String>> pinyinToCommandsMapTmp = new HashMap<String, Set<String>>();
        for (String line : lines)
        {
            String[] elements = line.trim().split("\t");
            //提取标准词
            String command = elements[1];
            SentenceElement element = new SentenceElement(command);
            element.addCharacterWithPinyin();
            if (element != null && !commandMapTmp.containsKey(command))
            {
                commandMapTmp.put(command, element);
                int len = command.length();
                List<String> lengthToCommandsList = lengthToCommandsMapTmp.get(len);
                if (lengthToCommandsList == null)
                {
                    lengthToCommandsList = new ArrayList<String>();
                    lengthToCommandsMapTmp.put(len, lengthToCommandsList);
                }
                lengthToCommandsList.add(command);
            }
            
            //构造同义词（包含标准词）的拼音到标准词的映射
            for (int i = 1; i < elements.length; i ++)
            {
                SentenceElement likelyElements = new SentenceElement(elements[i]);
                likelyElements.addCharacterWithPinyin();
                likelyElements.clearNasalsAndPingqiao();
                String pinyin = likelyElements.getPinyinEle().getPinyin();
                Set<String> commandSet = pinyinToCommandsMapTmp.get(pinyin);
                if (commandSet == null)
                {
                    commandSet = new HashSet<String>();
                    pinyinToCommandsMapTmp.put(pinyin, commandSet);
                }
                commandSet.add(command);
            }
        }
        commandMap = commandMapTmp;
        lengthToCommandsMap = lengthToCommandsMapTmp;
        pinyinToCommandsMap = pinyinToCommandsMapTmp;
        checkPinyinToCommandsMap();
    }
    
    public static SentenceElement getSentenceElement(String sentence)
    {
        return commandMap.get(sentence);
    }
    
    public static List<String> getPotentialCommands(String command)
    {
        List<String> potentialCommands = new ArrayList<String>();
        for(int i = command.length() - 1; i <= command.length() + 1; i ++)
        {
            if (lengthToCommandsMap.get(i) != null)
            {
                potentialCommands.addAll(lengthToCommandsMap.get(i));
            }
        }
        return potentialCommands;
    }
    
    /**
     * 之前element已经经过长度的筛选了，如果不符合条件，返回为null
     * 
     * @param target 用户输入的element
     * @param element 同义词中的指令信息
     * @return
     */
    public static CommandCompareElement getCommandCompareElement(SentenceElement target, SentenceElement command)
    {
        List<SentenceElement> targetSingleSentenceElements = target.getSingleSentenceElement();
        List<SentenceElement> commandSingleSentenceElements = command.getSingleSentenceElement();
        
        int targetIndex = 0;
        int commandIndex = 0;
        int targetLen = targetSingleSentenceElements.size();
        int commandLen = commandSingleSentenceElements.size();
        /* 音同字不同 */
        int errorCount1 = 0;
        /* 发音相近词 */
        int errorCount2 = 0;
        /* 多字母少字母错误 */
        int errorCount3 = 0;
        
        while(targetIndex < targetLen && commandIndex < commandLen)
        {
            SentenceElement targetElement = targetSingleSentenceElements.get(targetIndex);
            SentenceElement commandElement = commandSingleSentenceElements.get(commandIndex);
            
            int ret = compareCommandElement(targetElement, commandElement);
            
            //如果认为一致
            if (ret <= 1 || (ret == 2 && !isAllLetters(targetElement.getSentence()) && !isAllLetters(commandElement.getSentence())))
            {
                targetIndex ++;
                commandIndex ++;
                if (ret == 1)
                {
                    errorCount1 ++;
                }
                else if (ret == 2)
                {
                    errorCount2 ++;
                    if (errorCount2 >= Constants.ERROR_COUNT_2_THRESHOLD)
                    {
                        return null;
                    }
                    else if(commandLen < Constants.ERROR_COUNT_2_LEN_THRESHOLD)
                    {
                        return null;
                    }
                }
                continue;
            }
            
            errorCount3 ++;
            if (errorCount3 >= Constants.ERROR_COUNT_3_THRESHOLD)
            {
                return null;
            }
            /**
             * 如果不一致，有三种情况：
             * 
             * 1. target + 1 = command + 1 并且target和command是字母
             * 2. target + 1 = command 并且target是字母
             * 3. command + 1 = target 并且command是字母
             * 4. 如果两个均不为字母，说明是汉字有错误，直接返回null
             * 
             */
            if (!isAllLetters(targetElement.getSentence()) && !isAllLetters(commandElement.getSentence()))
            {
                return null;
            }
            else if (isAllLetters(targetElement.getSentence()) && targetIndex < targetLen - 1
                    && compareCommandElement(targetSingleSentenceElements.get(targetIndex + 1), commandElement) <= 2)
            {
                targetIndex ++;
                continue;
            }
            else if (isAllLetters(commandElement.getSentence()) && commandIndex < commandLen - 1
                    && compareCommandElement(commandSingleSentenceElements.get(commandIndex + 1), targetElement) <= 2)
            {
                commandIndex ++;
                continue;
            }
            else if (isAllLetters(targetElement.getSentence()) && isAllLetters(commandElement.getSentence())
                    && targetIndex < targetLen - 1 && commandIndex < commandLen - 1
                    && compareCommandElement(targetSingleSentenceElements.get(targetIndex + 1), commandSingleSentenceElements.get(commandIndex + 1)) <= 2)
            {
                targetIndex ++;
                commandIndex ++;
                continue;
            }
            
            //如果以上的代码都没有结果，就直接跳出
            break;
        }
        
        //跳出上面的循环后如果两个list还有值，那么顺序判断结果，如果均为字母，则认为是可以出错的
        while(targetIndex < targetLen && commandIndex < commandLen)
        {
            SentenceElement targetElement = targetSingleSentenceElements.get(targetIndex);
            SentenceElement commandElement = commandSingleSentenceElements.get(commandIndex);
            if (isAllLetters(targetElement.getSentence()) && isAllLetters(commandElement.getSentence()))
            {
                errorCount3 ++;
                if (errorCount3 >= Constants.ERROR_COUNT_3_THRESHOLD)
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
            targetIndex ++;
            commandIndex ++;
        }
        
        while(targetIndex < targetLen)
        {
            SentenceElement targetElement = targetSingleSentenceElements.get(targetIndex);
            if (isAllLetters(targetElement.getSentence()))
            {
                errorCount3 ++;
                if (errorCount3 >= Constants.ERROR_COUNT_3_THRESHOLD)
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
            targetIndex ++;
        }
        
        while(commandIndex < commandLen)
        {
            SentenceElement commandElement = commandSingleSentenceElements.get(commandIndex);
            if (isAllLetters(commandElement.getSentence()))
            {
                errorCount3 ++;
                if (errorCount3 >= Constants.ERROR_COUNT_3_THRESHOLD)
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
            commandIndex ++;
        }

        return new CommandCompareElement(command.getSentence(), errorCount1, errorCount2, errorCount3);
    }
    
    /**
     * 返回值：
     * 
     * 0: 一致（考虑英文字母的大小写问题）
     * 1: 音同字不同
     * 2: 近音字
     * 3: 不同
     * 
     * @param target
     * @param command
     * @return
     */
    private static int compareCommandElement(SentenceElement target, SentenceElement command)
    {
        if (target.getSentence().toUpperCase().equals(command.getSentence().toUpperCase()))
        {
            return 0;
        }
        else if (PinyinUtils.comparePinyin(target.getPinyin()[0], command.getPinyin()[0]))
        {
            return 1;
        }
        else if (PinyinUtils.comparePinyin2(target.getPinyin()[0], command.getPinyin()[0]))
        {
            return 2;
        }
        else
        {
            return 3;
        }
    }
    
    private static boolean isAllLetters(String str)
    {
        char[] charArray = str.toCharArray();
        for(int i = 0; i < charArray.length; i ++)
        {
            char ch = charArray[i];
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
            {
                continue;
            }
            return false;
        }
        return true;
    }
    
    private static void checkPinyinToCommandsMap()
    {
        for(Map.Entry<String, Set<String>> entry : pinyinToCommandsMap.entrySet())
        {
            if (entry.getValue().size() > 1)
            {
                logger.error("相同同义词发音指向不同的标准指令: " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }
    
    public static String getCommandsFromSynonym(String sentence)
    {
        SentenceElement sentenceElement = new SentenceElement(sentence);
        sentenceElement.addCharacterWithPinyin();
        sentenceElement.clearNasalsAndPingqiao();
        Set<String> commandsSet = pinyinToCommandsMap.get(sentenceElement.getPinyinEle().getPinyin());
        if (commandsSet == null || commandsSet.isEmpty())
        {
            return null;
        }
        double distance = Double.MAX_VALUE;
        SentenceElement ret = null;
        //需要计算编辑距离，求出最优结果
        for (String command : commandsSet)
        {
            SentenceElement targetElement = commandMap.get(command);
            if (sentenceElement == null)
            {
                continue;
            }
            double distanceTmp = EditDistanceUtils.getEditDistance(sentenceElement, targetElement);
            if (distanceTmp < distance)
            {
                ret = targetElement;
                distance = distanceTmp;
            }
        }
        if (ret == null)
        {
            return null;
        }
        return ret.getSentence();
    }
    
    static class MyConstants
    {
        private static String COMMAND_INFO = "专有词库>长虹>其他>指令";
        private static String[] LEVEL_INFOS = {COMMAND_INFO};
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
