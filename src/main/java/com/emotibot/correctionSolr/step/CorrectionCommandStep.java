package com.emotibot.correctionSolr.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.util.StringUtils;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correctionSolr.constants.Constants;
import com.emotibot.correctionSolr.element.CommandCompareElement;
import com.emotibot.correctionSolr.response.MyResponseType;
import com.emotibot.correctionSolr.response.correctionCommand.CorrectionCommandResponse;
import com.emotibot.correctionSolr.task.CorrectionCommandTask;
import com.emotibot.correctionSolr.utils.CommandUtils;
import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.step.AbstractStep;
import com.google.gson.JsonArray;

public class CorrectionCommandStep extends AbstractStep
{

    public CorrectionCommandStep()
    {
        
    }
    
    public CorrectionCommandStep(ExecutorService executorService)
    {
        super(executorService);
    }
    
    @Override
    public void beforeRun(Context context)
    {
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        if (StringUtils.isEmpty(sentence))
        {
            return;
        }
        List<String> potentialCommands = CommandUtils.getPotentialCommands(sentence);
        if (potentialCommands.isEmpty())
        {
            return;
        }
        List<SentenceElement> potentialCommandElements = new ArrayList<SentenceElement>();
        for (String potentialCommand : potentialCommands)
        {
            potentialCommandElements.add(CommandUtils.getSentenceElement(potentialCommand));
        }
        int threadNum = Constants.COMMOND_THREAD_NUM;
        if (threadNum > potentialCommandElements.size())
        {
            threadNum = potentialCommandElements.size();
        }
        List<List<SentenceElement>> backets = new ArrayList<List<SentenceElement>>();
        for (int i = 0; i < threadNum; i ++)
        {
            backets.add(new ArrayList<SentenceElement>());
        }
        int count = 0;
        for (SentenceElement element : potentialCommandElements)
        {
            backets.get(count % threadNum).add(element);
            count ++;
        }
        SentenceElement targetElement = new SentenceElement(sentence);
        targetElement.addCharacterWithPinyin();
        //需要进行调整，将字母的拼音也写入
        for (int i = 0; i < threadNum; i ++)
        {
            CorrectionCommandTask task = new CorrectionCommandTask(backets.get(i), targetElement);
            this.addTask(context, task);
        }
    }

    @Override
    public void afterRun(Context context)
    {
        long start = System.currentTimeMillis();
        List<Response> responseList = this.getOutputMap(context).get(MyResponseType.CORRECTION_COMMAND);
        if (responseList == null)
        {
            return;
        }
        List<CommandCompareElement> resultElements = new ArrayList<CommandCompareElement>();
        for (Response response : responseList)
        {
            if (response == null)
            {
                continue;
            }
            CorrectionCommandResponse correctionCommandResponse = (CorrectionCommandResponse) response;
            List<CommandCompareElement> resultEles = correctionCommandResponse.getCommandCompareElements();
            if (resultEles == null)
            {
                continue;
            }
            resultElements.addAll(resultEles);
        }
        if (resultElements.isEmpty())
        {
            return;
        }
        Collections.sort(resultElements);
        System.out.println(resultElements);
        JsonArray output = new JsonArray();
        output.add(resultElements.get(0).getSentence());
        context.setValue(Constants.CORRECTION_COMMAND_SENTENCE_KEY, output.toString());
        long end = System.currentTimeMillis();
        System.out.println("cost: [" + (end - start) + "]ms");
    }

}
