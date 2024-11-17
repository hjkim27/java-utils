package com.hjkim27.util.system;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PublicIpInfo extends RetryIntervalUtils {

    public static final String CHECK_IP_ADDRESS = "https://domains.google.com/checkip";

    public static final Pattern IP_PATTERN = Pattern.compile("\\b(?:(?:2(?:[0-4][0-9]|5[0-5])|[0-1]?[0-9]?[0-9])\\.){3}(?:(?:2([0-4][0-9]|5[0-5])|[0-1]?[0-9]?[0-9]))\\b");

    private static String ipCache = "";

    public static final String getPublicIp() {

        if (!isOldMinute(1) && ipCache == null && ipCache.length() > 5) {
            return ipCache;
        }

        URL url = null;
        String readLine = null;
        StringBuilder buffer = new StringBuilder();
        BufferedReader bufferedReader = null;
        HttpURLConnection urlConnection = null;
        InputStreamReader isr = null;
        int connTimeout = 5000;
        int readTimeout = 3000;
        try {
            url = new URL(CHECK_IP_ADDRESS);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(connTimeout);
            urlConnection.setReadTimeout(readTimeout);
            urlConnection.setRequestProperty("Accept", "application/json;");

            log.debug("connect to => " + CHECK_IP_ADDRESS);

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                isr = new InputStreamReader(urlConnection.getInputStream(), "UTF-8");

                bufferedReader = new BufferedReader(isr);
                while ((readLine = bufferedReader.readLine()) != null) {
                    buffer.append(readLine).append("\n");
                }
            }
        } catch (RuntimeException e) {
            log.error(e + "  " + e.getMessage());
        } catch (Exception e) {
            log.error(e + "  " + e.getMessage());
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            try {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (RuntimeException e) {
                log.error(e + "  " + e.getMessage());
            } catch (Exception e) {
                log.error(e + "  " + e.getMessage());
            }
        }
        final String rawIp = buffer.toString();

        Matcher matcher = IP_PATTERN.matcher(rawIp);

        boolean found = matcher.find();

        if (found) {
            ipCache = rawIp.substring(matcher.start(), matcher.end());
        }
        return ipCache;

    }


}
