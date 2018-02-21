package com.emotibot.correctionSolr.utils;

import org.junit.Test;

public class TemplateUtilsTest
{
    public static final String sentence = "我想看大话西游111集";
    
    @Test
    public void test()
    {
        System.out.println(TemplateUtils.fetchSeasonStr(sentence));
    }

}
