package com.hjkim27.util.mail.exception;

/**
 * @author hjkim27
 * @since 0.0.1-SNAPSHOT
 */
public class EmailSendException extends Exception {


    public EmailSendException() {
    }

    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailSendException(Throwable cause) {
        super(cause);
    }

    public EmailSendException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
