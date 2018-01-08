package com.emotibot.correctionSolr.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.step.CorrectionCommandStep;
import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.utils.StringUtils;
import com.google.gson.JsonArray;

/**
 * 纠错的指令位于同义词的专有词库->长虹->其他->指令目录，需要与consul同步
 * 
 * 1. 同音 (6/7/14/15)，需要处理多音字
 * 2. 翘舌音、前后鼻音、常见读音错误(如L/N, H/F)，case 1/4/5/13
 * 3. 多一字母，少一字母，错一字母，case 8/9/10
 * 4. 其它相近发音规则，如韵母相同，声母不同，错一字，且字数超过4个，case 11
 * 
 * @author yanbinwang@emotibot.com
 *
 */

@Service("correctionCommandService")
@EnableAutoConfiguration
@EnableConfigurationProperties
public class CorrectionCommandServiceImpl implements CorrectionCommandService
{

    private static final Logger logger = Logger.getLogger(CorrectionCommandServiceImpl.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(100);
    private CorrectionCommandStep correctionCommandStep = new CorrectionCommandStep(executorService);
    
    @Override
    public String getCorrectionCommand(String text)
    {
        Context context = new Context();
        context.setValue(Constants.SENTENCE_KEY, text.trim());
        getCorrectionCommand(context);
        String result = getCorrection(context);
        return result;
    }
    
    private void getCorrectionCommand(Context context)
    {
        long startTime = System.currentTimeMillis();
        correctionCommandStep.execute(context);
        long endTime = System.currentTimeMillis();
        logger.info("correctionStep: [" + (endTime - startTime) + "]ms");
    }
    
    private String getCorrection(Context context)
    {
        String ret = (String) context.getValue(Constants.CORRECTION_COMMAND_SENTENCE_KEY);
        if (StringUtils.isEmpty(ret))
        {
            ret = new JsonArray().toString();
        }
        return ret;
    }
}
