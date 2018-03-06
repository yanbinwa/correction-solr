package com.emotibot.correctionSolr.constants;

public class Constants
{
    //Solr
    public static final String SOLR_URL_KEY = "SOLR_URL";
    public static final int SOLR_CONNECTION_TIMEOUT = 30000;
    public static final int SOLR_MAX_CONNECTION = 100;
    public static final int SOLR_MAX_TOTAL_CONNECTION = 100;
    public static final int SOLR_SO_TIMEOUT = 30000;
    
    //词库
    public static final String CONSUL_SERVICE_URL_KEY = "CONSUL_SERVICE_URL";
    public static final String CONSUL_KEY_PREFIX_KEY = "CONSUL_KEY_PREFIX";
    public static final String RUN_ON_LOCAL_KEY = "RUN_ON_LOCAL";
    public static final int CONSUL_INTERVAL_TIME = 2;
    public static final int CONSUL_WAIT_TIME = 2;
    public static final String CONSUL_VALUE_JSON_KEY_URL = "url";
    public static final String CONSUL_VALUE_JSON_KEY_VERSION = "version";
    public static final String LINE_SPLIT_REGEX = "\\r\\n|\\n|\\r";
    public static final String LINE_FIELD_SPLIT = "\t";
    public static final int CONSUL_JOIN_TIME = 20;
    
    //Tomcat配置
    public static final String TOMCAT_PORT_KEY = "TOMCAT_PORT";
    public static final String TOMCAT_MAX_CONNECTION_KEY = "TOMCAT_MAX_CONNECTION";
    public static final String TOMCAT_MAX_THREAD_KEY = "TOMCAT_MAX_THREAD";
    public static final String TOMCAT_CONNECTION_TIMEOUT_KEY = "TOMCAT_CONNECTION_TIMEOUT";
    
    //Context
    public static final String SENTENCE_KEY = "SENTENCE";
    public static final String CORRECTION_SENTENCE_KEY = "CORRECTION_SENTENCE";
    public static final String SENTENCE_LIKELY_KEY = "SENTENCE_LIKELY";
    
    //Solr查询配置
    public static final String SOLR_SENTENCE_FIELD = "sentence";
    public static final String SOLR_SENTENCE_SYN_FIELD = "sentence_syn";
    public static final String APPID_KEY = "APPID_KEY";
    public static final String FIELD_KEY = "FIELD_KEY";
    public static final String FIELD_SPLIT = ",";
    
    //纠错算法中的调节参数
    /* Solr结果中score的最大值，如果大于最大值，取该最大值作为Solr的score */
    public static final float SOLR_MAX_SCORE = 5f;
    
    /* 通过Solr得到的score做梯度下降截取候选的词条*/
    public static final float SCORE_THRESHOLD_RATE_PROTENTIAL = 3 / 1.0f;
    public static final float SCORE_THRESHOLD_PROTENTIAL = 2.0f;
    public static final int POTENTIAL_NUM = 10;
    public static final float SCORE_THRESHOLD_DIFF_PROTENTIAL = 0.5f;
    
    /* 对于Solr得到的score进行调整后做梯度下降截取候选的词条*/
    public static final float SCORE_THRESHOLD_RATE_RECOMMEND = 2 / 1.0f;
    public static final float SCORE_THRESHOLD_RECOMMEND_0 = 1.5f;
    public static final float SCORE_THRESHOLD_RECOMMEND_1 = 1.8f;
    public static final float SCORE_THRESHOLD_RECOMMEND_2 = 2.1f;
    public static final float SCORE_THRESHOLD_DIFF_RECOMMEND = 0.3f;
    
    /* 弃用 */
    public static final float SYN_WORD_ADJUST_THRESHOLD = 4.0f;
    public static final float SYN_WORD_ADJUST_RATE = 1.0f;
    
    /* 对于Solr得到的score进行调整时所用 */
    public static final float DISTANCE_TOTAL_MATCH_RATE = -1.0f;
    public static final float DISTANCE_WITHOUT_ORDER_RATE = 0.3f;
    public static final float DISTANCE_RATE = 0.3f;
    public static final float DISTANCE_SYN_SCORE = 0.5f;
    
    /* 最终选取词条时所用 */
    public static final float SCORE_THRESHOLD_DIFF_CHOOSE = 0.05f;
    
    //correction参数
    public static final String ENABLE_HOMONYM_CORRECTION_KEY = "ENABLE_HOMONYM_CORRECTION";
    public static final String ENABLE_SYNONYM_CORRECTION_KEY = "ENABLE_SYNONYM_CORRECTION";
    public static final String ENABLE_INVERT_ORDER_CORRECTION_KEY = "ENABLE_INVERT_ORDER_CORRECTION";
    public static final String ENABLE_RECOMMEND_KEY = "ENABLE_RECOMMEND";
    public static final String MAX_RECOMMEND_NUM_KEY = "MAX_RECOMMEND_NUM";
    public static final String CORRECTION_THRESHOLD_LEVEL_KEY = "CORRECTION_THRESHOLD_LEVEL";
    
    //返回结果中的key
    public static final String OLD_NAME = "old_text";
    public static final String LIKELY_NAME_ARR = "likely_names";
    public static final String NAME = "name";
    public static final String FIELD = "field";
    public static final String SCORE = "score";
    public static final String FIELD_MOVIE = "video";
    public static final String FIELD_MUSIC = "music";
}
