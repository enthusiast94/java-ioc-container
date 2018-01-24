package com.enthusiast94.ioc;

public class IocException extends RuntimeException {

    public IocException() {
        super();
    }

    public IocException(String message) {
        super(message);
    }

    public IocException(Throwable cause) {
        super(cause);
    }
}
