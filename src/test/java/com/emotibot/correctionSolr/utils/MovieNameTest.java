package com.emotibot.correctionSolr.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.emotibot.middleware.utils.FileUtils;

public class MovieNameTest
{

    public static final String MOVIE_FILE = "/Users/emotibot/Documents/workspace/other/correction-solr/file/correction.txt";
    public static final String TARGET_FILE = "/Users/emotibot/Documents/workspace/other/correction-solr/file/correction1.txt";
    public static final String DORP_PATTERN = "^\\d{6,}.*";
    
    
    @Test
    public void test()
    {
        test2();
    }
    
    @SuppressWarnings("unused")
    private void test1()
    {
        List<String> movieList = FileUtils.readFile(MOVIE_FILE);
        Set<String> movieSet = new HashSet<String>(movieList);
        movieList = new ArrayList<String>();
        for (String str : movieSet)
        {
            if (isMarched(DORP_PATTERN, str))
            {
                System.out.println(str);
            }
            else
            {
                movieList.add(str);
            }
        }
        Collections.sort(movieList);
        FileUtils.storeFile(movieList, TARGET_FILE);
    }
    
    @SuppressWarnings("unused")
    private static void test2()
    {
        String pattern = ".*主演是_Actor_$";
        String text = "我想看主演是_Actor_的";
        if (isMarched(pattern, text))
        {
            System.out.println("true");
        }
        else
        {
            System.out.println("false");
        }
    }
    
    private static boolean isMarched(String regex, String text)
    {
        regex = "^" + regex + "$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }
}
