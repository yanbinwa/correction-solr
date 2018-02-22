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
import com.emotibot.correctionSolr.utils.QueryElementUtils;
import com.emotibot.correctionSolr.utils.SolrUtils;
import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.step.AbstractStep;
import com.google.gson.JsonArray;

/**
 * 生成不同的query，分配到task中
 * 
 * @author emotibot
 *
 */
public class CorrectionStep extends AbstractStep
{
    private static final Logger logger = Logger.getLogger(CorrectionStep.class);
    private static boolean enableHomonym = ConfigManager.INSTANCE.getPropertyBoolean(Constants.ENABLE_HOMONYM_CORRECTION_KEY);
    private static boolean enableRecommend = ConfigManager.INSTANCE.getPropertyBoolean(Constants.ENABLE_RECOMMEND_KEY);
    private static int maxRecommendNum = ConfigManager.INSTANCE.getPropertyInt(Constants.MAX_RECOMMEND_NUM_KEY);
    private static boolean enableInvertOrder = ConfigManager.INSTANCE.getPropertyBoolean(Constants.ENABLE_INVERT_ORDER_CORRECTION_KEY);
    
    public CorrectionStep()
    {
        
    }
    
    public CorrectionStep(ExecutorService executorService)
    {
        super(executorService);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void beforeRun(Context context)
    {
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        String appid = (String) context.getValue(Constants.APPID_KEY);
        List<String> fields = (List<String>) context.getValue(Constants.FIELD_KEY);
        if (StringUtils.isEmpty(sentence) || StringUtils.isEmpty(appid))
        {
            return;
        }
        context.setValue(Constants.SENTENCE_LIKELY_KEY, sentence);
        List<QueryElement> queryElementList = new ArrayList<QueryElement>();
        for (DatabaseType type : SolrUtils.databaseType)
        {
            QueryElement queryElement = QueryElementUtils.getQueryElement(appid, fields, sentence, type);
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
                    else if (resultEle.getScore() == oldEle.getScore() && resultEle.getDatabase() != DatabaseType.WORD_SYN_DATABASE)
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
        logger.debug("Sorted list: " + ret);
        ret = getPotentialResult(ret);
        ret = adjustSolrScore(ret);
        String sentence = (String) context.getValue(Constants.SENTENCE_LIKELY_KEY);
        ret = getAdjustResult(ret, sentence);
        logger.debug("Output list: " + ret);
        if (ret == null || ret.isEmpty())
        {
            return;
        }
        JsonArray output = new JsonArray();
        for (ResultElement element : ret)
        {
            output.add(element.getResult());
        }
        context.setValue(Constants.CORRECTION_SENTENCE_KEY, output.toString());
        long end = System.currentTimeMillis();
        System.out.println("cost: [" + (end - start) + "]ms");
    }

    /**
     * 通过solr的分数进行梯度下降
     * 
     * @param resultElements
     * @return
     */
    private List<ResultElement> getPotentialResult(List<ResultElement> resultElements)
    {
        if (resultElements.size() <= 0)
        {
            return resultElements;
        }
        if (resultElements.get(0).getScore() < Constants.SCORE_THRESHOLD_PROTENTIAL)
        {
            return new ArrayList<ResultElement>();
        }
        float scoreThreshold = Math.max(resultElements.get(0).getScore() / Constants.SCORE_THRESHOLD_RATE_PROTENTIAL, Constants.SCORE_THRESHOLD_PROTENTIAL);
        float scoreDiff = Constants.SCORE_THRESHOLD_DIFF_PROTENTIAL;
        List<ResultElement> potentialResult = new ArrayList<ResultElement>();
        potentialResult.add(resultElements.get(0));
        for (int i = 1; i < resultElements.size(); i ++)
        {
            float scoreDiffTmp = resultElements.get(i - 1).getScore() - resultElements.get(i).getScore();
            if (((resultElements.get(i).getScore() < scoreThreshold || scoreDiffTmp > scoreDiff) && scoreDiffTmp > Constants.SCORE_THRESHOLD_DIFF_PROTENTIAL) || i >= Constants.POTENTIAL_NUM)
            {
                break;
            }
            scoreDiff = scoreDiffTmp;
            potentialResult.add(resultElements.get(i));
        }
        if (potentialResult.size() > Constants.POTENTIAL_NUM)
        {
            potentialResult = potentialResult.subList(0, Constants.POTENTIAL_NUM);
        }
        return potentialResult;
    }
    
    /**
     * 当输入"啦啦啦啦啦啦"，solr结果返回"啦啦队之舞"，并且分数15.5，由于返回分数过高，导致后续的判断无法生效
     * 所以当solr返回的分数过高时，将其调整到适当的范围
     * 
     * @param resultElements
     * @return
     */
    private List<ResultElement> adjustSolrScore(List<ResultElement> resultElements)
    {
        for (ResultElement resultElement : resultElements)
        {
            if (resultElement.getScore() > Constants.SOLR_MAX_SCORE)
            {
                resultElement.setScore(Constants.SOLR_MAX_SCORE);
            }
        }
        return resultElements;
    }
    
    /**
     * 通过编辑距离来进行调节
     * 
     * 需要降低召回率，所以要进行细化
     * 
     * 1. 判断所有元素是否全部匹配（不按顺序）但需要考虑拼音，或者是否目标元素完全包含，求出差异结果（颠倒）
     * 2. 通过编辑距离计算结果（相似度）
     * 3. 是否通过同义词库得到的结果（）
     * 
     * 从三方面得到的结果与之前的结果进行组合后得到最终结果
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
            float adjustScore;
            if (enableInvertOrder)
            {
                float distanceWithoutOrder = getDistanceWithoutOrder(element, targetElement, enableHomonym) * Constants.DISTANCE_WITHOUT_ORDER_RATE;
                float distance = (float) EditDistanceUtils.getEditDistance(element, targetElement, enableHomonym) * Constants.DISTANCE_RATE;
                adjustScore = retElement.getScore() - distanceWithoutOrder - distance;
            }
            else
            {
                float distance = (float) EditDistanceUtils.getEditDistance(element, targetElement, enableHomonym) * (Constants.DISTANCE_RATE + Constants.DISTANCE_WITHOUT_ORDER_RATE);
                adjustScore = retElement.getScore() - distance;
            }
            //当通过同义词的database中得到的结果，并且长度大于3时，增加分数，这是因为同义词在求编辑距离时会扣分
            if (retElement.getDatabase().equals(DatabaseType.WORD_SYN_DATABASE) || retElement.getResult().length() >= 3)
            {
                adjustScore += Constants.DISTANCE_SYN_SCORE;
            }
            retElement.setScore(adjustScore);
        }
        resultElements = sortElement(resultElements);
        logger.info("AdjustResult: " + resultElements);
        
        //通过调整后的分数，再进行一次梯度下降
        float scoreThreshold = getScoreThreshold();
        if (resultElements.isEmpty() || resultElements.get(0).getScore() < scoreThreshold)
        {
            return null;
        }
        scoreThreshold = Math.max(resultElements.get(0).getScore() / Constants.SCORE_THRESHOLD_RATE_RECOMMEND, scoreThreshold);
        float scoreDiff = Constants.SCORE_THRESHOLD_DIFF_RECOMMEND;
        List<ResultElement> potentialResult = new ArrayList<ResultElement>();
        potentialResult.add(resultElements.get(0));
        for (int i = 1; i < resultElements.size(); i ++)
        {
            float scoreDiffTmp = resultElements.get(i - 1).getScore() - resultElements.get(i).getScore();
            
            if ((scoreDiffTmp > scoreDiff && scoreDiffTmp > Constants.SCORE_THRESHOLD_DIFF_RECOMMEND) ||
                    resultElements.get(i).getScore() < scoreThreshold)
            {
                break;
            }
            scoreDiff = scoreDiffTmp;
            potentialResult.add(resultElements.get(i));
        }
        potentialResult = getChooseResult(potentialResult);
        return potentialResult;
    }
    
    /**
     * 1. 判断所有元素是否全部匹配（不按顺序）但需要考虑拼音，或者是否目标元素完全包含，求出差异结果（颠倒）
     * 
     * 如果target完全包含在片命中，需要判断词语的先后顺序
     * 
     * 小理玩具 -> 查理的玩具小屋
     * 5 1 3   ->  命中了三个部分，而且相差较多时，认为是不存在的
     * 
     * 
     * @param element
     * @param targetElement
     * @param tag 是否考虑同音词
     * @return
     */
    private float getDistanceWithoutOrder(SentenceElement element, SentenceElement targetElement, boolean tag)
    {
        int maxLen = Math.max(element.getLength(), targetElement.getLength());
        int minLen = Math.min(element.getLength(), targetElement.getLength());
        float distance1 = (float) EditDistanceUtils.getEditDistanceWithoutOrder(element, targetElement, tag);
        if (distance1 == 0)
        {
            return Constants.DISTANCE_TOTAL_MATCH_RATE * maxLen;
        }
        //target完全包含在片命中，例如我想看小美好
        else if (distance1 <= (maxLen - minLen) && targetElement.getLength() == minLen && minLen >= 3)
        {
            int matchParter = EditDistanceUtils.getMatchParterWithoutOrder(targetElement, element, tag);
            if (matchParter <= 2)
            {
                return 0.0f;
            }
            else if ((maxLen - minLen) <= 2)
            {
                return 0.0f;
            }
            else
            {
                return distance1 * 0.8f + (maxLen - minLen) * 1.2f;
            }
        }
        else
        {
            return distance1 * 0.8f + (maxLen - minLen) * 1.2f;
        }
    }
    
    /**
     * 当推荐的数据中有得分一致的，或者得分非常相近的，可以随机输出
     * 
     * 这里根据配置来输出推荐的词条
     * 
     * @param potentialResult
     * @return
     */
    private List<ResultElement> getChooseResult(List<ResultElement> potentialResult)
    {
        List<ResultElement> ret = new ArrayList<ResultElement>();
        List<ResultElement> tmp = new ArrayList<ResultElement>();
        float lastScore = potentialResult.get(0).getScore();
        tmp.add(potentialResult.get(0));
        int recommendNum;
        if (enableRecommend)
        {
            recommendNum = maxRecommendNum;
        }
        else
        {
            recommendNum = 1;
        }
        
        for (int i = 1; i < potentialResult.size(); i ++)
        {
            ResultElement ele = potentialResult.get(i);
            if (i >= recommendNum && (lastScore - ele.getScore()) > Constants.SCORE_THRESHOLD_DIFF_CHOOSE)
            {
                Collections.shuffle(tmp);
                ret.addAll(tmp);
                break;
            }
            else if ((lastScore - ele.getScore()) > Constants.SCORE_THRESHOLD_DIFF_CHOOSE)
            {
                Collections.shuffle(tmp);
                ret.addAll(tmp);
                tmp.clear();
            }
            tmp.add(ele);
            lastScore = ele.getScore();
        }
        Collections.shuffle(tmp);
        ret.addAll(tmp);

        if (ret.size() > recommendNum)
        {
            ret = ret.subList(0, recommendNum);
        }
        return ret;
    }
    
    private float getScoreThreshold()
    {
        int scoreThresholdLevel = ConfigManager.INSTANCE.getPropertyInt(Constants.CORRECTION_THRESHOLD_LEVEL_KEY);
        switch(scoreThresholdLevel)
        {
        case 0:
            return Constants.SCORE_THRESHOLD_RECOMMEND_0;
        case 1:
            return Constants.SCORE_THRESHOLD_RECOMMEND_1;
        case 2:
            return Constants.SCORE_THRESHOLD_RECOMMEND_2;
        default:
            logger.error("unsupport score level");
            return Float.MAX_VALUE;
        }
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
