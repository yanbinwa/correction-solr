package com.emotibot.correctionSolr.service;

public interface CorrectionSolrService
{
    public String getCorrectionName(String text);
    
    public String getLikelyName(String text);
}
