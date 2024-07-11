package com.hjkim27.util.enc;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * <pre>
 *     비대칭 암호화
 * </pre>
 *
 * @author hjkim27
 * @date 2024. 07. 11
 * @since 0.0.1-SNAPSHOT
 */
public class SHAUtils {

    /**
     * <pre>
     *     보안대책 : (salt:무작위 랜덤 문자열) 생성
     * </pre>
     *
     * @return
     */
    protected static String getSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[20];
        secureRandom.nextBytes(salt);
        return new String(Base64Utils.encode(salt));
    }


    /**
     * <pre>
     *     옵션에 따라 256,512 암호화 방식 설정
     *     salt 사용여부 설정
     * </pre>
     *
     * @param str     암호화 대상 문자열
     * @param type    256 / 512
     * @param useSalt salt 사용여부
     * @return
     * @throws NoSuchAlgorithmException
     */
    protected static final String getShaEncrypt(String str, int type, boolean useSalt) throws NoSuchAlgorithmException {
        TYPE_FORMAT typeFormat = (type == 512) ? TYPE_FORMAT.SHA_512 : TYPE_FORMAT.SHA_256;

        MessageDigest md = MessageDigest.getInstance(typeFormat.type);
        md.update(str.getBytes());
        if (useSalt) {
            md.update(getSalt().getBytes());
        }
        return String.format(typeFormat.format, new BigInteger(1, md.digest()));
    }

    /**
     * <pre>
     *     SHA-256 암호화
     * </pre>
     *
     * @param str 암호화 대상 문자열
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static final String get256Encrypt(String str) throws NoSuchAlgorithmException {
        return getShaEncrypt(str, 256, false);
    }

    /**
     * <pre>
     *     SHA-256 암호화 : salt 사용
     * </pre>
     *
     * @param str 암호화 대상 문자열
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static final String get256EncryptWithSalt(String str) throws NoSuchAlgorithmException {
        return getShaEncrypt(str, 256, true);
    }

    /**
     * <pre>
     *     SHA-512 암호화
     * </pre>
     *
     * @param str 암호화 대상 문자열
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static final String get512Encrypt(String str) throws NoSuchAlgorithmException {
        return getShaEncrypt(str, 512, false);
    }

    /**
     * <pre>
     *     SHA-512 암호화 : salt 사용
     * </pre>
     *
     * @param str 암호화 대상 문자열
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static final String get512EncryptWithSalt(String str) throws NoSuchAlgorithmException {
        return getShaEncrypt(str, 512, true);
    }


    @Getter
    @AllArgsConstructor
    private enum TYPE_FORMAT {
        SHA_256("SHA-256", "%02x"),
        SHA_512("SHA-512", "%0128x");

        final String type;
        final String format;
    }

}
