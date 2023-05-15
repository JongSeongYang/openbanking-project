package com.example.openbanking.exception;

import com.example.openbanking.utils.UtcZoneDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Builder
public class ExceptionResponse {

    private final int code;
    private final String codeName;
    private final String message;
    @JsonSerialize(using = UtcZoneDateTimeSerializer.class)
    private final LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);

    public static ResponseEntity<ExceptionResponse> toResponseEntity(ExceptionCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ExceptionResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage()+message)
                        .codeName(errorCode.name())
                        .build()
                );
    }

}
