package com.emotibot.correctionSolr.element;

import org.apache.solr.client.solrj.beans.Field;

public class CorrectionElement
{
    @Field
    private String id;
    
    @Field
    private String database;
    
    @Field
    private String sentence;
    
    @Field
    private String sentence_syn;
    
    @Field
    private String sentence_original;
    
    @Field
    private String appid;
    
    public CorrectionElement()
    {
        
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getId()
    {
        return this.id;
    }
    
    public void setDatabase(String database)
    {
        this.database = database;
    }
    
    public String getDatabase()
    {
        return this.database;
    }
    
    public void setSentence(String sentence)
    {
        this.sentence = sentence;
    }
    
    public String getSentence()
    {
        return this.sentence;
    }
    
    public void setSentence_syn(String sentence_syn)
    {
        this.sentence_syn = sentence_syn;
    }
    
    public String getSentence_syn()
    {
        return this.sentence_syn;
    }
    
    public void setSentence_original(String sentence_original)
    {
        this.sentence_original = sentence_original;
    }
    
    public String getSentence_original()
    {
        return this.sentence_original;
    }
    
    public void setAppid(String appid)
    {
        this.appid = appid;
    }
    
    public String getAppid()
    {
        return this.appid;
    }
}
