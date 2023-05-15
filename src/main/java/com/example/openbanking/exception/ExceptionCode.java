package com.example.openbanking.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.OK;

@Getter
@AllArgsConstructor
public enum ExceptionCode {

    /* 10000  */
    EMAIL_NOT_FOUND(10001,"이메일이 존재하지 않습니다."),

    /* 500 internal server error */
    INTERNAL_ERROR(500, "Internal server error")
    ;

    private final int code;
    private final String message;
    private final HttpStatus status = OK;
}
