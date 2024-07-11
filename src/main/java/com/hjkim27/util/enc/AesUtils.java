package com.hjkim27.util.enc;

import com.hjkim27.util.enc.exception.EncodingException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;

/**
 * <pre>
 *     AES256Util
 *     암호화/복호화 과정에서 동일한 키를 사용하는 대칭 키 알고리즘
 * </pre>
 *
 * @author hjkim27
 * @date 2024. 07. 11
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
public class AesUtils {
    protected static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5Padding";
    protected static final String AES = "AES";
    protected static final String UTF_8 = StandardCharsets.UTF_8.name();


    protected static byte[] generateIvFromString(final String key) throws EncodingException {
        if (key == null) {
            throw new EncodingException("key:string is null");
        }

        StringBuilder var1 = new StringBuilder(key);
        while (var1.length() < 16) {
            var1.append(key);
        }
        var1 = new StringBuilder(var1.substring(0, 16));
        return var1.toString().getBytes();
    }

    /**
     * <pre>
     *     AES256 암/복호화 parameter validation
     * </pre>
     *
     * @param str 암/복호화 대상 문자열
     * @param key string(32)
     * @param iv  byte[16]
     * @throws EncodingException
     */
    protected static void validateParam(final String str, final String key, final byte[] iv) throws EncodingException {
        if (str == null) {
            throw new EncodingException("target:string is null");
        }
        if (key == null) {
            throw new EncodingException("key:string is null");
        }
        if (iv == null) {
            throw new EncodingException("iv:byte[] is null");
        }
        if (iv.length != 16) {
            throw new EncodingException("The length entered for iv:byte[] is " + iv.length + ", not 16.");
        }
    }

    /* ================================================== */

    /**
     * <pre>
     *     AES256 복호화
     * </pre>
     *
     * @param str 대상 문자열
     * @param key string(32)
     * @param iv  byte[16]
     * @return 복호화된 string
     * @throws EncodingException
     */
    public static String decrypt(final String str, final String key, final byte[] iv) throws EncodingException {
        try {
            validateParam(str, key, iv);

            String secretKey = key.substring(0, 32);
            byte[] textBytes = Base64Utils.decode(str);

            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec newKey = new SecretKeySpec(secretKey.getBytes(UTF_8), AES);
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);

            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
            byte[] bytes = cipher.doFinal(textBytes);

            return new String(bytes, UTF_8);

        } catch (EncodingException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new EncodingException(e);
        }
    }

    /**
     * <pre>
     *     AES256 복호화 : iv from key 사용
     * </pre>
     *
     * @param str 대상 문자열
     * @param key string(32)
     * @return 복호화된 string
     * @throws EncodingException
     */
    public static String decrypt(String str, final String key) throws EncodingException {
        return decrypt(str, key, generateIvFromString(key));
    }

    /**
     * <pre>
     *      암호화
     * </pre>
     *
     * @param str 대상 문자열
     * @param key string(32)
     * @param iv  byte[16]
     * @return 암호화된 string
     * @throws EncodingException
     */
    public static String encrypt(final String str, final String key, final byte[] iv) throws EncodingException {
        try {
            validateParam(str, key, iv);

            String secretKey = key.substring(0, 32);
            byte[] textBytes = str.getBytes(UTF_8);

            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec newKey = new SecretKeySpec(secretKey.getBytes(UTF_8), AES);
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);

            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
            byte[] bytes = cipher.doFinal(textBytes);

            return Base64Utils.encodeToString(bytes);
        } catch (EncodingException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new EncodingException(e);
        }
    }

    /**
     * <pre>
     *     AES256 복ㅎ화 : iv fron key 사용
     * </pre>
     *
     * @param str 대상 문자열
     * @param key string(32)
     * @return 암호화된 string
     * @throws EncodingException
     */
    public static String encrypt(final String str, final String key) throws EncodingException {
        return encrypt(str, key, generateIvFromString(str));
    }
}
