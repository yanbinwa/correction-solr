package com.emotibot.correctionSolr.common.synonym;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.emotibot.middleware.utils.FileUtils;

public class BuildDictTest
{

    public static final String VIDEO_PREFIX = "video" + "\t";
    public static final String MUSIC_PREFIX = "music" + "\t";
    public static final String VIDEO_FILE = "/Users/emotibot/Documents/workspace/other/correction-standard/file/video.txt";
    public static final String MUSIC_FILE = "/Users/emotibot/Documents/workspace/other/correction-standard/file/music.txt";
    public static final String TARGET_FILE = "/Users/emotibot/Documents/workspace/other/correction-standard/file/dictionary.txt";
    
    @Test
    public void test()
    {
        List<String> allNames = new ArrayList<String>();
        List<String> videoNames = FileUtils.readFile(VIDEO_FILE);
        for (String name : videoNames)
        {
            allNames.add(VIDEO_PREFIX + name);
        }
        List<String> musicNames = FileUtils.readFile(MUSIC_FILE);
        for (String name : musicNames)
        {
            allNames.add(MUSIC_PREFIX + name);
        }
        FileUtils.storeFile(allNames, TARGET_FILE);
    }

}
