package com.emotibot.correctionSolr.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.step.CorrectionStep;
import com.emotibot.correctionSolr.utils.CorrectionUtils;
import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.utils.JsonUtils;
import com.emotibot.middleware.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 将text生成不同的QueryElement，并行调用得到结果
 * 
 * @author emotibot
 *
 */

@Service("correctionSolrService")
@EnableAutoConfiguration
@EnableConfigurationProperties
public class CorrectionSolrServiceImpl implements CorrectionSolrService
{
    private static final Logger logger = Logger.getLogger(CorrectionSolrServiceImpl.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(100);
    private CorrectionStep correctionStep = new CorrectionStep(executorService);
    
    @Override
    public String getCorrectionName(String text)
    {
        Context context = new Context();
        context.setValue(Constants.SENTENCE_KEY, text.trim());
        correctionSentence(context);
        String result = getCorrection(context);
        return result;
    }
    
    @Override
    public String getLikelyName(String text)
    {
        text = CorrectionUtils.getLikelyCorrection(text);
        return text;
    }

    private void correctionSentence(Context context)
    {
        long startTime = System.currentTimeMillis();
        correctionStep.execute(context);
        long endTime = System.currentTimeMillis();
        logger.info("correctionStep: [" + (endTime - startTime) + "]ms");
    }
    
    private String getCorrection(Context context)
    {
        String correctionNameArrStr = (String) context.getValue(Constants.CORRECTION_SENTENCE_KEY);
        if (StringUtils.isEmpty(correctionNameArrStr))
        {
            correctionNameArrStr = new JsonArray().toString();
        }
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        String oldName = CorrectionUtils.getLikelyCorrection2(sentence);
        if (StringUtils.isEmpty(oldName))
        {
            oldName = "";
        }
        JsonArray correctionNameArr = (JsonArray) JsonUtils.getObject(correctionNameArrStr, JsonArray.class);
        JsonObject retObj = new JsonObject();
        retObj.addProperty(Constants.OLD_NAME, oldName);
        retObj.add(Constants.LIKELY_NAME_ARR, correctionNameArr);
        return retObj.toString();
    }

}
