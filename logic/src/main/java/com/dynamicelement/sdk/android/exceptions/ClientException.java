package com.dynamicelement.sdk.android.exceptions;

/**
 * Throws exception when there is any error from Client. For example, providing the
 * no or empty parameters , wrong instance type etc.
 */
public class ClientException extends Exception {

    public ClientException(String message) {
        super(message);
    }
}
