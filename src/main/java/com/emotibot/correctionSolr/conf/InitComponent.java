package com.emotibot.correctionSolr.conf;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.emotibot.correctionSolr.utils.SolrUtils;

@Component
public class InitComponent implements ApplicationListener<ApplicationReadyEvent>
{
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event)
    {
        SolrUtils.test();
    }

}
