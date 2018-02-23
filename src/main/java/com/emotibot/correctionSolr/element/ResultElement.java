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
    
    @SerializedName("field")
    @Expose
    private String field;
    
    public ResultElement()
    {
        
    }
    
    public ResultElement(float score, String result)
    {
        this.score = score;
        this.result = result;
    }
    
    public ResultElement(float score, String result, DatabaseType database, String field)
    {
        this.score = score;
        this.result = result;
        this.database = database;
        this.field = field;
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
    
    public void setField(String field)
    {
        this.field = field;
    }
    
    public String getField()
    {
        return this.field;
    }
    
    @Override
    public String toString()
    {
        return JsonUtils.getJsonStr(this);
    }
}
