package com.flashandroid.sdk.misc.exceptions;

/**
 * Throws exception when there is any error from the server. For example, wrong username,
 * userID, password , hostname etc. These exceptions are thrown from the MDDI backend
 */
public class ServerException extends Exception {

    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
