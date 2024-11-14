package com.hjkim27.util.http;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *  입력한 URL의 각 요소를 반환하는 메서드를 담은 클래스
 *  - Map.key : enum, String 두가지 타입
 * </pre>
 */
@Slf4j
public class UrlParserUtil {

    /**
     * <pre>
     * Map.key :: enum타입
     *     - HTTPS : https(true), http(false)
     *     - PORT: default - https(443), http(80)
     *     - ERROR : url형식이 올바르지 않을 경우, 각 요소를 분할하던 중 에러가 발생했을 경우
     * </pre>
     *
     * @param url
     * @return
     */
    public static Map<Items, Object> getUrlItems(String url) {
        Map<Items, Object> result = new HashMap<>();
        try {
            result.put(Items.URL, url);
            String tmp = url.replace("//", "/");
            String[] v2 = tmp.split("/");

            // url 형식 확인
            if (!url.startsWith("http") || v2.length < 2) {
                result.put(Items.ERROR, "URL형식이 올바르지 않습니다.");
                return result;
            }

            // https
            boolean https = tmp.startsWith("https");
            result.put(Items.HTTPS, https);

            // domain, port
            String[] v3 = v2[1].split(":");
            result.put(Items.DOMAIN, v3[0]);

            if (v3.length != 1 && v3[1].matches("^[0-9]+$")) {
                result.put(Items.PORT, v3[1]);
            } else {
                result.put(Items.PORT, (https) ? 443 : 80);
            }

            // contextPath
            if (v2.length == 2) {
                result.put(Items.CONTEXTPATH, "");
            } else {
                result.put(Items.CONTEXTPATH, v2[2].split("/")[0]);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.put(Items.ERROR, e.getMessage());
        }
        return result;
    }

    /**
     * <pre>
     * Map.key :: String타입
     *     - HTTPS : https(true), http(false)
     *     - PORT: default - https(443), http(80)
     *     - ERROR : url형식이 올바르지 않을 경우, 각 요소를 분할하던 중 에러가 발생했을 경우
     * </pre>
     *
     * @param url
     * @return
     */
    public static Map<String, Object> getUrlItemsStringKey(String url) {
        Map<Items, Object> map = getUrlItems(url);
        Map<String, Object> result = new HashMap<>();
        for (Items item : map.keySet()) {
            result.put(item.name(), map.get(item));
        }
        return result;
    }

    enum Items {
        URL, HTTPS, DOMAIN, PORT, CONTEXTPATH, ERROR;
    }
}

