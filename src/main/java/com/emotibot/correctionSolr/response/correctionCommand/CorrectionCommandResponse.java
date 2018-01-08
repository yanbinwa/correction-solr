package com.emotibot.correctionSolr.response.correctionCommand;

import java.util.List;

import com.emotibot.correctionSolr.element.CommandCompareElement;
import com.emotibot.correctionSolr.response.MyResponseType;
import com.emotibot.middleware.response.AbstractResponse;

public class CorrectionCommandResponse extends AbstractResponse
{

    List<CommandCompareElement> commandCompareElements;
    
    public CorrectionCommandResponse()
    {
        super(MyResponseType.CORRECTION_COMMAND);
    }

    public CorrectionCommandResponse(List<CommandCompareElement> commandCompareElements)
    {
        super(MyResponseType.CORRECTION_COMMAND);
        this.commandCompareElements = commandCompareElements;
    }
    
    public List<CommandCompareElement> getCommandCompareElements()
    {
        return this.commandCompareElements;
    }
}
