package com.emotibot.correctionSolr.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils
{
  //private static final Logger logger = Logger.getLogger(HttpUtils.class);
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final String HostAndPortRegex = ".*//([^/]+)/*";
    private static final int BufferSize = 4096;
    
    private HttpUtils()
    {
        
    }
    
    /**
     * 从url得到主机和端口号
     * @param URL
     * @return 主机和端口号
     */
    public static String getHostAndPort(String URL){
        String hostAndPort = URL.replaceAll(HostAndPortRegex, "$1");
        return hostAndPort;
    }
    
    /**
     * 获取url的文件内容
     * @param url
     * @return 文件内容
     */
    public static byte[] getContent(String url) {
        try {
            InputStream in = new URL(url).openStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] data = new byte[BufferSize];
            int count = -1;
            while ((count = in.read(data, 0, BufferSize)) > 0)
                outStream.write(data, 0, count);
            data = null;
            return outStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
            return null;
        }
    }
    
    public static String get(String inputURL, int connectTimeout, int readTimeout) {
        logger.debug("The URL through get is: " + inputURL);
        HttpURLConnection conn = null;
        String result = "";
        try {
            URL url = new URL(inputURL);
            conn = (HttpURLConnection) url.openConnection();
            // 设置超时时间
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);

            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept-charset", "UTF-8");
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed: HTTP error code: " + responseCode);
            } else {
                InputStream inputStream = conn.getInputStream();
                byte[] data = readInputStream(inputStream);
                result = new String(data, "UTF-8");
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.info(sw.toString());
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        return result;
    }

    public static String get(String inputURL) {
        return get(inputURL, Constants.DEFAULT_TIMEOUT, Constants.DEFAULT_TIMEOUT);
    }

    private static byte[] readInputStream(InputStream inputStream) {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outstream.write(buffer, 0, len);
            }
            inputStream.close();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.info(sw.toString());
        }

        return outstream.toByteArray();
    }

    public static String postByJson(String server, String json, int connectTimeout, int readTimeout) {
        logger.info(String.format("server: %s; json: %s", server, json));

        HttpURLConnection conn = null;
        String result = "";

        try {
            URL url = new URL(server);
            conn = (HttpURLConnection) url.openConnection();
            // 设置超时时间
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.flush();
            os.close();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            } else {
                InputStream inputStream = conn.getInputStream();
                byte[] data = readInputStream(inputStream);
                result = new String(data, "UTF-8");
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.info(sw.toString());
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        return result;
    }
    public static String postByJson(String server, String json) {
        return postByJson(server, json, Constants.DEFAULT_TIMEOUT, Constants.DEFAULT_TIMEOUT);
    }
    private class Constants {
        public static final int DEFAULT_TIMEOUT = 4500;
    }
}
