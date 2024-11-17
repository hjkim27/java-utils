package com.hjkim27.util.otp;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GoogleOTPUtil {

    private static final String HmacSHA1 = "HmacSHA1";

    private static final String format2 = "http://chart.apis.google.com/chart?cht=qr&chs=200x200&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s&chld=H|0";

    /**
     * <PRE>
     * otp key 생성
     * </PRE>
     *
     * @param userName 사용자 이름
     * @param hostName host 이름
     * @return HashMap&lt;String, String&gt;
     * map.put("encodedKey", encodedKey);
     * map.put("url", url);
     * @throws InvalidKeyException
     */
    public static final HashMap<String, String> generate(final String userName, final String hostName) throws InvalidKeyException {

        if (userName == null) {
            throw new InvalidKeyException("userName is null");
        }
        if (hostName == null) {
            throw new InvalidKeyException("hostName is null");
        }

        HashMap<String, String> map = new HashMap<String, String>();
        byte[] buffer = new byte[5 + 5 * 5];
        new Random().nextBytes(buffer);
        Base32 codec = new Base32();
        byte[] secretKey = Arrays.copyOf(buffer, 10);
        byte[] bEncodedKey = codec.encode(secretKey);
        String encodedKey = new String(bEncodedKey);
        String url = getQRBarcodeURL(userName, hostName, encodedKey);
        map.put("encodedKey", encodedKey);
        map.put("url", url);
        return map;
    }

    /**
     * <PRE>
     *
     * </PRE>
     *
     * @param userCode 비교할 입력값
     * @param otpKey   otp 토큰 (must save secure)
     * @param window   앞뒤로 몇회차까지 허용할지
     * @return boolean   if true : 올바른 otp
     */
    public static final boolean checkCode(final String userCode, final String otpKey, final int window) {
        boolean result = false;
        try {
            if (userCode == null) {
                throw new InvalidKeyException("userName is null");
            }
            if (otpKey == null) {
                throw new InvalidKeyException("hostName is null");
            }

            long otpnum = Integer.parseInt(userCode);
            long wave = new Date().getTime() / 30000;
            Base32 codec = new Base32();
            byte[] decodedKey = codec.decode(otpKey);
            for (int i = -window; i <= window; ++i) {
                long hash = verify_code(decodedKey, wave + i);

                if (hash == otpnum) {
                    result = true;
                }
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * <PRE>
     * 입력값이 올바른 값인지 검증
     * 앞뒤 3회차까지 허용
     * (변경시 3rd param  int window 추가)
     * </PRE>
     *
     * @param userCode 비교할 입력값
     * @param otpKey   otp 토큰 (must save secure)
     * @return boolean   if true : 올바른 otp
     */
    public static final boolean checkCode(final String userCode, final String otpKey) {
        return checkCode(userCode, otpKey, 3);
    }

    private static int verify_code(final byte[] key, final long t) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(key, HmacSHA1);
        Mac mac = Mac.getInstance(HmacSHA1);
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }

    public static final String getQRBarcodeURL(final String user, final String host, final String secret) {
        return String.format(format2, user, host, secret);
    }

}

