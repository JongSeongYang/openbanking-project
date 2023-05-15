package com.example.openbanking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Openbanking {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AuthCodeRequest {
        private String code;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String grantType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AuthCodeResponse {
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AuthTokenRequest {
        private String code;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String grantType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
//    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AuthTokenResponse {
        @JsonProperty(value = "access_token")
        private String accessToken;
        private String tokenType;
        private String expiresIn;
        private String refreshToken;
        private String scope;
        private String userSeqNo;
    }


}
