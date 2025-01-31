package com.hjkim27.util.http;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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
    protected static CloseableHttpClient getClient(String url, boolean useConnectionManager) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
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


    /* multipart/form-data ========== */


    /**
     * <pre>
     *     multipart/form-data HEAD
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runHead(String url, Map<String, String> headerMap, HttpMultipartMode multipartMode, Map<String, Object> paramMap, String charset) {
        return sendFormData(url, headerMap, paramMap, null, false, HttpRequestMethod.HEAD);
    }

    /**
     * <pre>
     *     multipart/form-data DELETE
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runDelete(String url, Map<String, String> headerMap, HttpMultipartMode multipartMode, Map<String, Object> paramMap, String charset) {
        return sendFormData(url, headerMap, paramMap, null, false, HttpRequestMethod.DELETE);
    }

    /**
     * <pre>
     *     multipart/form-data OPTIONS
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runOptions(String url, Map<String, String> headerMap, HttpMultipartMode multipartMode, Map<String, Object> paramMap, String charset) {
        return sendFormData(url, headerMap, paramMap, null, false, HttpRequestMethod.OPTIONS);
    }

    /**
     * <pre>
     *     multipart/form-data TRACE
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runTrace(String url, Map<String, String> headerMap, HttpMultipartMode multipartMode, Map<String, Object> paramMap, String charset) {
        return sendFormData(url, headerMap, paramMap, null, false, HttpRequestMethod.TRACE);
    }

    /**
     * <pre>
     *     multipart/form-data GET
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runGet(String url, Map<String, String> headerMap, HttpMultipartMode multipartMode, Map<String, Object> paramMap, String charset) {
        return sendFormData(url, headerMap, paramMap, null, false, HttpRequestMethod.GET);
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
    public static CloseableHttpResponse sendFormData(
            String url
            , Map<String, String> headerMap
            , Map<String, Object> paramMap
            , RequestConfig requestConfig
            , boolean useConnectionManager
            , HttpRequestMethod method
    ) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = getClient(url, useConnectionManager);

            // parameter
            HttpGet get = null;
            URI uri = null;
            if (paramMap != null) {
                URIBuilder uriBuilder = new URIBuilder(url);
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    uriBuilder.addParameter(key, String.valueOf(value));
                }
                uri = uriBuilder.build();
            } else {
                uri = URI.create(url);
            }

            HttpRequestBase request = null;
            switch (method) {
                case HEAD:
                    request = new HttpHead(uri);
                    break;
                case DELETE:
                    request = new HttpDelete(uri);
                    break;
                case OPTIONS:
                    request = new HttpOptions(uri);
                    break;
                case TRACE:
                    request = new HttpTrace(uri);
                    break;
                case GET:
                default:
                    request = new HttpGet(uri);
                    break;
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
     *     multipart/form-data PUT
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPut(String url, Map<String, String> headerMap, HttpMultipartMode multipartMode, Map<String, Object> paramMap, String charset) {
        return sendFormDataWithEntity(url, headerMap, multipartMode, null, paramMap, null, false, charset, HttpRequestMethod.PUT);
    }

    /**
     * <pre>
     *     multipart/form-data PATCH
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPatch(String url, Map<String, String> headerMap, HttpMultipartMode multipartMode, Map<String, Object> paramMap, String charset) {
        return sendFormDataWithEntity(url, headerMap, multipartMode, null, paramMap, null, false, charset, HttpRequestMethod.PATCH);
    }

    /**
     * <pre>
     *     multipart/form-data POST
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPost(String url, Map<String, String> headerMap, HttpMultipartMode multipartMode, Map<String, Object> paramMap, String charset) {
        return sendFormDataWithEntity(url, headerMap, multipartMode, null, paramMap, null, false, charset, HttpRequestMethod.POST);
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
    public static CloseableHttpResponse sendFormDataWithEntity(
            String url
            , Map<String, String> headerMap
            , HttpMultipartMode multipartMode
            , Map<String, byte[]> binaryMap
            , Map<String, Object> paramMap
            , RequestConfig requestConfig
            , boolean useConnectionManager
            , String charset
            , HttpRequestMethod method
    ) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = getClient(url, useConnectionManager);

            HttpEntityEnclosingRequestBase request = null;
            switch (method) {
                case PUT:
                    request = new HttpPut();
                    break;
                case PATCH:
                    request = new HttpPatch();
                    break;
                case POST:
                default:
                    request = new HttpPost();
                    break;
            }

            // header
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    request.setHeader(key, value);
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
                request.setConfig(requestConfig);
            }

            request.setEntity(builder.build());

            response = client.execute(request);
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
    /* /multipart/form-data ========== */


    /* JSON ========== */

    /**
     * <pre>
     *     JSON PUT
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPUTJson(String url, Map<String, String> headerMap, String jsonData) {
        return sendJsonWithEntity(url, headerMap, jsonData, null, false, null, HttpRequestMethod.PUT);
    }

    /**
     * <pre>
     *     JSON PATCH
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPATCHJson(String url, Map<String, String> headerMap, String jsonData) {
        return sendJsonWithEntity(url, headerMap, jsonData, null, false, null, HttpRequestMethod.PATCH);
    }

    /**
     * <pre>
     *     JSON POST
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPOSTJson(String url, Map<String, String> headerMap, String jsonData) {
        return sendJsonWithEntity(url, headerMap, jsonData, null, false, null, HttpRequestMethod.POST);
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
    public static CloseableHttpResponse sendJsonWithEntity(
            String url
            , Map<String, String> headerMap
            , String jsonData
            , RequestConfig requestConfig
            , boolean useConnectionManager
            , String charset
            , HttpRequestMethod method
    ) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = getClient(url, useConnectionManager);

            HttpEntityEnclosingRequestBase request = null;
            switch (method) {
                case PUT:
                    request = new HttpPut();
                    break;
                case PATCH:
                    request = new HttpPatch();
                    break;
                case POST:
                default:
                    request = new HttpPost();
                    break;
            }

            // header
            request.setHeader("Content-type", ContentType.APPLICATION_JSON.toString());
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    request.setHeader(key, value);
                }
            }

            // requestConfig
            if (requestConfig != null) {
                request.setConfig(requestConfig);
            }

            // charset
            if (jsonData != null) {
                if (charset == null || charset.isEmpty()) {
                    request.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
                } else {
                    // supported check
                    if (Charset.isSupported(charset)) {
                        log.warn("is not supported charset!!!");
                        request.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
                    } else {
                        request.setEntity(new StringEntity(jsonData, ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), charset)));
                    }
                }
            }

            response = client.execute(request);
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

    /* /JSON ========== */


    /* x-www-form-urlencoded ========== */

    /**
     * <pre>
     *     x-www-form-urlencoded    PUT
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPUTNameValuePair(String url, Map<String, String> headerMap, Map<String, Object> paramMap) {
        return sendNameValuePairWithEntity(url, headerMap, paramMap, null, false, null, HttpRequestMethod.PUT);
    }

    /**
     * <pre>
     *     x-www-form-urlencoded    PATCH
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPATCHNameValuePair(String url, Map<String, String> headerMap, Map<String, Object> paramMap) {
        return sendNameValuePairWithEntity(url, headerMap, paramMap, null, false, null, HttpRequestMethod.PATCH);
    }

    /**
     * <pre>
     *     x-www-form-urlencoded    POST
     * </pre>
     *
     * @return
     */
    public static CloseableHttpResponse runPOSTNameValuePair(String url, Map<String, String> headerMap, Map<String, Object> paramMap) {
        return sendNameValuePairWithEntity(url, headerMap, paramMap, null, false, null, HttpRequestMethod.POST);
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
    public static CloseableHttpResponse sendNameValuePairWithEntity(
            String url
            , Map<String, String> headerMap
            , Map<String, Object> paramMap
            , RequestConfig requestConfig
            , boolean useConnectionManager
            , String charset
            , HttpRequestMethod method
    ) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        UrlEncodedFormEntity formEntity = null;

        try {
            client = RestApiUtil.getClient(url, useConnectionManager);

            HttpEntityEnclosingRequestBase request = null;
            switch (method) {
                case PUT:
                    request = new HttpPut();
                    break;
                case PATCH:
                    request = new HttpPatch();
                    break;
                case POST:
                default:
                    request = new HttpPost();
                    break;
            }

            // header
            request.setHeader("Content-type", ContentType.APPLICATION_FORM_URLENCODED.toString());
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    request.setHeader(key, value);
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

            request.setEntity(formEntity);

            // requestConfig
            if (requestConfig != null) {
                request.setConfig(requestConfig);
            }

            response = client.execute(request);
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
    /* /x-www-form-urlencoded ========== */

}
