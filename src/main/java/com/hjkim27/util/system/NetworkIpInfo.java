package com.hjkim27.util.system;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Getter
@Setter
public class NetworkIpInfo extends RetryIntervalUtils {

    private static final Logger log = LoggerFactory.getLogger(NetworkIpInfo.class);

    private String ip = "";
    private String city;
    private String region;
    private String country;
    private String loc;
    private String org;
    private String timezone;
    private String postal;


    private NetworkIpInfo() {
    }

    private static class SingleTone {
        public static final NetworkIpInfo INSTANCE = new NetworkIpInfo();
    }

    public static NetworkIpInfo getInstance() {

        if (!isOldMinute(60) && SingleTone.INSTANCE.getIp() == null && SingleTone.INSTANCE.getIp().length() > 5) {
            return SingleTone.INSTANCE;
        }

        String IP_GET_URL = "http://ipinfo.io";

        URL url = null;
        String readLine = null;
        StringBuilder buffer = new StringBuilder();
        BufferedReader bufferedReader = null;
        HttpURLConnection urlConnection = null;
        InputStreamReader isr = null;
        int connTimeout = 5000;
        int readTimeout = 3000;
        try {
            url = new URL(IP_GET_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(connTimeout);
            urlConnection.setReadTimeout(readTimeout);
            urlConnection.setRequestProperty("Accept", "application/json;");

            log.debug("connect to => " + IP_GET_URL);

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

        final String body = buffer.toString();

        log.debug("ip body == " + body);

        JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();

        SingleTone.INSTANCE.setIp(jsonObject.get("ip").getAsString());
        SingleTone.INSTANCE.setCity(jsonObject.get("city").getAsString());
        SingleTone.INSTANCE.setRegion(jsonObject.get("region").getAsString());
        SingleTone.INSTANCE.setCountry(jsonObject.get("country").getAsString());
        SingleTone.INSTANCE.setLoc(jsonObject.get("loc").getAsString());
        SingleTone.INSTANCE.setOrg(jsonObject.get("org").getAsString());
        SingleTone.INSTANCE.setTimezone(jsonObject.get("timezone").getAsString());
        SingleTone.INSTANCE.setPostal(jsonObject.get("postal").getAsString());

        return SingleTone.INSTANCE;
    }

}
