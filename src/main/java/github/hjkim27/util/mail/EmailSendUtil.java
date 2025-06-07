package github.hjkim27.util.mail;

import github.hjkim27.util.mail.bean.EmailSender;
import github.hjkim27.exception.EmailSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * <pre>
 *     이메일 발송 Util 클래스
 * </pre>
 *
 * @author hjkim27
 * @since 0.0.1-SNAPSHOT
 */
public class EmailSendUtil {

    private final Logger log = LoggerFactory.getLogger("UTILS");

    /**
     * <pre>
     *     이메일 발송(인증X)
     * </pre>
     *
     * @param smtpUser         메일계정 ID
     * @param smtpPassword     메일계정 password
     * @param smtpHost         메일발송 host
     * @param smtpPort         메일서버 port
     * @param recipientTypeTO  수신 이메일 목록
     * @param recipientTypeCC  참조 이메일 목록
     * @param recipientTypeBCC 숨은참조 이메일 목록
     * @param title            제목
     * @param body             본문
     * @param files            첨부파일 목록
     * @param senderAddress    송신 email 주소
     * @throws EmailSendException
     */
    public static void sendMultipartMailwithoutAuth(
            final String smtpUser,
            final String smtpPassword,
            final String smtpHost,
            final int smtpPort,
            Set<String> recipientTypeTO,
            Set<String> recipientTypeCC,
            Set<String> recipientTypeBCC,
            String title,
            String body,
            List<File> files,
            String senderAddress
    ) throws EmailSendException {
        sendMultipartMail(smtpUser, smtpPassword, smtpHost, smtpPort, false, false, false, recipientTypeTO,
                recipientTypeCC, recipientTypeBCC, title, body, files, senderAddress);
    }

    /**
     * <pre>
     *     이메일 발송(ssl 인증 사용)
     * </pre>
     *
     * @param smtpUser         메일계정 ID
     * @param smtpPassword     메일계정 password
     * @param smtpHost         메일발송 host
     * @param smtpPort         메일서버 port
     * @param recipientTypeTO  수신 이메일 목록
     * @param recipientTypeCC  참조 이메일 목록
     * @param recipientTypeBCC 숨은참조 이메일 목록
     * @param title            제목
     * @param body             본문
     * @param files            첨부파일 목록
     * @param senderAddress    송신 email 주소* @param smtpUser
     * @throws EmailSendException
     */
    public static void sendMultipartMailwithSSL(
            final String smtpUser,
            final String smtpPassword,
            final String smtpHost,
            final int smtpPort,
            Set<String> recipientTypeTO,
            Set<String> recipientTypeCC,
            Set<String> recipientTypeBCC,
            String title,
            String body,
            List<File> files,
            String senderAddress
    ) throws EmailSendException {
        sendMultipartMail(smtpUser, smtpPassword, smtpHost, smtpPort, true, true, false, recipientTypeTO,
                recipientTypeCC, recipientTypeBCC, title, body, files, senderAddress);
    }


    /**
     * <pre>
     *     이메일 발송(tls 인증 사용)
     * </pre>
     *
     * @param smtpUser         메일계정 ID
     * @param smtpPassword     메일계정 password
     * @param smtpHost         메일발송 host
     * @param smtpPort         메일서버 port
     * @param recipientTypeTO  수신 이메일 목록
     * @param recipientTypeCC  참조 이메일 목록
     * @param recipientTypeBCC 숨은참조 이메일 목록
     * @param title            제목
     * @param body             본문
     * @param files            첨부파일 목록
     * @param senderAddress    송신 email 주소* @param smtpUser
     * @throws EmailSendException
     */
    public static void sendMultipartMailwithTLS(
            final String smtpUser,
            final String smtpPassword,
            final String smtpHost,
            final int smtpPort,
            Set<String> recipientTypeTO,
            Set<String> recipientTypeCC,
            Set<String> recipientTypeBCC,
            String title,
            String body,
            List<File> files,
            String senderAddress
    ) throws EmailSendException {
        sendMultipartMail(smtpUser, smtpPassword, smtpHost, smtpPort, true, false, true, recipientTypeTO,
                recipientTypeCC, recipientTypeBCC, title, body, files, senderAddress);
    }

    private static void sendMultipartMail(
            final String smtpUser,
            final String smtpPassword,
            final String smtpHost,
            final int smtpPort,
            final Boolean authEnable,
            final Boolean sslEnable,
            final Boolean tlsEnable,
            Set<String> recipientTypeTO,
            Set<String> recipientTypeCC,
            Set<String> recipientTypeBCC,
            String title,
            String body,
            List<File> files,
            String senderAddress
    ) throws EmailSendException {
        EmailSender emailSender = EmailSenderFactory.createSimpleSmtp(sslEnable, tlsEnable);

        emailSender.setSmtpHost(smtpHost);
        emailSender.setSmtpUser(smtpUser);
        emailSender.setSmtpPassword(smtpPassword);
        emailSender.setSmtpPort(smtpPort);

        emailSender.setTitle(title);
        emailSender.setBody(body);

        if (senderAddress != null) {
            emailSender.setSenderAddress(senderAddress);
        }
        if (authEnable != null) {
            emailSender.setAuthEnable(authEnable);
        }

        if (recipientTypeTO != null) {
            emailSender.setRecipientTypeTO(recipientTypeTO);
        }
        if (recipientTypeCC != null) {
            emailSender.setRecipientTypeCC(recipientTypeCC);
        }
        if (recipientTypeBCC != null) {
            emailSender.setRecipientTypeBCC(recipientTypeBCC);
        }
        if (files != null) {
            emailSender.setSendFiles(files);
        }
        emailSender.send();
    }
}
