package com.emotibot.correctionSolr.utils;

import org.junit.Test;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correctionSolr.element.CommandCompareElement;

public class CommandUtilsTest
{

    String target = "打开ACNTV";
    String command = "打开ICNTV";
    
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
