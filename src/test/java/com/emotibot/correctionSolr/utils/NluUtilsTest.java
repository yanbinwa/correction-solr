package com.emotibot.correctionSolr.utils;

import java.util.List;

import org.junit.Test;

public class NluUtilsTest
{

    public static final String sentence = "我想看周星驰演的大话西游";
    
    @Test
    public void test()
    {
        String nameEntity = NluUtils.getNameEntitis(sentence);
        List<String> ret = NluUtils.fetchNameEntitiesByTag(nameEntity, TemplateUtils.NAME_ENTITY_PERSON_TAG);
        System.out.println(ret);
    }

}
