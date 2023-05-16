package com.example.openbanking.controller;

import com.example.openbanking.dto.Openbanking;
import com.example.openbanking.service.OpenbankingService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OpenbankingController {

    private final OpenbankingService openbankingService;

    @ApiOperation(value = "코드발급", notes = "코드발급")
    @GetMapping(value = "/code", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Openbanking.AuthTokenResponse> getCode(@RequestParam("code") String code,
                                                                 @RequestParam("scope") String scope,
                                                                 @RequestParam("state") String state) {
        Openbanking.AuthTokenResponse response = openbankingService.getToken(code);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "토큰발급", notes = "토큰발급")
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Openbanking.AuthTokenResponse> getToken(@RequestParam("code") String code) {

        Openbanking.AuthTokenResponse response = openbankingService.getToken(code);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "토큰재발급", notes = "토큰재발급")
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Openbanking.RefreshTokenResponse> getRefreshToken(HttpServletRequest request) {

        Openbanking.RefreshTokenResponse response = openbankingService.getRefreshToken();
        return ResponseEntity.ok(response);
    }
}
