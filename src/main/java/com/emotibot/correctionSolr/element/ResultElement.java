package com.emotibot.correctionSolr.element;

import com.emotibot.middleware.utils.JsonUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultElement
{
    @SerializedName("score")
    @Expose
    private float score;
    
    @SerializedName("result")
    @Expose
    private String result;
    
    @SerializedName("database")
    @Expose
    private DatabaseType database;
    
    public ResultElement()
    {
        
    }
    
    public ResultElement(float score, String result)
    {
        this.score = score;
        this.result = result;
    }
    
    public ResultElement(float score, String result, DatabaseType database)
    {
        this.score = score;
        this.result = result;
        this.database = database;
    }
    
    public void setScore(float score)
    {
        this.score = score;
    }
    
    public float getScore()
    {
        return this.score;
    }
    
    public void setResult(String result)
    {
        this.result = result;
    }
    
    public String getResult()
    {
        return this.result;
    }
    
    public void setDatabase(DatabaseType database)
    {
        this.database = database;
    }
    
    public DatabaseType getDatabase()
    {
        return this.database;
    }
    
    @Override
    public String toString()
    {
        return JsonUtils.getJsonStr(this);
    }
}
