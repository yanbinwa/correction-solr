package com.emotibot.correctionSolr.utils;

import java.lang.reflect.Method;

public class Base64CoderUtils
{
    private Base64CoderUtils(){
    }

    
    public static byte[] string2byteArray(String str) {
        return str.getBytes();
    }
    
    public static String byteArray2string(byte[] bstr) {
        return new String(bstr);
    } 
    
   /**  
    * encoder 
    * 
    * @param bstr  
    * @return String  
    */    
   @SuppressWarnings("restriction")
   public static String encode(byte[] bstr) {    
       return new sun.misc.BASE64Encoder().encode(bstr);    
   }    
   
   /**  
    * decoder
    *   
    * @param str  
    * @return string  
    */    
    @SuppressWarnings("restriction")
    public static byte[] decode(String str){    
       byte[] bt = null;    
       try {    
           sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();    
           bt = decoder.decodeBuffer( str );    
       } catch (Exception e) {    
           e.printStackTrace();    
       }    
       return bt;    
   }    
   
   /**  
    * decoder
    *   
    * @param str  
    * @return string  
    */    
    @SuppressWarnings("restriction")
    public static String decode2string(String str){    
       byte[] bt = null;    
       try {    
           sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();    
           bt = decoder.decodeBuffer( str );    
       } catch (Exception e) {    
           e.printStackTrace();    
       }    
       return byteArray2string(bt);    
    }  
   
    /**
     * encode by Base64 
     */  
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String encodeBase64(byte[]input) throws Exception{  
        Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");  
        Method mainMethod= clazz.getMethod("encode", byte[].class);  
        mainMethod.setAccessible(true);  
         Object retObj=mainMethod.invoke(null, new Object[]{input});  
         return (String) retObj;  
    }  
    
    /**
     * decode by Base64 
     */  
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static byte[] decodeBase64(String input) throws Exception{  
        Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");  
        Method mainMethod= clazz.getMethod("decode", String.class);  
        mainMethod.setAccessible(true);  
         Object retObj=mainMethod.invoke(null, input);  
         return (byte[]) retObj;  
    } 
    
    /**
     * decode by Base64 , return String 
     */  
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String decodeBase64ToString(String input) throws Exception{  
        Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");  
        Method mainMethod= clazz.getMethod("decode", String.class);  
        mainMethod.setAccessible(true);  
         Object retObj=mainMethod.invoke(null, input);  
         return byteArray2string((byte[]) retObj);  
    } 
}
