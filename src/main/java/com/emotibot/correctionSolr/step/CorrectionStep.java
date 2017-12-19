package com.emotibot.correctionSolr.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correction.utils.EditDistanceUtils;
import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.element.DatabaseType;
import com.emotibot.correctionSolr.element.QueryElement;
import com.emotibot.correctionSolr.element.ResultElement;
import com.emotibot.correctionSolr.response.MyResponseType;
import com.emotibot.correctionSolr.response.correction.CorrectionResponse;
import com.emotibot.correctionSolr.task.CorrectionTask;
import com.emotibot.correctionSolr.utils.CorrectionUtils;
import com.emotibot.correctionSolr.utils.QueryElementUtils;
import com.emotibot.correctionSolr.utils.SolrUtils;
import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.step.AbstractStep;

/**
 * 生成不同的query，分配到task中
 * 
 * @author emotibot
 *
 */
public class CorrectionStep extends AbstractStep
{
    private static final Logger logger = Logger.getLogger(CorrectionStep.class);
    
    public CorrectionStep()
    {
        
    }
    
    public CorrectionStep(ExecutorService executorService)
    {
        super(executorService);
    }
    
    @Override
    public void beforeRun(Context context)
    {
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        sentence = CorrectionUtils.getLikelyCorrection(sentence);
        if (StringUtils.isEmpty(sentence))
        {
            return;
            //sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        }
        context.setValue(Constants.SENTENCE_LIKELY_KEY, sentence);
        System.out.println(sentence);
        List<QueryElement> queryElementList = new ArrayList<QueryElement>();
        for (DatabaseType type : SolrUtils.databaseType)
        {
            QueryElement queryElement = QueryElementUtils.getQueryElement(sentence, type);
            if (queryElement != null)
            {
                queryElementList.add(queryElement);
            }
        }
        for (QueryElement queryElement : queryElementList)
        {
            CorrectionTask task = new CorrectionTask(queryElement);
            this.addTask(context, task);
        }
    }

    @Override
    public void afterRun(Context context)
    {
        long start = System.currentTimeMillis();
        List<Response> responseList = this.getOutputMap(context).get(MyResponseType.CORRECTION);
        if (responseList == null)
        {
            return;
        }
        Map<String, ResultElement> resultElementMap = new HashMap<String, ResultElement>(); 
        for (Response response : responseList)
        {
            if (response == null)
            {
                continue;
            }
            CorrectionResponse correctionResponse = (CorrectionResponse) response;
            List<ResultElement> resultEles = correctionResponse.getResultElement();
            if (resultEles == null)
            {
                continue;
            }
            for (ResultElement resultEle : resultEles)
            {
                if (resultElementMap.containsKey(resultEle.getResult()))
                {
                    ResultElement oldEle = resultElementMap.get(resultEle.getResult());
                    if (resultEle.getScore() > oldEle.getScore())
                    {
                        resultElementMap.put(resultEle.getResult(), resultEle);
                    }
                }
                else
                {
                    resultElementMap.put(resultEle.getResult(), resultEle);
                }
            }
        }
        if (resultElementMap.isEmpty())
        {
            return;
        }
        List<ResultElement> ret = new ArrayList<ResultElement>(resultElementMap.values());
        ret = sortElement(ret);
        ret = getPotentialResult(ret);
        String sentence = (String) context.getValue(Constants.SENTENCE_LIKELY_KEY);
        ret = getAdjustResult(ret, sentence);
        ret = sortElement(ret);
        if (ret.isEmpty())
        {
            return;
        }
        //context.setValue(Constants.CORRECTION_SENTENCE_KEY, ret.get(0).getResult());
        List<String> output = new ArrayList<String>();
        for (ResultElement element : ret)
        {
            output.add(element.getResult());
        }
        logger.debug("Sorted list: " + ret);
        context.setValue(Constants.CORRECTION_SENTENCE_KEY, output);
        long end = System.currentTimeMillis();
        System.out.println("cost: [" + (end - start) + "]ms");
    }

    private List<ResultElement> getPotentialResult(List<ResultElement> resultElements)
    {
        if (resultElements.size() <= 0)
        {
            return resultElements;
        }
        if (resultElements.get(0).getScore() < Constants.SCORE_THRESHOLD)
        {
            return new ArrayList<ResultElement>();
        }
        float scoreThreshold = Math.max(resultElements.get(0).getScore() / Constants.SCORE_THRESHOLD_RATE, Constants.SCORE_THRESHOLD);
        float scoreDiff = Float.MAX_VALUE;
        List<ResultElement> potentialResult = new ArrayList<ResultElement>();
        potentialResult.add(resultElements.get(0));
        for (int i = 1; i < resultElements.size(); i ++)
        {
            float scoreDiffTmp = resultElements.get(i - 1).getScore() - resultElements.get(i).getScore();
            if (resultElements.get(i).getScore() < scoreThreshold || scoreDiffTmp > scoreDiff)
            {
                break;
            }
            scoreDiff = scoreDiffTmp;
            potentialResult.add(resultElements.get(i));
        }
        if (potentialResult.size() > Constants.RECOMMEND_NUM)
        {
            potentialResult = potentialResult.subList(0, Constants.RECOMMEND_NUM);
        }
        return potentialResult;
    }
    
    /**
     * 通过编辑距离来进行调节
     * 
     * @param resultElements
     * @return
     */
    private List<ResultElement> getAdjustResult(List<ResultElement> resultElements, String sentence)
    {
        SentenceElement targetElement = new SentenceElement(sentence);
        for (ResultElement retElement : resultElements)
        {
            SentenceElement element = new SentenceElement(retElement.getResult());
            float distance = (float)EditDistanceUtils.getEditDistance(element, targetElement);
            retElement.setScore(retElement.getScore() - distance * Constants.DISTANCE_RATE);
        }
        return resultElements;
    }
    
    private List<ResultElement> sortElement(List<ResultElement> result)
    {
        Collections.sort(result, new Comparator<ResultElement>() {

            @Override
            public int compare(ResultElement o1, ResultElement o2)
            {
                if (o1.getScore() > o2.getScore())
                {
                    return -1;
                }
                else if (o1.getScore() < o2.getScore())
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
            
        });
        return result;
    }
}
