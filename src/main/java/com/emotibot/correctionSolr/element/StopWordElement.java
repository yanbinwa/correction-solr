package com.emotibot.correctionSolr.element;

import java.util.HashSet;
import java.util.Set;

import com.emotibot.middleware.utils.JsonUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StopWordElement
{
    public static final String BEGIN_WORD = "beginWord";
    public static final String END_WORD = "endWord";
    
    @SerializedName("word")
    @Expose
    private String word;
    
    @SerializedName("beforeWords")
    @Expose
    private Set<String> beforeWords = new HashSet<String>();
    
    @SerializedName("afterWords")
    @Expose
    private Set<String> afterWords = new HashSet<String>();
    
    public StopWordElement()
    {
        
    }
    
    public void setWord(String word)
    {
        this.word = word;
    }
    
    public String getWord()
    {
        return this.word;
    }
    
    public void addBeforeWord(String word)
    {
        beforeWords.add(word);
    }
    
    public void addAfterWord(String word)
    {
        afterWords.add(word);
    }
    
    public boolean isBeforeWord(String word)
    {
        return beforeWords.contains(word);
    }
    
    public boolean isAfterWord(String word)
    {
        return afterWords.contains(word);
    }
    
    @Override
    public String toString()
    {
        return JsonUtils.getJsonStr(this);
    }
}
