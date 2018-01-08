package com.emotibot.correctionSolr.element;

import com.emotibot.middleware.utils.JsonUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CommandCompareElement implements Comparable<CommandCompareElement>  
{
    @SerializedName("sentence")
    @Expose
    private String sentence;
    
    /* 音同字不同 */
    @SerializedName("errorCount1")
    @Expose
    private int errorCount1;
    
    /* 发音相近*/
    @SerializedName("errorCount2")
    @Expose
    private int errorCount2;
    
    /* 多字母或少字母*/
    @SerializedName("errorCount3")
    @Expose
    private int errorCount3;
    
    public CommandCompareElement()
    {
        
    }
    
    public CommandCompareElement(String sentence, int errorCount1, int errorCount2, int errorCount3)
    {
        this.sentence = sentence;
        this.errorCount1 = errorCount1;
        this.errorCount2 = errorCount2;
        this.errorCount3 = errorCount3;
    }
    
    public void setSentence(String sentence)
    {
        this.sentence = sentence;
    }
    
    public String getSentence()
    {
        return this.sentence;
    }
    
    public void setErrorCount1(int errorCount1)
    {
        this.errorCount1 = errorCount1;
    }
    
    public int getErrorCount1()
    {
        return this.errorCount1;
    }
    
    public void setErrorCount2(int errorCount2)
    {
        this.errorCount2 = errorCount2;
    }
    
    public int getErrorCount2()
    {
        return this.errorCount2;
    }
    
    public void setErrorCount3(int errorCount3)
    {
        this.errorCount3 = errorCount3;
    }
    
    public int getErrorCount3()
    {
        return this.errorCount3;
    }

    @Override
    public int compareTo(CommandCompareElement other)
    {
        if (errorCount1 > other.errorCount1)
        {
            return 1;
        }
        else if (errorCount1 < other.errorCount1)
        {
            return -1;
        }
        
        if (errorCount2 > other.errorCount2)
        {
            return 1;
        }
        else if (errorCount2 < other.errorCount2)
        {
            return -1;
        }
        
        if (errorCount3 > other.errorCount3)
        {
            return 1;
        }
        else if (errorCount3 < other.errorCount3)
        {
            return -1;
        }
        return 0;
    }
    
    @Override
    public String toString()
    {
        return JsonUtils.getJsonStr(this);
    }
}
