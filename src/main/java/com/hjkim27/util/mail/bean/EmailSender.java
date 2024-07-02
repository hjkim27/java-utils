package com.hjkim27.util.mail.bean;


import com.hjkim27.util.mail.exception.EmailSendException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * @author hjkim27
 * @since 0.0.1-SNAPSHOT
 */
@Getter
@Builder
public class EmailSender {

    private final Logger log = LoggerFactory.getLogger("UTILS");

    private final String ENCODE = StandardCharsets.UTF_8.name();

    /**
     * mailServer default port
     */
    public final static int DEFAULT_PORT = 25;
    /**
     * SSL default protocol
     */
    public final static int DEFAULT_SSL_PORT = 465;
    /**
     * TLS default protocol
     */
    public final static int DEFAULT_TLS_PORT = 587;


    // 메일계정(ID)
    @Setter
    private String smtpUser;
    // 메일계정(비밀번호)
    @Setter
    private String smtpPassword;
    // 메일계정(host)
    @Setter
    private String smtpHost;
    // 메일서버(port)
    @Setter
    private int smtpPort = DEFAULT_PORT;

    // Authentication 사용여부
    @Setter
    private Boolean authEnable = false;
    // ssl 사용여부
    private Boolean sslEnable = false;
    // ssl 버전
    @Setter
    private String sslVersion;
    // tls 사용여부
    private Boolean tlsEnable = false;
    // tls 버전
    @Setter
    private String tlsVersion;

    // 수신자 목록
    @Builder.Default
    private Set<String> recipientTypeTO = new HashSet<>();
    // 참조 목록
    @Builder.Default
    private Set<String> recipientTypeCC = new HashSet<>();
    // 숨은참조 목록
    @Builder.Default
    private Set<String> recipientTypeBCC = new HashSet<>();

    // 제목
    @Setter
    private String title = "";
    // 본문
    @Setter
    private String body = "";
    // 첨부파일 목록
    @Builder.Default
    private List<File> sendFiles = new ArrayList<>();
    // 송신 mail 주소
    @Setter
    private String senderAddress = "";


    /**
     * parameter null check
     */
    public void validate() {
        if (StringUtils.isBlank(title)) {
            throw new InvalidParameterException("title is null or empty");
        }
        if (StringUtils.isBlank(smtpUser)) {
            throw new InvalidParameterException("smtpUser is null or empty");
        }
        if (StringUtils.isBlank(smtpHost)) {
            throw new InvalidParameterException("smtpHost is null or empty");
        }
        if (StringUtils.isBlank(body)) {
            log.warn("mail body is null ... set empty body");
            body = "";
        }
        if (recipientTypeTO.isEmpty()) {
            throw new InvalidParameterException("empty TO email ...");
        }

        final boolean emptyPasswd = StringUtils.isBlank(smtpPassword);
        if (authEnable && emptyPasswd) {
            throw new InvalidParameterException("smtpPassword is null or empty");
        } else if (!authEnable && !emptyPasswd) {
            log.warn("enter password and (authUsed == false) ... might be invalid setting");
        }

        if (sslEnable && tlsEnable) {
            log.warn("both ssl and tls enabled ... might be invalid setting, will take ssl settings precedence");
        }
    }

