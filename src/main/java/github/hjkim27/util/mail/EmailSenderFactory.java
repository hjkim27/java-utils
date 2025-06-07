package github.hjkim27.util.mail;

import github.hjkim27.util.mail.bean.EmailSender;
import github.hjkim27.exception.EmailSendException;

/**
 * <pre>
 *     이메일 발송을 위한 {@link EmailSender} 의 인증 사용여부 관련 정보를 세팅하는 클래스
 * </pre>
 *
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
     * @return {@link EmailSender}
     * @throws EmailSendException
     */
    public static EmailSender createSimpleSmtp(
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
     * @return {@link EmailSender}
     */
    public static EmailSender simpleSmtpWithoutAuth() {
        return simpleSmtpWithoutAuth(EmailSender.DEFAULT_PORT);
    }

    /**
     * <pre>
     *     인증 없이 이메일 발송, 메일서버 포트 변경
     * </pre>
     *
     * @param port 메일서버 port
     * @return {@link EmailSender}
     */
    public static EmailSender simpleSmtpWithoutAuth(final int port) {
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
     * @return {@link EmailSender}
     */
    public static EmailSender defaultSSLSmtp() {
        return defaultSSLSmtp(EmailSender.DEFAULT_SSL_PORT);
    }

    /**
     * <pre>
     *     이메일발송 ssl 인증 사용, 메일서버 포트 변경
     * </pre>
     *
     * @param port 메일서버 port
     * @return {@link EmailSender}
     */
    public static EmailSender defaultSSLSmtp(final int port) {
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
     * @return {@link EmailSender}
     */
    public static EmailSender defaultSTARTTLSSmtp() {
        return defaultSTARTTLSSmtp(EmailSender.DEFAULT_TLS_PORT);
    }

    /**
     * <pre>
     *     이메일발송 ssl 인증 사용, 메일서버 포트 변경
     * </pre>
     *
     * @param port 메일서버 port
     * @return {@link EmailSender}
     */
    public static EmailSender defaultSTARTTLSSmtp(final int port) {
        return EmailSender.builder()
                .smtpPort(port)
                .authEnable(true).sslEnable(false).tlsEnable(true)
                .build();
    }

}
