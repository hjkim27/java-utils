package com.hjkim27.util.enc;

import com.hjkim27.exception.EncodingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.MessageDigest;
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
     * @param str     Encrypted target String
     * @param type    SHA_256 / SHA_512
     * @param useSalt salt enable
     * @param salt    salt string
     * @return Encrypted String
     * @throws EncodingException Algorithm that does not exist
     */
    protected static String getShaEncrypt(String str, String type, boolean useSalt, String salt) throws EncodingException {
        SHATypeFormatEnum typeFormat = SHATypeFormatEnum.valueOf(type);

        if (typeFormat != null) {
            try {
                MessageDigest md = MessageDigest.getInstance(typeFormat.type);
                md.update(str.getBytes());
                if (useSalt) {
                    if (salt != null) {
                        md.update(salt.getBytes());
                    } else {
                        md.update(getSalt().getBytes());
                    }
                }
                return String.format(typeFormat.format, new BigInteger(1, md.digest()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new EncodingException(e);
            }
        } else {
            return null;
        }
    }

    /**
     * <pre>
     *     SHA-256 Encrypt
     * </pre>
     *
     * @param str Encrypted target String
     * @return Encrypted String
     * @throws EncodingException Algorithm that does not exist
     */
    public static String get256Encrypt(String str) throws EncodingException {
        return getShaEncrypt(str, "SHA_256", false, null);
    }

    /**
     * <pre>
     *     SHA-256 Encrypt with Salt String
     * </pre>
     *
     * @param str  Encrypted target String
     * @param salt salt
     * @return Encrypted String
     * @throws EncodingException Algorithm that does not exist
     */
    public static String get256EncryptWithSalt(String str, String salt) throws EncodingException {
        return getShaEncrypt(str, "SHA_256", true, salt);
    }

    /**
     * <pre>
     *     SHA-256 Encrypt with Salt String
     * </pre>
     *
     * @param str
     * @return Encrypted String
     * @throws EncodingException Algorithm that does not exist
     */
    public static String get256EncryptWithSalt(String str) throws EncodingException {
        return getShaEncrypt(str, "SHA_256", true, null);
    }

    /**
     * <pre>
     *     SHA-512 Encrypt
     * </pre>
     *
     * @param str Encrypted target String
     * @return Encrypted String
     * @throws EncodingException Algorithm that does not exist
     */
    public static String get512Encrypt(String str) throws EncodingException {
        return getShaEncrypt(str, "SHA_512", false, null);
    }

    /**
     * <pre>
     *     SHA-512 Encrypt with Salt String
     * </pre>
     *
     * @param str Encrypted target String
     * @return Encrypted String
     * @throws EncodingException Algorithm that does not exist
     */
    public static String get512EncryptWithSalt(String str, String salt) throws EncodingException {
        return getShaEncrypt(str, "SHA_512", true, salt);
    }


    /**
     * <pre>
     *     SHA-256 Encrypt with Salt String
     * </pre>
     *
     * @param str
     * @return Encrypted String
     * @throws EncodingException Algorithm that does not exist
     */
    public static String get512EncryptWithSalt(String str) throws EncodingException {
        return getShaEncrypt(str, "SHA_512", true, null);
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
