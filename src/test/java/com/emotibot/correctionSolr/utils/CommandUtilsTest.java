package com.emotibot.correctionSolr.utils;

import org.junit.Test;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correctionSolr.element.CommandCompareElement;

public class CommandUtilsTest
{

    String target = "你应关机";
    String command = "女婴关机";
    
    @Test
    public void test()
    {
        SentenceElement targetElement = new SentenceElement(target);
        SentenceElement commandElement = new SentenceElement(command);
        targetElement.addCharacterWithPinyin();
        commandElement.addCharacterWithPinyin();
        CommandCompareElement result = CommandUtils.getCommandCompareElement(targetElement, commandElement);
        System.out.println(result);
    }

}
