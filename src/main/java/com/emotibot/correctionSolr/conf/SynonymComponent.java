package com.emotibot.correctionSolr.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.emotibot.correctionSolr.common.synonym.SynonymService;


@Component
public class SynonymComponent implements ApplicationListener<ApplicationReadyEvent>, ExitCodeGenerator
{
    @Autowired
    SynonymService synonymService;

    @Override
    public int getExitCode()
    {
        synonymService.stop();
        return 0;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent arg0)
    {
        synonymService.start();
    }
    
}
