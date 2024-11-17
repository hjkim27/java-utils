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
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base64Util {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    static final Base64.Decoder DECODER = Base64.getDecoder();

    static final Base64.Encoder ENCODER = Base64.getEncoder();

    /**
     * <pre>
     *     Decrypt with Base64 (byte array to byte array)
     * </pre>
     *
     * @param origin byte array to decoded
     * @return new byte array containing the decoded bytes
     */
    public static byte[] decode(byte[] origin) {
        try {
            return DECODER.decode(origin);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            } else {
                log.warn(e.getMessage(), e);
            }
        }
        return new byte[0];
    }

    /**
     * <pre>
     *     Decrypt with Base64 (string to byte array)
     * </pre>
     *
     * @param originStr string to decoded
     * @return new byte array containing the decoded bytes
     * @see #decode(byte[])
     */
    public static byte[] decode(String originStr) {
        return decode(originStr.getBytes(CHARSET));
    }

    /**
     * <pre>
     *     Decrypt with Base64 (byte array to String)
     * </pre>
     *
     * @param origin byte array to decoded
     * @return The value of converting a new byte array containing decoded bytes into a string.
     */
    public static String decodeToString(byte[] origin) {
        try {
            return new String(decode(origin));
        } catch (RuntimeException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            } else {
                log.warn(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * <pre>
     *     Decrypt with Base64 (String to String)
     * </pre>
     *
     * @param originStr string to decoded
     * @return The value of converting a new byte array containing decoded bytes into a string.
     */
    public static String decodeToString(String originStr) {
        return decodeToString(originStr.getBytes(CHARSET));
    }

    /**
     * <pre>
     *     Encrypt with Base64 (byte array to byte array)
     * </pre>
     *
     * @param origin byte array to encoded
     * @return new byte array containing the encoded bytes
     */
    public static byte[] encode(byte[] origin) {
        try {
            return ENCODER.encode(origin);
        } catch (RuntimeException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            } else {
                log.warn(e.getMessage(), e);
            }
        }
        return new byte[0];
    }

    /**
     * <pre>
     *     Encrypt with Base64 (String to byte array)
     * </pre>
     *
     * @param originStr String to encoded
     * @return new byte array containing the encoded byte
     */
    public static byte[] encode(String originStr) {
        return encode(originStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * <pre>
     *     Encrypt with Base64 (byte array to String)
     * </pre>
     *
     * @param origin byte array to encoded
     * @return The value of converting a new byte array containing encoded bytes into a string.
     */
    public static String encodeToString(byte[] origin) {
        try {
            return new String(encode(origin));
        } catch (RuntimeException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            } else {
                log.warn(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * <pre>
     *     Encrypt with Base64 (String to String)
     * </pre>
     *
     * @param originStr String to encoded String
     * @return The value of converting a new byte array containing encoded bytes into a string
     */
    public static String encodeToString(String originStr) {
        return encodeToString(originStr.getBytes(StandardCharsets.UTF_8));
    }
}
