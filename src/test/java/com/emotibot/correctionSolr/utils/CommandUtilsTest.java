package com.emotibot.correctionSolr.utils;

import org.junit.Test;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correctionSolr.element.CommandCompareElement;

public class CommandUtilsTest
{

    String target = "乐视还求购物";
    String command = "阅视环球购物";
    
    @Test
    public void test()
    {
        SentenceElement targetElement = new SentenceElement(target);
        SentenceElement commandElement = new SentenceElement(command);
        CommandCompareElement result = CommandUtils.getCommandCompareElement(targetElement, commandElement);
        System.out.println(result);
    }

}
