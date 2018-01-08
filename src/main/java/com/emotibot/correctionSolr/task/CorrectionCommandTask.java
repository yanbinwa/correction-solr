package com.emotibot.correctionSolr.task;

import java.util.ArrayList;
import java.util.List;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correctionSolr.element.CommandCompareElement;
import com.emotibot.correctionSolr.response.correctionCommand.CorrectionCommandResponse;
import com.emotibot.correctionSolr.utils.CommandUtils;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.task.AbstractTask;

public class CorrectionCommandTask extends AbstractTask
{

    private List<SentenceElement> commandElements;
    private SentenceElement targetElement;
    
    public CorrectionCommandTask()
    {
        
    }
    
    public CorrectionCommandTask(List<SentenceElement> commandElements, SentenceElement targetElement)
    {
        this.commandElements = commandElements;
        this.targetElement = targetElement;
    }
    
    @Override
    public Response call() throws Exception
    {
        List<CommandCompareElement> ret = new ArrayList<CommandCompareElement>();
        for (SentenceElement commandElement : commandElements)
        {
            CommandCompareElement compareElement = 
                    CommandUtils.getCommandCompareElement(targetElement, commandElement);
            if (compareElement != null)
            {
                ret.add(compareElement);
            }
        }
        return new CorrectionCommandResponse(ret);
    }

}
