package com.emotibot.correctionSolr.common.synonym;

/**
 * 同义词服务，监听同义词更新，并将其同步到特定的服务当中
 * 
 * @author emotibot
 *
 */
public interface SynonymService
{
    public void start();
    
    public void stop();
}
