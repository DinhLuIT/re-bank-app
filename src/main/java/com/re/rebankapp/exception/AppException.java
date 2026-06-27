package com.re.rebankapp.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ResponseCode responseCode;

    public AppException(ResponseCode responseCode) {
        super(responseCode.getMessage());

        this.responseCode = responseCode;
    }
}
