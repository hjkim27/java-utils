package com.hjkim27.util.enc;

import com.hjkim27.exception.EncodingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * <pre>
 *     Asymmetric Encryption
 *     - SHA-256, SHA-512
 * </pre>
 *
 * @author hjkim27
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
public class SHAUtils {

    private static SHATypeFormatEnum DEFAULT_TYPE = SHATypeFormatEnum.SHA_256;

    /**
     * <pre>
     *     Security measures: (salt:random string) generation
     * </pre>
     *
     * @return salt String
     */
    protected static String getSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[20];
        secureRandom.nextBytes(salt);
        return new String(Base64Utils.encode(salt));
    }


    /**
     * <pre>
     *     Optionally set SHA-256, SHA-512 encryption methods
     *     setting salt enable
     * </pre>
     *
     * @param str            Encrypted target String
     * @param useSalt        salt enable
     * @param typeFormatEnum {@link SHATypeFormatEnum}
     * @return
     * @throws EncodingException Exception occurs during Encrypt
     */
    protected static String encrypt(String str, boolean useSalt, SHATypeFormatEnum typeFormatEnum) throws EncodingException {
        if (typeFormatEnum == null) {
            log.warn("shaEncrypt type is null >> setting default Type : {}", DEFAULT_TYPE);
            typeFormatEnum = DEFAULT_TYPE;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(typeFormatEnum.type);
            md.update(str.getBytes());
            if (useSalt) {
                md.update(getSalt().getBytes());
            }
            return String.format(typeFormatEnum.format, new BigInteger(1, md.digest()));
        } catch (NoSuchAlgorithmException e) {
            log.warn("Algorithm that does not exist : {}", typeFormatEnum.type);
            throw new EncodingException(e);
        }
    }

    /**
     * <pre>
     *     Optionally set SHA-256, SHA-512 encryption methods
     *     setting salt enable
     * </pre>
     *
     * @param str     Encrypted target String
     * @param useSalt salt enable
     * @param type    SHA Encrypt Type in {@link SHATypeFormatEnum#values()}
     * @return Encrypted String
     * @throws EncodingException Exception occurs during Encrypt
     */
    protected static String encrypt(String str, boolean useSalt, String type) throws EncodingException {
        SHATypeFormatEnum typeFormat = SHATypeFormatEnum.valueOf(type);
        return encrypt(str, useSalt, typeFormat);

    }

    /**
     * <pre>
     *     SHA encrypt without salt
     *     Optionally set SHA-256, SHA-512 encryption methods
     * </pre>
     *
     * @param str            Encrypt target String
     * @param typeFormatEnum {@link SHATypeFormatEnum}
     * @return
     * @throws EncodingException Exception occurs during Encrypt
     */
    public static String encrypt(String str, SHATypeFormatEnum typeFormatEnum) throws EncodingException {
        return encrypt(str, false, typeFormatEnum);
    }

    /**
     * <pre>
     *     SHA encrypt with salt
     *     Optionally set SHA-256, SHA-512 encryption methods
     * </pre>
     *
     * @param str            Encrypt target String
     * @param typeFormatEnum {@link SHATypeFormatEnum}
     * @return
     * @throws EncodingException Exception occurs during Encrypt
     */
    public static String encryptWithSalt(String str, SHATypeFormatEnum typeFormatEnum) throws EncodingException {
        return encrypt(str, true, typeFormatEnum);
    }


    /**
     * <pre>
     *     SHA encrypt without salt
     *     Optionally set SHA-256, SHA-512 encryption methods
     * </pre>
     *
     * @param str  Encrypt target String
     * @param type SHA Encrypt Type in {@link SHATypeFormatEnum#values()}
     * @return
     * @throws EncodingException Exception occurs during Encrypt
     */
    public static String encrypt(String str, String type) throws EncodingException {
        return encrypt(str, false, type);
    }

    /**
     * <pre>
     *     SHA encrypt with salt
     *     Optionally set SHA-256, SHA-512 encryption methods
     * </pre>
     *
     * @param str  Encrypt target String
     * @param type SHA Encrypt Type in {@link SHATypeFormatEnum#values()}
     * @return
     * @throws EncodingException Exception occurs during Encrypt
     */
    public static String encryptWithSalt(String str, String type) throws EncodingException {
        return encrypt(str, true, type);
    }


    /**
     * <pre>
     * setting SHA Encrypt type, format
     * </pre>
     */
    @Getter
    @AllArgsConstructor
    public enum SHATypeFormatEnum {
        SHA_256("SHA-256", "%02x"),
        SHA_512("SHA-512", "%0128x");

        final String type;
        final String format;
    }
}
