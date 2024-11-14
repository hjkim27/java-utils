package com.hjkim27.util.http;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
public class SSLCertCheckUtils {

    static final String https = "https://";

    static final String exMsg = "ssl check exception :: ";

    public static CertInfo sslCheck(final String host) throws CertificateException {
        return sslCheck(host, 443);
    }

    public static CertInfo sslCheck(final String host, final int port) throws CertificateException {

        try {
            if (host == null || host.isEmpty()) {
                throw new InvalidParameterException(exMsg + "host is null");
            }

            final String url;

            if (host.startsWith(https)) {
                url = host;
            } else {
                url = https + host;
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager passthroughTrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[]{passthroughTrustManager}, null);

            SSLSocketFactory ssf = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) ssf.createSocket(host, port);
            socket.startHandshake();

            X509Certificate[] peerCertificates = (X509Certificate[]) socket.getSession().getPeerCertificates();

            // By default on Oracle JRE, algorithm is PKIX
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // 'null' will initialise the tmf with the default CA certs installed
            // with the JRE.
            tmf.init((KeyStore) null);
            X509TrustManager tm = (X509TrustManager) tmf.getTrustManagers()[0];

            // Assuming RSA key here.
            tm.checkServerTrusted(peerCertificates, "RSA");

            X509Certificate serverCert = peerCertificates[0];

            Date beforeDate = serverCert.getNotBefore();
            Date afterDate = serverCert.getNotAfter();


            CertInfo certInfo = new CertInfo();

            TimeZone tz = TimeZone.getDefault();

            Calendar beforeCal = Calendar.getInstance(tz);
            Calendar afterCal = Calendar.getInstance(tz);

            beforeCal.setTime(beforeDate);
            afterCal.setTime(afterDate);

            certInfo.setBefore(beforeCal);
            certInfo.setAfter(afterCal);


            int var1 = serverCert.getBasicConstraints();
            certInfo.setBasicConstraints(var1);


            int var2 = serverCert.getVersion();
            certInfo.setVersion(var2);


            List<String> var3 = serverCert.getExtendedKeyUsage();
            certInfo.setExtendedKeyUsage(var3);


            BigInteger var5 = serverCert.getSerialNumber();
            certInfo.setSerialNumber(var5);


            String var6 = serverCert.getSigAlgName();
            certInfo.setSigAlgName(var6);


            String var7 = serverCert.getSigAlgOID();
            certInfo.setSigAlgOID(var7);


            byte[] var8 = serverCert.getSignature();
            certInfo.setSignature(var8);


            String var9 = serverCert.getType();
            certInfo.setType(var9);


            Principal var10 = serverCert.getSubjectDN();
            certInfo.setSubjectDN(var10.getName());


            X500Principal var11 = serverCert.getIssuerX500Principal();
            certInfo.setIssuerX500PrincipalName(var11.getName());
            certInfo.setIssuerX500Principal(var11.toString());


            X500Principal var12 = serverCert.getSubjectX500Principal();
            certInfo.setSubjectX500PrincipalName(var12.getName());
            certInfo.setSubjectX500Principal(var12.toString());


            return certInfo;

        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            // Here you may check which subclass of CertificateException to know what the error is.
            log.error(e.getMessage(), e);
            throw new CertificateException(e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CertificateException(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CertificateException(e);
        }
    }

    @Getter
    @Setter
    public static class CertInfo {
        private int basicConstraints = -1;
        private int version = -1;
        private List<String> extendedKeyUsage = new ArrayList<>();
        private BigInteger serialNumber = BigInteger.valueOf(-1);
        private String sigAlgName = "";
        private String sigAlgOID = "";
        private byte[] signature = new byte[1];
        private String type = "";
        private Calendar before = null;
        private Calendar after = null;
        private String subjectDN = "";
        private String issuerX500PrincipalName = "";
        private String issuerX500Principal = "";
        private String subjectX500PrincipalName = "";
        private String subjectX500Principal = "";


    }


}
