package com.enthusiast94.ioc.exceptions;

public class IocException extends RuntimeException {

    public IocException(String message) {
        super(message);
    }

    public IocException(Throwable cause) {
        super(cause);
    }
}
