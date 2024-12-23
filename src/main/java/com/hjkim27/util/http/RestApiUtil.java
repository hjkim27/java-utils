package com.hjkim27.util.http;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RestApiUtil {

    @Getter
    private static String SSL = "TLSv1.2";

    /**
     * <pre>
     *      기본 설정된 SSL 말고 다른 버전을 사용하고 싶을 경우 설정 (default : TLSv1.2)
     * </pre>
     *
     * @param ssl https 사용 시 default 말고 tls 버전을 사용하고자 할 경우
     */
    public static void setSSL(String ssl) {
        if (ssl != null && !ssl.isEmpty()) {
            RestApiUtil.SSL = ssl;
        }
    }

    /**
     * <pre>
     *     REST API 통신에서 http/https 에 따른 ssl 설정 정보를 추가하기 위한 메서드
     *     - connectionMagager 설정 포함
     * </pre>
     *
     * @param url
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public static CloseableHttpClient getClient(String url, boolean useConnectionManager) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("URL ===== {}", url);

        HttpClientBuilder clientBuilder = HttpClients.custom();
        CloseableHttpClient client;
        // https 설정
        if (url.startsWith("https")) {
            SSLContext sslContext = SSLContexts.custom()
                    .useProtocol(getSSL())
                    .loadTrustMaterial(null, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                            return true;
                        }
                    }).build();
            clientBuilder.setSSLHostnameVerifier(new NoopHostnameVerifier()).setSSLContext(sslContext);
        }
        if (useConnectionManager) {
            clientBuilder.setMaxConnPerRoute(10);
            clientBuilder.setMaxConnTotal(100);
        }
        client = clientBuilder.build();
        return client;
    }

    /**
     * <pre>
     *     REST API GET
     * </pre>
     *
     * @param url
     * @param headerMap            header info Map
     * @param paramMap             parameter Map
     * @param requestConfig        config
     * @param useConnectionManager PoolingHttpClientConnectionManager 사용여부
     * @return
     */
    public static CloseableHttpResponse runGet(
            String url
            , Map<String, String> headerMap
            , Map<String, Object> paramMap
            , RequestConfig requestConfig
            , boolean useConnectionManager
    ) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = getClient(url, useConnectionManager);

            // parameter
            HttpGet get = null;
            if (paramMap != null) {
                URIBuilder uriBuilder = new URIBuilder(url);
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    uriBuilder.addParameter(key, String.valueOf(value));
                }
                get = new HttpGet(uriBuilder.build());
            } else {
                get = new HttpGet(url);
            }

            // header
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    get.setHeader(key, value);
                }
            }

            // requestConfig
            if (requestConfig != null) {
                get.setConfig(requestConfig);
            }

            response = client.execute(get);
            log.info("{} ## HttpStatus {}", url, response.getStatusLine().getStatusCode());
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } finally {
        }
        return response;
    }

    /**
     * <pre>
     *     REST API POST :: MULTIPART_FORM_DATA
     *     - pararmeter data만 전달할 경우
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPost(
            String url
            , Map<String, String> headerMap
            , HttpMultipartMode multipartMode
            , Map<String, Object> paramMap
    ) {
        return runPost(url, headerMap, multipartMode, null, paramMap);
    }

    /**
     * <pre>
     *     REST API POST :: MULTIPART_FORM_DATA
     *     - 기본설정
     * </pre>
     *
     * @param url
     * @param headerMap     header info Map
     * @param multipartMode HttpMultipartMode
     * @param binaryMap     image byte Map
     * @param paramMap      parameter Map
     * @return
     */
    public static CloseableHttpResponse runPost(
            String url
            , Map<String, String> headerMap
            , HttpMultipartMode multipartMode
            , Map<String, byte[]> binaryMap
            , Map<String, Object> paramMap
    ) {
        return runPost(url, headerMap, multipartMode, binaryMap, paramMap, null, false, null);
    }

    /**
     * <pre>
     *     REST API POST :: MULTIPART_FORM_DATA
     *     - charset 제외 설정
     * </pre>
     *
     * @param url
     * @param headerMap            header info Map
     * @param multipartMode        HttpMultipartMode
     * @param binaryMap            image byte Map
     * @param requestConfig        requestConfig
     * @param useConnectionManager PoolingHttpClientConnectionManager 사용여부
     * @param paramMap             parameter Map
     * @return
     */
    public static CloseableHttpResponse runPost(
            String url
            , Map<String, String> headerMap
            , HttpMultipartMode multipartMode
            , Map<String, byte[]> binaryMap
            , Map<String, Object> paramMap
            , RequestConfig requestConfig
            , boolean useConnectionManager
    ) {
        return runPost(url, headerMap, multipartMode, binaryMap, paramMap, requestConfig, useConnectionManager, null);
    }

    /**
     * <pre>
     *     REST API POST :: MULTIPART_FORM_DATA
     *     - requestConfig 세팅 추가
     * </pre>
     *
     * @param url
     * @param headerMap            header info Map
     * @param multipartMode        HttpMultipartMode
     * @param binaryMap            image byte Map
     * @param paramMap             parameter Map
     * @param requestConfig        requestConfig
     * @param useConnectionManager PoolingHttpClientConnectionManager 사용여부
     * @param charset              인코딩 사용 시 설정
     * @return
     */
    public static CloseableHttpResponse runPost(
            String url
            , Map<String, String> headerMap
            , HttpMultipartMode multipartMode
            , Map<String, byte[]> binaryMap
            , Map<String, Object> paramMap
            , RequestConfig requestConfig
            , boolean useConnectionManager
            , String charset
    ) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = getClient(url, useConnectionManager);
            HttpPost post = new HttpPost(url);

            // header
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    post.setHeader(key, value);
                }
            }

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            // HttpMultipartMode
            if (multipartMode != null) {
                builder.setMode(multipartMode);
            }

            // binaryBody
            if (binaryMap != null) {
                for (Map.Entry<String, byte[]> entry : binaryMap.entrySet()) {
                    builder.addBinaryBody(entry.getKey(), entry.getValue(), ContentType.DEFAULT_BINARY, entry.getKey() + ".jpg");
                }
            }

            // parameters
            if (paramMap != null) {
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // charset
                    if (charset == null || charset.isEmpty()) {
                        builder.addTextBody(key, String.valueOf(value), ContentType.MULTIPART_FORM_DATA);
                    } else {
                        // supported check
                        if (Charset.isSupported(charset)) {
                            log.warn("is not supported charset!!!");
                            builder.addTextBody(key, String.valueOf(value), ContentType.MULTIPART_FORM_DATA);
                        } else {
                            builder.addTextBody(key, String.valueOf(value), ContentType.create(ContentType.MULTIPART_FORM_DATA.getMimeType(), charset));
                        }
                    }
                }
            }

            // requestConfig
            if (requestConfig != null) {
                post.setConfig(requestConfig);
            }

            post.setEntity(builder.build());

            response = client.execute(post);
            log.info("{} ## HttpStatus {}", url, response.getStatusLine().getStatusCode());
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        }
        return response;
    }

    /**
     * <pre>
     *     REST API POST :: JSON
     *     - charset 제외 설정
     * </pre>
     *
     * @param url
     * @param headerMap            header info Map
     * @param jsonData             parameterData :: jsonString
     * @param requestConfig        requestConfig
     * @param useConnectionManager PoolingHttpClientConnectionManager 사용여부
     * @return
     */
    public static CloseableHttpResponse runPostJSON(
            String url
            , Map<String, String> headerMap
            , String jsonData
            , RequestConfig requestConfig
            , boolean useConnectionManager
    ) {
        return runPostJSON(url, headerMap, jsonData, requestConfig, useConnectionManager, null);
    }

    /**
     * <pre>
     *     REST API POST :: JSON
     * </pre>
     *
     * @param url
     * @param headerMap            header info Map
     * @param jsonData             parameterData :: jsonString
     * @param requestConfig        requestConfig
     * @param useConnectionManager PoolingHttpClientConnectionManager 사용여부
     * @param charset              인코딩 사용 시 설정
     * @return
     */
    public static CloseableHttpResponse runPostJSON(
            String url
            , Map<String, String> headerMap
            , String jsonData
            , RequestConfig requestConfig
            , boolean useConnectionManager
            , String charset
    ) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = getClient(url, useConnectionManager);
            HttpPost post = new HttpPost(url);

            // header
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    post.setHeader(key, value);
                }
            }

            // requestConfig
            if (requestConfig != null) {
                post.setConfig(requestConfig);
            }

            // jsonData
            post.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));

            // charset
            if (charset == null || charset.isEmpty()) {
                post.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
            } else {
                // supported check
                if (Charset.isSupported(charset)) {
                    log.warn("is not supported charset!!!");
                    post.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
                } else {
                    post.setEntity(new StringEntity(jsonData, ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), charset)));
                }
            }

            response = client.execute(post);
            log.info("{} ## HttpStatus {}", url, response.getStatusLine().getStatusCode());
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        }
        return response;
    }


    /**
     * <pre>
     *     REST API POST :: x-www-form-urlencoded
     * </pre>
     *
     * @param url
     * @param paramMap             parameter map
     * @param useConnectionManager PoolingHttpClientConnectionManager 사용여부
     * @return
     */
    public static CloseableHttpResponse runPostNameValuePair(String url, Map<String, Object> paramMap, boolean useConnectionManager) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-type", ContentType.APPLICATION_FORM_URLENCODED.toString());
        return runPostNameValuePair(url, headerMap, paramMap, null, useConnectionManager, StandardCharsets.UTF_8.toString());
    }

    /**
     * <pre>
     *     REST API POST :: x-www-form-urlencoded
     * </pre>
     *
     * @param url
     * @param headerMap            header info Map
     * @param paramMap             parameter Map
     * @param requestConfig        requestConfig
     * @param useConnectionManager PoolingHttpClientConnectionManager 사용여부
     * @param charset              인코딩 사용 시 설정
     * @return
     */
    public static CloseableHttpResponse runPostNameValuePair(
            String url
            , Map<String, String> headerMap
            , Map<String, Object> paramMap
            , RequestConfig requestConfig
            , boolean useConnectionManager
            , String charset
    ) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        UrlEncodedFormEntity formEntity = null;

        try {
            client = getClient(url, useConnectionManager);
            HttpPost post = new HttpPost(url);

            // header
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    post.setHeader(key, value);
                }
            }

            // parameters
            List<BasicNameValuePair> paramList = new ArrayList<>();
            if (paramMap != null) {
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    paramList.add(new BasicNameValuePair(key, String.valueOf(value)));
                }
            }

            // charset
            if (charset == null || charset.isEmpty()) {
                formEntity = new UrlEncodedFormEntity(paramList);
            } else {
                // supported check
                if (Charset.isSupported(charset)) {
                    log.warn("is not supported charset!!!");
                    formEntity = new UrlEncodedFormEntity(paramList);
                } else {
                    formEntity = new UrlEncodedFormEntity(paramList, charset);
                }
            }

            post.setEntity(formEntity);

            // requestConfig
            if (requestConfig != null) {
                post.setConfig(requestConfig);
            }

            response = client.execute(post);
            log.info("{} ## HttpStatus {}", url, response.getStatusLine().getStatusCode());
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        }
        return response;
    }

    /**
     * <pre>
     * parameterType 이 query 인 값들을 받아 url을 생성한다.
     * </pre>
     *
     * @param url           url
     * @param queryParamMap query parameter map
     * @return url?key=value&key=value...
     * @since 2024.12
     */
    public static String generateURLUsingQueryParam(String url, Map<String, Object> queryParamMap) {
        if (queryParamMap != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            boolean isFirst = true;
            for (Map.Entry<String, Object> entry : queryParamMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                sb.append((isFirst) ? "?" : "&");
                sb.append(key).append("=").append(value);
                isFirst = false;
            }
            url = sb.toString();
        }
        return url;
    }
}
