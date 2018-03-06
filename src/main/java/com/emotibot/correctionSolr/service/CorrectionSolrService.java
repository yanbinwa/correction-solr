package com.emotibot.correctionSolr.service;

public interface CorrectionSolrService
{
    public String getCorrectionName(String appid, String text, String fields);
    
    public String getCorrectionVideoName(String appid, String text);
    
    public String getCorrectionMusicName(String appid, String text);
}
