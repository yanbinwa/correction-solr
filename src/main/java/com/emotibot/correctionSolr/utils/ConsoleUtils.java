package com.emotibot.correctionSolr.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.emotibot.configclient.ConfigClientOptions;
import com.emotibot.configclient.ConfigResponseCallback;
import com.emotibot.configclient.ConsulConfigClient;

public class ConsoleUtils
{
    private static ConsulConfigClient configClient = null;
    
    public static boolean registerConsul(String consulServiceURL, String consulKeyPrefix, ConfigResponseCallback callback)
    {
        try 
        {
            ConfigClientOptions options = new ConfigClientOptions();
            options.setRecurse(true);
            options.setInterval(10);
            options.setWait(10);

            String hostAndPort = getHostAndPort(consulServiceURL);
            configClient = new ConsulConfigClient(hostAndPort, consulKeyPrefix, callback, options);
            configClient.start();

            configClient.join(10);
            return true;
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public static String getHostAndPort(String url)
    {
        if (url == null || url.trim().equals("")) 
        {  
            return null;  
        }  
          
        if(url.startsWith("http://localhost"))
        {  
            url = url.replace("http://localhost", "http://127.0.0.1") ;  
        }  
          
        String host = "";  
        Pattern p = Pattern.compile("(?<=//|)([\\w\\-]+\\.)+\\w+(:\\d{0,5})?");
        Matcher matcher = p.matcher(url);  
        if (matcher.find()) 
        {  
            host = matcher.group() ;  
        }  
          
        if(host.contains(":") == false)
        {  
            return null;
        }  
        else
        {
            return host;
        }
    }
    
    public static String getHost(String url)
    {
        if (url == null || url.trim().equals("")) 
        {  
            return null;  
        }  
          
        if(url.startsWith("http://localhost"))
        {  
            return "localhost";
        }  
          
        String host = "";  
        Pattern p = Pattern.compile("(?<=//|)([\\w\\-]+\\.)+\\w+");
        Matcher matcher = p.matcher(url);  
        if (matcher.find()) 
        {  
            host = matcher.group() ;  
        }
        return host;
    }
}
