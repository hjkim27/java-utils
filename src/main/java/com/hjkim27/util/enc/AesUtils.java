package com.hjkim27.util.enc;

import com.hjkim27.exception.EncodingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;

/**
 * <pre>
 *     AES256Util
 *     - Symmetric key algorithms using the same key during encryption/decryption
 * </pre>
 *
 * @author hjkim27
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
public class AesUtils {

    // AES 암호호 알고리즘
    // 암호 운용방식 : CBC모드
    // 패딩 기법 : PKCS5
    protected static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5Padding";
    protected static final String AES = "AES";
    protected static final String UTF_8 = StandardCharsets.UTF_8.name();
    protected static final String SECRET_KEY = "42673e967095443ab5856396b4ea51a8";

    /**
     * <pre>
     *   generate private key
     * </pre>
     *
     * @param key
     * @return
     * @throws EncodingException
     */
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
     *     parameter validation
     * </pre>
     *
     * @param str Encrypt/Decrypt target String
     * @param key key
     * @param iv  {@link #generateIvFromString(String)}
     * @throws EncodingException exist null value among input parameter or iv length does not 16
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
     *     AES256 Decrypt
     * </pre>
     *
     * @param str Decrypted target String
     * @param key string(32)
     * @param iv  {@link #generateIvFromString(String)}
     * @return Decrypted string
     * @throws EncodingException Error occurs during decryption
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
     *     AES256 Decrypt
     * </pre>
     *
     * @param str Decrypted target String
     * @param key string(32)
     * @return Decrypted string
     * @throws EncodingException Error occurs during decryption
     */
    public static String decrypt(String str, final String key) throws EncodingException {
        return decrypt(str, key, generateIvFromString(key));
    }

    /**
     * <pre>
     *      AES256 Encrypt
     * </pre>
     *
     * @param str Encrypted target String
     * @param key string(32)
     * @param iv  {@link #generateIvFromString(String)}
     * @return Encrypted String
     * @throws EncodingException Error occurs during encryption
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
     *     AES256 Encrypt
     * </pre>
     *
     * @param str Encrypted target String
     * @param key string(32)
     * @return Encrypted String
     * @throws EncodingException Error occurs during encryption
     */
    public static String encrypt(final String str, final String key) throws EncodingException {
        return encrypt(str, key, generateIvFromString(str));
    }
}
