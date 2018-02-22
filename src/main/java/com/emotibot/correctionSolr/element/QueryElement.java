package com.emotibot.correctionSolr.element;

import java.util.List;

import com.emotibot.middleware.utils.JsonUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Solr query的参数
 * 
 * @author emotibot
 *
 */
public class QueryElement
{
    @SerializedName("q")
    @Expose
    private String q = "*:*";
    
    @SerializedName("fq")
    @Expose
    private String fq;
    
    @SerializedName("fl")
    @Expose
    private String fl;
    
    @SerializedName("defType")
    @Expose
    private String defType;
    
    @SerializedName("qf")
    @Expose
    private String qf;
    
    @SerializedName("start")
    @Expose
    private int start = 0;
    
    @SerializedName("rows")
    @Expose
    private int rows = 10;
    
    @SerializedName("database")
    @Expose
    private DatabaseType database;
    
    @SerializedName("appid")
    @Expose
    private String appid;
    
    @SerializedName("fields")
    @Expose
    private List<String> fields;
    
    public QueryElement()
    {
        
    }
    
    public QueryElement(String appid, String q, String fq)
    {
        this.appid = appid;
        this.q = q;
        this.fq = fq;
    }
    
    public QueryElement(String appid, String q, String fq, String fl, int start, int rows)
    {
        this.appid = appid;
        this.q = q;
        this.fq = fq;
        this.start = start;
        this.rows = rows;
    }
    
    public void setQ(String q)
    {
        this.q = q;
    }
    
    public String getQ()
    {
        return this.q;
    }
    
    public void setFq(String fq)
    {
        this.fq = fq;
    }
    
    public String getFq()
    {
        return this.fq;
    }
    
    public void setFl(String fl)
    {
        this.fl = fl;
    }
    
    public String getFl()
    {
        return this.fl;
    }
    
    public void setDefType(String defType)
    {
        this.defType = defType;
    }
    
    public String getDefType()
    {
        return this.defType;
    }
    
    public void setQf(String qf)
    {
        this.qf = qf;
    }
    
    public String getQf()
    {
        return this.qf;
    }
    
    public void setStart(int start)
    {
        this.start = start;
    }
    
    public int getStart()
    {
        return this.start;
    }
    
    public void setRows(int rows)
    {
        this.rows = rows;
    }
    
    public int getRows()
    {
        return this.rows;
    }
    
    public void setDatabase(DatabaseType database)
    {
        this.database = database;
    }
    
    public DatabaseType getDatabase()
    {
        return this.database;
    }
    
    public void setAppid(String appid)
    {
        this.appid = appid;
    }
    
    public String getAppid()
    {
        return this.appid;
    }
    
    public void setFields(List<String> fields)
    {
        this.fields = fields;
    }
    
    public List<String> getFields()
    {
        return this.fields;
    }
    
    @Override
    public String toString()
    {
        return JsonUtils.getJsonStr(this);
    }
}
