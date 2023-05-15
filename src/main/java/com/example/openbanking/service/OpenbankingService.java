package com.example.openbanking.service;

import com.example.openbanking.config.AppConfig;
import com.example.openbanking.dto.Openbanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    public Openbanking.AuthTokenResponse getToken(Openbanking.AuthTokenRequest request){
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        if(null == request.getCode()){
            paramMap.add("code", CODE);
        } else {
            paramMap.add("code", request.getCode());
        }

        if(null == request.getRedirectUri()){
            paramMap.add("redirect_uri", "http://localhost:8081");
        } else {
            paramMap.add("redirect_uri", request.getRedirectUri());
        }

        paramMap.add("client_id", CLIENT_ID);
        paramMap.add("client_secret", CLIENT_SECRET);
        paramMap.add("grant_type", "authorization_code");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        HttpEntity<MultiValueMap<String, String>> param=
                new HttpEntity<MultiValueMap<String,String>>(paramMap,httpHeaders);
        String requestUrl="https://testapi.openbanking.or.kr/oauth/2.0/token";
        ResponseEntity<Openbanking.AuthTokenResponse> exchange = restTemplate.exchange(requestUrl, HttpMethod.POST, param, Openbanking.AuthTokenResponse.class);
        log.info(exchange.getBody().toString());
        return exchange.getBody();
//        try {
//            response = restTemplate.exchange(requestUrl, HttpMethod.POST, param, Openbanking.AuthTokenResponse.class).getBody();
//        } catch(Exception e){
//            log.info(e.getMessage());
//        }
    }
}