    /**
     * <pre>
     *     이메일 발송
     * </pre>
     *
     * @throws EmailSendException
     */
    public void send() throws EmailSendException {
        try {
            validate();

            Properties prop = new Properties();

            if (authEnable) {
                prop.put("mail.smtp.auth", "true");
            }
            if (sslEnable) {
                prop.put("mail.smtp.ssl.enable", "true");
                if (sslVersion != null) {
                    prop.put("mail.smtp.ssl.protocol", sslVersion);
                }
            } else if (tlsEnable) {
                prop.put("mail.smtp.starttls.enable", "true");
                if (tlsVersion != null) {
                    prop.put("mail.smtp.ssl.protocol", tlsVersion);
                }
            }
            prop.put("mail.smtp.host", smtpHost);
            prop.put("mail.smtp.port", smtpPort);

            Session session = null;
            if (StringUtils.isEmpty(smtpUser) || StringUtils.isEmpty(smtpPassword)) {
                session = Session.getInstance(prop);
            } else {
                session = Session.getInstance(prop, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUser, smtpPassword);
                    }
                });
            }

            MimeMessage message = new MimeMessage(session);
            if (senderAddress.isEmpty()) {
                if (!smtpUser.isEmpty()) {
                    message.setFrom(new InternetAddress(smtpUser));
                }
            } else {
                message.setFrom(new InternetAddress(senderAddress));
            }

            // 수신
            if (recipientTypeTO != null && !recipientTypeTO.isEmpty()) {
                int toCount = 0;
                InternetAddress[] toInternetAddresses = new InternetAddress[recipientTypeTO.size()];
                for (String addr : recipientTypeTO) {
                    InternetAddress tmp = new InternetAddress(addr);
                    toInternetAddresses[toCount++] = tmp;
                }
                message.addRecipients(Message.RecipientType.TO, toInternetAddresses);
            }

            // 참조
            if (recipientTypeCC != null && !recipientTypeCC.isEmpty()) {
                int ccCount = 0;
                InternetAddress[] ccInternetAddresses = new InternetAddress[recipientTypeCC.size()];
                for (String addr : recipientTypeCC) {
                    InternetAddress tmp = new InternetAddress(addr);
                    ccInternetAddresses[ccCount++] = tmp;
                }
                message.addRecipients(Message.RecipientType.CC, ccInternetAddresses);
            }

            // 숨은참조
            if (recipientTypeBCC != null && !recipientTypeBCC.isEmpty()) {
                int bccCount = 0;
                InternetAddress[] bccInternetAddresses = new InternetAddress[recipientTypeBCC.size()];
                for (String addr : recipientTypeBCC) {
                    InternetAddress tmp = new InternetAddress(addr);
                    bccInternetAddresses[bccCount++] = tmp;
                }
                message.addRecipients(Message.RecipientType.BCC, bccInternetAddresses);
            }

            message.setSubject(title, ENCODE);
            message.setText(body, ENCODE, "html");

            // 첨부파일
            if (!sendFiles.isEmpty()) {
                Multipart multipart = new MimeMultipart();
                for (File file : sendFiles) {
                    if (!file.exists()) {
                        throw new FileNotFoundException(file.getAbsolutePath());
                    }
                    String htmlBody = "";
                    MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent(htmlBody, "text/html");
                    multipart.addBodyPart(htmlPart);

                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    DataSource dataSource = new FileDataSource(file.getAbsolutePath());
                    mimeBodyPart.setDataHandler(new DataHandler(dataSource));
                    mimeBodyPart.setFileName(file.getName());

                    multipart.addBodyPart(mimeBodyPart);
                }
                message.setContent(multipart);
            }

            Transport.send(message);

        } catch (FileNotFoundException e) {
            log.error("fail attachFile ... file not found : {}", e.getMessage());
            throw new EmailSendException("attach file not found " + e.getMessage() + " || " + e);
        } catch (Exception e) {
            log.error("fail emailSend ... title : {} | {}", title, e);
            throw new EmailSendException(e);
        }
    }

    public void setRecipientTypeTO(Set<String> recipientTypeTO) {
        recipientTypeTO.addAll(recipientTypeTO);
    }

    public void setRecipientTypeCC(Set<String> recipientTypeCC) {
        recipientTypeCC.addAll(recipientTypeCC);
    }

    public void setRecipientTypeBCC(Set<String> recipientTypeBCC) {
        recipientTypeBCC.addAll(recipientTypeBCC);
    }

    public void setSendFiles(List<File> files) {
        files.addAll(files);
    }
}
