package com.hjkim27.util.enc;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * <pre>
 *     Base64 Encoding Utils
 * </pre>
 *
 * @author hjkim27
 * @date 2024. 07. 11
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base64Utils {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    static final Base64.Decoder decoder = Base64.getDecoder();

    static final Base64.Encoder encoder = Base64.getEncoder();

    public static byte[] decode(byte[] origin) {
        try {
            return decoder.decode(origin);
        } catch (RuntimeException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new byte[0];
    }

    public static byte[] decode(String originStr) {
        return decode(originStr.getBytes(CHARSET));
    }

    public static String decodeToString(byte[] origin) {
        try {
            return new String(decode(origin));
        } catch (RuntimeException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String decodeToString(String originStr) {
        return decodeToString(originStr.getBytes(CHARSET));
    }

    public static byte[] encode(byte[] origin) {
        try {
            return encoder.encode(origin);
        } catch (RuntimeException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new byte[0];
    }

    public static byte[] encode(String originStr) {
        return encode(originStr.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeToString(byte[] origin) {
        try {
            return new String(encode(origin));
        } catch (RuntimeException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String encodeToString(String originStr) {
        return encodeToString(originStr.getBytes(StandardCharsets.UTF_8));
    }
}
