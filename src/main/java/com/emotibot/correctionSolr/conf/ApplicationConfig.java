package com.emotibot.correctionSolr.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.emotibot.correctionSolr.common.synonym.SynonymService;
import com.emotibot.correctionSolr.common.synonym.SynonymServiceImpl;

@Configuration
public class ApplicationConfig
{
    
    @Bean(name="synonymService")
    public SynonymService synonymService()
    {
        return new SynonymServiceImpl();
    }
}
