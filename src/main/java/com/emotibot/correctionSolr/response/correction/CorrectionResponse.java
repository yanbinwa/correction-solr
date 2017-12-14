package com.emotibot.correctionSolr.response.correction;

import java.util.List;

import com.emotibot.correctionSolr.element.ResultElement;
import com.emotibot.correctionSolr.response.MyResponseType;
import com.emotibot.middleware.response.AbstractResponse;

public class CorrectionResponse extends AbstractResponse
{
    List<ResultElement> resultElement;
    
    public CorrectionResponse()
    {
        super(MyResponseType.CORRECTION);
    }
    
    public CorrectionResponse(List<ResultElement> resultElement)
    {
        super(MyResponseType.CORRECTION);
        this.resultElement = resultElement;
    }
    
    public List<ResultElement> getResultElement()
    {
        return this.resultElement;
    }
}
