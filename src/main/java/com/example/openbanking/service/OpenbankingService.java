package com.example.openbanking.service;

import com.example.openbanking.config.AppConfig;
import com.example.openbanking.dto.Openbanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenbankingService {

    private final AppConfig appConfig;

    @Value("${openbanking.auth-code}")
    String CODE;

    @Value("${openbanking.client-id}")
    String CLIENT_ID;

    @Value("${openbanking.secret-id}")
    String CLIENT_SECRET;

    public Openbanking.AuthTokenResponse getToken(String code){
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("code", code);
        paramMap.add("redirect_uri", "http://localhost:8081/api/v1/auth/code");
        paramMap.add("client_id", CLIENT_ID);
        paramMap.add("client_secret", CLIENT_SECRET);
        paramMap.add("grant_type", "authorization_code");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        HttpEntity<MultiValueMap<String, String>> param=
                new HttpEntity<MultiValueMap<String,String>>(paramMap,httpHeaders);
        String requestUrl="https://testapi.openbanking.or.kr/oauth/2.0/token";
        ResponseEntity<Openbanking.AuthTokenResponse> exchange = restTemplate.postForEntity(requestUrl, param, Openbanking.AuthTokenResponse.class);
        return exchange.getBody();
//        try {
//            response = restTemplate.exchange(requestUrl, HttpMethod.POST, param, Openbanking.AuthTokenResponse.class).getBody();
//        } catch(Exception e){
//            log.info(e.getMessage());
//        }
    }

    public Openbanking.RefreshTokenResponse getRefreshToken(){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("client_id", CLIENT_ID);
        parameters.add("client_secret", CLIENT_SECRET);
        parameters.add("scope", "login inquiry transfer");
        parameters.add("refresh_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAxMDMyMzMwIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2OTI4NzMyMjEsImp0aSI6ImQ1YzJiYjY5LTMyZTAtNDU0Zi05NWQ0LTI0Mzk1ZmNkY2RkNSJ9.5T4fDDS1Fw3PeFXyZ5_WNMixBzzGx4tb5cwxO7IoIgM");
        parameters.add("grant_type", "refresh_token");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> param=
                new HttpEntity<MultiValueMap<String,String>>(parameters,httpHeaders);
        String requestUrl="https://testapi.openbanking.or.kr/oauth/2.0/token";
        ResponseEntity<Openbanking.RefreshTokenResponse> exchange = restTemplate.postForEntity(requestUrl, param, Openbanking.RefreshTokenResponse.class);
//        Openbanking.AuthTokenResponse map = appConfig.toCamelCaseMapper().map(exchange.getBody(), Openbanking.AuthTokenResponse.class);
        log.info(exchange.toString());
        return exchange.getBody();
    }
}
