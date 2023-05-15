package com.example.openbanking.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(CustomResponseStatusException.class)
    public ResponseEntity<ExceptionResponse> handleException(CustomResponseStatusException e) {
        return ExceptionResponse.toResponseEntity(e.getExceptionCode(), e.getMessage());
    }
}
