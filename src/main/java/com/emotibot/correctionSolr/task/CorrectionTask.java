package com.emotibot.correctionSolr.task;

import java.util.List;

import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.element.QueryElement;
import com.emotibot.correctionSolr.element.ResultElement;
import com.emotibot.correctionSolr.response.correction.CorrectionResponse;
import com.emotibot.correctionSolr.utils.SolrUtils;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.task.AbstractTask;

public class CorrectionTask extends AbstractTask
{

    private QueryElement query;
    
    public CorrectionTask(QueryElement query)
    {
        this.query = query;
    }
    
    @Override
    public Response call() throws Exception
    {
        if (query == null)
        {
            return null;
        }
        List<ResultElement> result = SolrUtils.querySolrData(query, "sentence_original");
        //result = adjustResultElement(result);
        return new CorrectionResponse(result);
    }
    
    /**
     * 当同义词返回的结果分数较高时，可以再进行调高，提高同义词的权重，这里不理想
     */
    @SuppressWarnings("unused")
    private List<ResultElement> adjustResultElement(List<ResultElement> result)
    {
        switch(query.getDatabase())
        {
        case WORD_SYN_DATABASE:
            return adjustResultElementForWordSynDatabase(result);
        default:
            return result;
        }
    }
    
    private List<ResultElement> adjustResultElementForWordSynDatabase(List<ResultElement> result)
    {
        for (ResultElement element : result)
        {
            if (element.getScore() > Constants.SYN_WORD_ADJUST_THRESHOLD)
            {
                float adjustScore = (element.getScore() - Constants.SYN_WORD_ADJUST_THRESHOLD) * Constants.SYN_WORD_ADJUST_RATE;
                element.setScore(element.getScore() + adjustScore);
            }
        }
        return result;
    }
    
}
