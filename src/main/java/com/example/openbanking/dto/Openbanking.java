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
    public static class AuthTokenResponse {
        private String accessToken;
        private String tokenType;
        private String expiresIn;
        private String refreshToken;
        private String scope;
        private String userSeqNo;
        private String rspCode;
        private String rspMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        private String client_id;
        private String client_secret;
        private String refresh_token;
        private String scope;
        private String grant_type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
//    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RefreshTokenResponse {
        private String access_token;
        private String token_type;
        private Integer expires_in;
        private String refresh_token;
        private String scope;
        private String user_seq_no;
        private String rsp_code;
        private String rsp_message;
    }
}
