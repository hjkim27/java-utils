package com.hjkim27.util.otp;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GoogleOTPUtil {

    private static String ISSUER = "issuer";

    public static void setISSUER(String ISSUER) {
        GoogleOTPUtil.ISSUER = ISSUER;
    }

    public static Map<String, String> generate(String otpKey, String username) {
        Map<String, String> map = new HashMap<>();
        String url = generateAuthenticatorURL(otpKey, username);
        map.put("url", url);
        map.put("qrcode", getQRBase64(url));
        return map;
    }

    /**
     * <pre>
     * otp 비밀 키 생성
     * </pre>
     *
     * @return otp 비밀키
     */
    public static String generateKey() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    /**
     * <pre>
     * 인증을 위한 url 생성
     * </pre>
     *
     * @param otpKey   otp 비밀키
     * @param username 사용자이름
     * @return 인증 url
     */
    public static String generateAuthenticatorURL(String otpKey, String username) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                ISSUER,
                username,
                new GoogleAuthenticatorKey.Builder(otpKey).build()
        );
    }

    /**
     * <pre>
     * QRCode 이미지 String 생성
     * </pre>
     *
     * @param authenticatorURL 인증 url
     * @return 인증 url 로 생성한 QRCode Image base64
     */
    public static String getQRBase64(String authenticatorURL) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            BitMatrix bitMatrix = qrCodeWriter.encode(authenticatorURL, BarcodeFormat.QR_CODE, 400, 400);
            MatrixToImageWriter.writeToStream(bitMatrix, "png", bout);
            return Base64.getEncoder().encodeToString(bout.toByteArray());
        } catch (IOException | WriterException e) {
            log.error("Occurred during generate qrcode!!!!");
            throw new RuntimeException(e);
        }
    }

    /**
     * <PRE>
     * 저장된 otpkey, 입력받은 code 비교해서 검증
     * - window는 앞뒤 비교 count
     * </PRE>
     *
     * @param code   otp 인증 코드
     * @param otpkey 비밀키
     * @param window
     * @return
     */
    public static boolean checkCode(String code, String otpkey, int window) {
        boolean result = false;
        try {
            long otpnum = Integer.parseInt(code);
            long wave = new Date().getTime() / 30000;
            Base32 codec = new Base32();
            byte[] decodedKey = codec.decode(otpkey);
            // int window = 3;
            for (int i = -window; i <= window; ++i) {
                long hash = verify_code(decodedKey, wave + i);

                if (hash == otpnum) {
                    result = true;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }

    public static boolean checkCode(String code, String otpkey) {
        return checkCode(code, otpkey, 3);
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static int verify_code(byte[] key, long t)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
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

}

