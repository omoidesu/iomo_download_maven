package com.omoi.iomo_download.exception;

/**
 * @author omoi
 * @date 2024/1/11
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}