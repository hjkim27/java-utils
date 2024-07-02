package com.hjkim27.util.mail;

import com.hjkim27.util.mail.bean.EmailSender;
import com.hjkim27.util.mail.exception.EmailSendException;

/**
 * @author hjkim27
 * @since 0.0.1-SNAPSHOT
 */
public class EmailSenderFactory {

    /**
     * <pre>
     *     EmailSender 인증관련 세팅
     * </pre>
     *
     * @param sslEnable ssl 사용여부
     * @param tlsEnable tls 사용여부
     * @return
     * @throws EmailSendException
     */
    public static final EmailSender createSimpleSmtp(
            final Boolean sslEnable, final Boolean tlsEnable
    ) throws EmailSendException {
        if (sslEnable && tlsEnable) {
            throw new EmailSendException("SSL and TLS can not be used same time");
        } else if (sslEnable || tlsEnable) {
            if (sslEnable) {
                return defaultSSLSmtp();
            } else {
                return defaultSTARTTLSSmtp();
            }
        } else {
            return simpleSmtpWithoutAuth();
        }
    }

    /**
     * <pre>
     *     인증 없이 이메일 발송
     * </pre>
     *
     * @return
     */
    public static final EmailSender simpleSmtpWithoutAuth() {
        return simpleSmtpWithoutAuth(EmailSender.DEFAULT_PORT);
    }

    /**
     * <pre>
     *     인증 없이 이메일 발송, 메일서버 포트 변경
     * </pre>
     *
     * @param port 메일서버 port
     * @return
     */
    public static final EmailSender simpleSmtpWithoutAuth(final int port) {
        return EmailSender.builder()
                .smtpPort(port)
                .authEnable(false).sslEnable(false).tlsEnable(false)
                .build();
    }

    /**
     * <pre>
     *     이메일발송 ssl 인증 사용
     * </pre>
     *
     * @return
     */
    public static final EmailSender defaultSSLSmtp() {
        return defaultSSLSmtp(EmailSender.DEFAULT_SSL_PORT);
    }

    /**
     * <pre>
     *     이메일발송 ssl 인증 사용, 메일서버 포트 변경
     * </pre>
     *
     * @param port 메일서버 port
     * @return
     */
    public static final EmailSender defaultSSLSmtp(final int port) {
        return EmailSender.builder()
                .smtpPort(port)
                .authEnable(true).sslEnable(true).tlsEnable(false)
                .build();
    }

    /**
     * <pre>
     *     이메일발송 tls 인증 사용
     * </pre>
     *
     * @return
     */
    public static final EmailSender defaultSTARTTLSSmtp() {
        return defaultSTARTTLSSmtp(EmailSender.DEFAULT_TLS_PORT);
    }

    /**
     * <pre>
     *     이메일발송 ssl 인증 사용, 메일서버 포트 변경
     * </pre>
     *
     * @param port 메일서버 port
     * @return
     */
    public static final EmailSender defaultSTARTTLSSmtp(final int port) {
        return EmailSender.builder()
                .smtpPort(port)
                .authEnable(true).sslEnable(false).tlsEnable(true)
                .build();
    }

}
