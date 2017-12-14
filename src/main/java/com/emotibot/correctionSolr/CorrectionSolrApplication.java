package com.emotibot.correctionSolr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties
public class CorrectionSolrApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(CorrectionSolrApplication.class, args);
    }
}
