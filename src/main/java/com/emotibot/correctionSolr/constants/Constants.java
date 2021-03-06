package com.emotibot.correctionSolr.constants;

public class Constants
{
    public static final String SOLR_URL_KEY = "SOLR_URL";
    
    public static final int SOLR_CONNECTION_TIMEOUT = 30000;
    public static final int SOLR_MAX_CONNECTION = 100;
    public static final int SOLR_MAX_TOTAL_CONNECTION = 100;
    public static final int SOLR_SO_TIMEOUT = 30000;
    
    //同义词
    public static final String CONSUL_SERVICE_URL_KEY = "CONSUL_SERVICE_URL_KEY";
    public static final String CONSUL_KEY_PREFIX_KEY = "CONSUL_KEY_PREFIX_KEY";
    public static final String RUN_ON_LOCAL_KEY = "RUN_ON_LOCAL_KEY";
    public static final String APPID_KEY = "APPID_KEY";
    
    public static final int CONSUL_INTERVAL_TIME = 2;
    public static final int CONSUL_WAIT_TIME = 2;
    
    public static final String CONSUL_VALUE_JSON_KEY_URL = "url";
    public static final String CONSUL_VALUE_JSON_KEY_MD5 = "md5";
    public static final String CONSUL_VALUE_JSON_KEY_SYNONYM_URL = "synonym-url";
    public static final String CONSUL_VALUE_JSON_KEY_SYNONYM_MD5 = "synonym-md5";
    public static final String LINE_SPLIT_REGEX = "\\r\\n|\\n|\\r";
    public static final int CONSUL_JOIN_TIME = 20;
    
    //MyEmbeddedServletContainerFactory
    public static final String TOMCAT_PORT_KEY = "TOMCAT_PORT";
    public static final String TOMCAT_MAX_CONNECTION_KEY = "TOMCAT_MAX_CONNECTION";
    public static final String TOMCAT_MAX_THREAD_KEY = "TOMCAT_MAX_THREAD";
    public static final String TOMCAT_CONNECTION_TIMEOUT_KEY = "TOMCAT_CONNECTION_TIMEOUT";
    
    
    public static final String SENTENCE_KEY = "SENTENCE";
    public static final String CORRECTION_SENTENCE_KEY = "CORRECTION_SENTENCE";
    public static final String SENTENCE_LIKELY_KEY = "SENTENCE_LIKELY";
    
    public static final String SOLR_SENTENCE_FIELD = "sentence";
    public static final String SOLR_SENTENCE_SYN_FIELD = "sentence_syn";
    
    public static final float SCORE_THRESHOLD_RATE_PROTENTIAL = 3 / 1.0f;
    public static final float SCORE_THRESHOLD_RATE_RECOMMEND = 2 / 1.0f;
    public static final float SCORE_THRESHOLD_PROTENTIAL = 2.0f;
    public static final float SCORE_THRESHOLD_RECOMMEND = 1.8f;
    public static final int RECOMMEND_NUM = 1;
    public static final int POTENTIAL_NUM = 10;
    public static final float SCORE_THRESHOLD_DIFF_PROTENTIAL = 0.3f;
    public static final float SCORE_THRESHOLD_DIFF_RECOMMEND = 0.3f;
    
    public static final float SYN_WORD_ADJUST_THRESHOLD = 4.0f;
    public static final float SYN_WORD_ADJUST_RATE = 1.0f;
    
    public static final float DISTANCE_TOTAL_MATCH_RATE = -1.0f;
    public static final float DISTANCE_WITHOUT_ORDER_RATE = 0.3f;
    public static final float DISTANCE_RATE = 0.3f;
    public static final float DISTANCE_SYN_SCORE = 0.5f;
    
    public static final float SCORE_THRESHOLD_DIFF_CHOOSE = 0.05f;
    
    
    //command
    public static final String COMMAND_FILE_PATH_KEY = "COMMAND_FILE_PATH";
    public static final int ERROR_COUNT_2_THRESHOLD = 2;
    public static final int ERROR_COUNT_3_THRESHOLD = 2;
    public static final int ERROR_COUNT_2_LEN_THRESHOLD = 4;
    public static final int COMMOND_THREAD_NUM = 5;
    
    public static final String CORRECTION_COMMAND_SENTENCE_KEY = "CORRECTION_COMMAND_SENTENCE";
    
    public static final String OLD_NAME = "old_text";
    public static final String LIKELY_NAME_ARR = "likely_names";
}
