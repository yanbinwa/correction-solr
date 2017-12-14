package com.emotibot.correctionSolr.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;

public class MD5Utils
{
    /**
     * 为文件生成md5码
     * 
     * @param file，文件
     * @return 文件的md5码，如果失败返回null
     */
    public static String getMd5ForFile(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            return getMD5ByStream(in);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 为字符串生成md5码
     * 
     * @param str，字符串
     * @return 文件的md5码，如果失败返回null
     */
    public static String getMd5ForStr(String str) {
        try {
            InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
            return getMD5ByStream(in);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @author 刘影 为url文件生成md5码
     * @param url，url文件
     * @return url文件的md5码，如果失败返回null
     */
    public static InputStream GetMd5ForUrl(String url) {
        try {
            // 构造请求url
            URL localURL = new URL(url);
            URLConnection connection = localURL.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

            // 设置连接属性
            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 返回代码大于300，表明出错
            if (httpURLConnection.getResponseCode() >= 300) {
                System.out.print(url.split("=")[1]);
                return null;
            }

            // 输入流
            InputStream inputStream;

            inputStream = httpURLConnection.getInputStream();
            return inputStream;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @author 刘影 根据输入流得到md5码
     * @param fis，输入流
     * @return md5码，如果失败返回null
     */
    private static String getMD5ByStream(InputStream fis) {
        String value = null;
        // 这个byte[]的长度可以是任意的
        byte[] buf = new byte[4096];
        MessageDigest md;
        boolean fileIsNull = true;

        try {
            int len = 0;
            md = MessageDigest.getInstance("MD5");

            len = fis.read(buf);
            if (len > 0) {
                fileIsNull = false;
                while (len > 0) {
                    md.update(buf, 0, len);
                    len = fis.read(buf);
                }
            }
        } catch (Exception e) {
            return null;
        }

        if (fileIsNull)
            return null;
        else {
            BigInteger bi = new BigInteger(1, md.digest());
            value = bi.toString(16);
            return value;
        }
    }

    public static String getMD5ByByte(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            int len = content.length;
            md.update(content, 0, len);
            BigInteger bi = new BigInteger(1, md.digest());
            String value = bi.toString(16);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
