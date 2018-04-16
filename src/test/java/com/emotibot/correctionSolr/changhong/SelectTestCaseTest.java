package com.emotibot.correctionSolr.changhong;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.emotibot.middleware.utils.FileUtils;

/**
 * 选取合适的测试用例，保证片名位于词库当中
 * 
 * @author emotibot
 *
 */
public class SelectTestCaseTest
{
    public static final String ORIGINAL_TEST_CASE_FILE = "file/自动纠错6.csv";
    public static final String VIDEO_NAME_FILE = "file/videoName.txt";
    public static final String NEW_TEST_CASE_FILE = "file/自动纠错7.csv";
    
    @Test
    public void test()
    {
        List<String> videoNames = FileUtils.readFile(VIDEO_NAME_FILE);
        Set<String> videoNameSet = new HashSet<String>(videoNames);
        List<List<String>> contents = FileUtils.readFileFromCsv(ORIGINAL_TEST_CASE_FILE);
        List<List<String>> newContents = new ArrayList<List<String>>();
        for (List<String> content : contents)
        {
            if (videoNameSet.contains(content.get(0)))
            {
                newContents.add(content);
            }
        }
        FileUtils.writeFileToCsv(NEW_TEST_CASE_FILE, newContents);
    }

}
