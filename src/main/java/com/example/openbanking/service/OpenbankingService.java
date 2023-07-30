package com.example.openbanking.service;

import com.example.openbanking.config.AppConfig;
import com.example.openbanking.domain.AccountEntity;
import com.example.openbanking.domain.MemberEntity;
import com.example.openbanking.domain.OpenBankingTransHistory;
import com.example.openbanking.domain.OpenBankingUsingAccountEntity;
import com.example.openbanking.dto.Openbanking;
import com.example.openbanking.exception.CustomResponseStatusException;
import com.example.openbanking.repository.AccountRepository;
import com.example.openbanking.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenbankingService {

    private final AppConfig appConfig;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final OpenBankingUsingAccountRepository openBankingUsingAccountRepository;
    private final OpenBankingTransHistoryRepository openBankingTransHistoryRepository;

    @Value("${openbanking.auth-code}")
    String CODE;

    @Value("${openbanking.client-id}")
    String CLIENT_ID;

    @Value("${openbanking.secret-id}")
    String CLIENT_SECRET;

    public Openbanking.AuthTokenResponse getToken(String code) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("code", code);
        paramMap.add("redirect_uri", "http://localhost:8081/api/v1/auth/code");
        paramMap.add("client_id", CLIENT_ID);
        paramMap.add("client_secret", CLIENT_SECRET);
        paramMap.add("grant_type", "authorization_code");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(paramMap, httpHeaders);
        String requestUrl = "https://testapi.openbanking.or.kr/oauth/2.0/token";
        ResponseEntity<Openbanking.AuthTokenResponse> exchange = restTemplate.postForEntity(requestUrl, param, Openbanking.AuthTokenResponse.class);
        return exchange.getBody();
//        try {
//            response = restTemplate.exchange(requestUrl, HttpMethod.POST, param, Openbanking.AuthTokenResponse.class).getBody();
//        } catch(Exception e){
//            log.info(e.getMessage());
//        }
    }

    public Openbanking.RefreshTokenResponse getRefreshToken() {
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

        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://testapi.openbanking.or.kr/oauth/2.0/token";
        ResponseEntity<Openbanking.RefreshTokenResponse> exchange = restTemplate.postForEntity(requestUrl, param, Openbanking.RefreshTokenResponse.class);
//        Openbanking.AuthTokenResponse map = appConfig.toCamelCaseMapper().map(exchange.getBody(), Openbanking.AuthTokenResponse.class);
        log.info(exchange.toString());
        return exchange.getBody();
    }

    public Openbanking.RegisterAccountResponse getRefreshToken(
                            Openbanking.RegisterAccountRequest request,
                            String accessToken) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출
        MemberEntity member = memberRepository.findByMemberId(memberId);
        // 1. 등록계좌입력받기
        // 2. 은행거래고유번호 생성
        //      2-1. 은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // 3. 조회 서비스 등록
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);
        OpenBankingUsingAccountEntity openBankingUsingAccountEntity = null;
        if("Y".equals(request.getInquiryAgreeYn())) {
            // openbanking에 request 발송 및 결과 수신
            Openbanking.ApiRegisterAccountResponse result =
                    registerAccountOpenbanking(request, member, bankTranId, httpHeaders);
            // DB에 결과 저장
            openBankingUsingAccountEntity = saveOpenBankingUsingAccountEntity(result);
        }
        // 4. 출금 서비스 등록
        if("Y".equals(request.getTransferAgreeYn())) {
            // openbanking에 request 발송 및 결과 수신
            Openbanking.ApiRegisterAccountResponse result =
                    registerAccountOpenbanking(request, member, bankTranId, httpHeaders);
            // DB에 결과 저장
            if(openBankingUsingAccountEntity == null){
                openBankingUsingAccountEntity = saveOpenBankingUsingAccountEntity();
            }
            // DB에 결과 업데이트
            else {
                openBankingUsingAccountEntity.setTransferAgreeYn("Y");
                openBankingUsingAccountEntity.setAgmtDataType(result.getAgmtDataType());
                openBankingUsingAccountEntity.setOperateDt(LocalDateTime.now(ZoneOffset.UTC));
            }
        }
        // 5. Response 조립
        return Openbanking.RegisterAccountResponse.builder().result("Y").build();
    }

    @NotNull
    private Openbanking.ApiRegisterAccountResponse registerAccountOpenbanking(Openbanking.RegisterAccountRequest request,
                                                                              MemberEntity member,
                                                                              String bankTranId,
                                                                              HttpHeaders httpHeaders) {
        // body 조립
        MultiValueMap<String, String> parameters = makeBody(request, member, bankTranId);
        // http 조립
        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://openapi.openbanking.or.kr/v2.0/user/register";
        Openbanking.ApiRegisterAccountResponse result = restTemplate
                .postForEntity(requestUrl, param, Openbanking.ApiRegisterAccountResponse.class)
                .getBody();
        // error_code != Null
        if(null != result.getErrorCode()){
            throw new CustomResponseStatusException(OPENBANKING_ERROR, "메세지");
        }
        // 요청의 은행거래고유번호 != 응답의 거래고유번호(참가은행)
        if(bankTranId != result.getBankTranId()){
            throw new CustomResponseStatusException(BANK_TANS_ID_NOT_SAME, "메세지");
        }
        return result;
    }

    public String getBankTranId(String bankCode){
        Random rand = new Random();
        return "0000000"+BANK_CODE+"U"+
                Integer.toString((int)rand.nextDouble()*1000000000);
    }

    public Openbanking.AccountBalanceResponse getAccountBalane(
            Openbanking.AccountBalanceRequest request,
            String accessToken) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출
        MemberEntity member = memberRepository.findByMemberId(memberId);
        // 1. 은행거래고유번호 생성
        //    은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // 2. 계좌가 조회등록이 되어있는지 확인
        //    계좌번호, 등록기관번호, 조회동의여부, 등록해지구분코드로 조회
        //    2-1. 존재하지 않는다면 Exception
        OpenBankingUsingAccountEntity openBankingUsingAccountEntity = openBankingUsingAccountRepository.
                findOneByAccountNoAndInquiryAgreeYnAndDeleteDvsnCdAndRegisterBankCodeStd(
                        request.getAccountNo(),"Y", "N", request.getBankCodeStd)
                .orElseThrow(() -> new CustomResponseStatusException(ACCOUNT_NOT_AGREE, ""));
        // 3. 잔액 조회
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);
        // openbanking에 request 발송 및 결과 수신
        // body 조립
        MultiValueMap<String, String> parameters = makeBody(request, member, bankTranId);
        // http 조립
        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://openapi.openbanking.or.kr/v2.0/account/balance/acnt_num";
        Openbanking.ApiAccountBalanceResponse result = restTemplate
                .postForEntity(requestUrl, param, Openbanking.ApiAccountBalanceResponse.class)
                .getBody();
        // error_code != Null
        if(null != result.getErrorCode()){
            throw new CustomResponseStatusException(OPENBANKING_ERROR, "메세지");
        }
        // 요청의 은행거래고유번호 != 응답의 거래고유번호(참가은행)
        if(bankTranId != result.getBankTranId()){
            throw new CustomResponseStatusException(BANK_TANS_ID_NOT_SAME, "메세지");
        }
        // Response 조립
        return Openbanking.RegisterAccountResponse.builder()
                .result("Y")
                .accountNo(result.getAccountNo())
                .bankCodeStd(result.getBankCodeStd())
                .balanceAmt(result.getBalanceAmt())
                .availableAmt(result.getAvailableAmt())
                .accountType(result.getAccountType())
                .productName(result.getProductName())
                .build();
    }


    public Openbanking.TransactionHistoryResponse getTransactionHistory(
            Openbanking.TransactionHistoryRequest request,
            String accessToken) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출
        MemberEntity member = memberRepository.findByMemberId(memberId);
        // 1. 다음페이지 조회에 대해 일정 횟수를 초과하지 않도록 제한
        if (request.getPage() >= MAX_INQUIRY_CNT) {
            throw new CustomResponseStatusException(OVER_INQUIRY_CNT, "메세지");
        }
        // 2. 은행거래고유번호 생성
        //    은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // 3. 계좌가 조회등록이 되어있는지 확인
        //    계좌번호, 등록기관번호, 조회동의여부, 등록해지구분코드로 조회
        //    3-1. 존재하지 않는다면 Exception
        OpenBankingUsingAccountEntity openBankingUsingAccountEntity = openBankingUsingAccountRepository.
                findOneByAccountNoAndInquiryAgreeYnAndDeleteDvsnCdAndRegisterBankCodeStd(
                        request.getAccountNo(),"Y", "N", request.getBankCodeStd)
                .orElseThrow(() -> new CustomResponseStatusException(ACCOUNT_NOT_AGREE, ""));
        // 4. 거래내역 조회
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);

        String beforeInquiryTraceInfo = "";
        // openbanking에 request 발송 및 결과 수신
        // 4.1 inquiry_type(조회기준코드) == T 이면
        //     from_time(조회시작시간) 과 to_time(조회종료시간) 설정
        // 4.2 request의 befor_inquiry_trace_info != Null 이면
        //     befor_inquiry_trace_info에 request의 befor_inquiry_trace_info 설정
        // body 조립
        MultiValueMap<String, String> parameters = makeBody(request, member, bankTranId);
        // http 조립
        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://openapi.openbanking.or.kr/v2.0/account/transaction_list/acnt_num";
        Openbanking.ApiTransactionHistoryResponse result = restTemplate
                .postForEntity(requestUrl, param, Openbanking.ApiTransactionHistoryResponse.class)
                .getBody();
        // error_code != Null
        if (null != result.getErrorCode()) {
            throw new CustomResponseStatusException(OPENBANKING_ERROR, "메세지");
        }
        // 요청의 은행거래고유번호 != 응답의 거래고유번호(참가은행)
        if (bankTranId != result.getBankTranId()) {
            throw new CustomResponseStatusException(BANK_TANS_ID_NOT_SAME, "메세지");
        }
        // 응답의 다음페이지가 존재할 경우 다음페이지 수를 계산하여 반환
        int nextPage = 0;
        if (request.getNextPageYn() != "N") {
            nextPage = request.getPage()+1;
        }
        // Response 조립
        Openbanking.TransactionHistoryResponse response = makeResponse(result, nextPage);
        return response;
    }

    public Openbanking.TransactionHistoryResponse getTransactionHistory(
            Openbanking.TransactionHistoryRequest request,
            String accessToken) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출
        MemberEntity member = memberRepository.findByMemberId(memberId);
        // 1. 다음페이지 조회에 대해 일정 횟수를 초과하지 않도록 제한
        if (request.getPage() >= MAX_INQUIRY_CNT) {
            throw new CustomResponseStatusException(OVER_INQUIRY_CNT, "메세지");
        }
        // 2. 은행거래고유번호 생성
        //    은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // 3. 계좌가 조회등록이 되어있는지 확인
        //    계좌번호, 등록기관번호, 조회동의여부, 등록해지구분코드로 조회
        //    3-1. 존재하지 않는다면 Exception
        OpenBankingUsingAccountEntity openBankingUsingAccountEntity = openBankingUsingAccountRepository.
                findOneByAccountNoAndInquiryAgreeYnAndDeleteDvsnCdAndRegisterBankCodeStd(
                        request.getAccountNo(),"Y", "N", request.getBankCodeStd)
                .orElseThrow(() -> new CustomResponseStatusException(ACCOUNT_NOT_AGREE, ""));
        // 4. 거래내역 조회
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);

        String beforeInquiryTraceInfo = "";
        // openbanking에 request 발송 및 결과 수신
        // 4.1 inquiry_type(조회기준코드) == T 이면
        //     from_time(조회시작시간) 과 to_time(조회종료시간) 설정
        // 4.2 request의 befor_inquiry_trace_info != Null 이면
        //     befor_inquiry_trace_info에 request의 befor_inquiry_trace_info 설정
        // body 조립
        MultiValueMap<String, String> parameters = makeBody(request, member, bankTranId);
        // http 조립
        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://openapi.openbanking.or.kr/v2.0/account/transaction_list/acnt_num";
        RestTemplate restTemplate = new RestTemplate();
        Openbanking.ApiTransactionHistoryResponse result = restTemplate
                .postForEntity(requestUrl, param, Openbanking.ApiTransactionHistoryResponse.class)
                .getBody();
        // error_code != Null
        if (bankTranId != result.getBankTranId()) {
            throw new CustomResponseStatusException(OPENBANKING_ERROR, "메세지");
        }
        // 요청의 은행거래고유번호 != 응답의 거래고유번호(참가은행)
        if (bankTranId != result.getBankTranId()) {
            throw new CustomResponseStatusException(BANK_TANS_ID_NOT_SAME, "메세지");
        }
        // 응답의 다음페이지가 존재할 경우 다음페이지 수를 계산하여 반환
        int nextPage = 0;
        if (request.getNextPageYn() != "N") {
            nextPage = request.getPage()+1;
        }
        // Response 조립
        Openbanking.TransactionHistoryResponse response = makeResponse(result, nextPage);
        return response;
    }

    public Openbanking.AccountCheckResponse checkAccount(
            Openbanking.AccountCheckRequest request,
            String accessToken) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출
        MemberEntity member = memberRepository.findByMemberId(memberId);
        // 1. 조회요청한 계좌가 자사 계좌이면
        //    1-1. 계좌 조회 후 바로 반환
        if (requset.getBankCodeStd() == BANK_CODE) {
            AccountEntity account =
                    accountRepository.findOneByAccountNoAndMemberId(request.getAccountNo(), member.getMemberId())
                            .orElseThrow(() -> new CustomResponseStatusException(ACCOUNT_NOT_EXIST, ""));
            return Openbanking.AccountCheckResponse.builder()
                    .result(true).accountNo(account.getAccountNo())
                    .bankCodeStd(BANK_CODE).bankName(BANK_NAME)
                    .accountHolderName(member.getName())
                    .build();
        }
        //    1-2. 타사 계좌일 경우 조회 API 호출
        // 2. 은행거래고유번호 생성
        //    은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // 3. 계좌 조회
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);

        String beforeInquiryTraceInfo = "";
        // openbanking에 request 발송 및 결과 수신
        // 3-1. 전자금융업자이므로 account_holder_info_type = "N"으로 설정
        // body 조립
        MultiValueMap<String, String> parameters = makeBody(request, member, bankTranId);
        // http 조립
        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://openapi.openbanking.or.kr/v2.0/account/inquiry/real_name";
        Openbanking.ApiAccountCheckResponse result = restTemplate
                .postForEntity(requestUrl, param, Openbanking.ApiAccountCheckResponse.class)
                .getBody();
        // error_code != Null
        if (null != result.getErrorCode()) {
            throw new CustomResponseStatusException(OPENBANKING_ERROR, "메세지");
        }
        // 요청의 은행거래고유번호 != 응답의 거래고유번호(참가은행)
        if (bankTranId != result.getBankTranId()) {
            throw new CustomResponseStatusException(BANK_TANS_ID_NOT_SAME, "메세지");
        }
        // 응답의 다음페이지가 존재할 경우 다음페이지 수를 계산하여 반환
        int nextPage = 0;
        if (request.getNextPageYn() != "N") {
            nextPage = request.getPage()+1;
        }
        // Response 조립
        Openbanking.AccountCheckResponse response = makeResponse(result);
        return response;
    }

    public Openbanking.RcvAccountCheckResponse checkReceiveAccount(
            Openbanking.RcvAccountCheckRequest request,
            String accessToken) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출
        MemberEntity member = memberRepository.findByMemberId(memberId);
        // 1. 조회요청한 계좌가 자사 계좌이면
        //    1-1. 계좌 조회 후 바로 반환
        if (requset.getBankCodeStd() == BANK_CODE) {
            AccountEntity account =
                    accountRepository.findOneByAccountNoAndMemberId(request.getAccountNo(), member.getMemberId())
                            .orElseThrow(() -> new CustomResponseStatusException(ACCOUNT_NOT_EXIST, ""));
            return Openbanking.RcvAccountCheckResponse.builder()
                    .result(true).accountNo(account.getAccountNo())
                    .bankCodeStd(BANK_CODE).bankName(BANK_NAME)
                    .accountHolderName(member.getName())
                    .build();
        }
        //    1-2. 타사 계좌일 경우 조회 API 호출
        // 2. 은행거래고유번호 생성
        //    은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // 3. 계좌 조회
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);

        // openbanking에 request 발송 및 결과 수신
        // param 조립
        // 3-1. cntr_account_type = "N" 으로 설정
        // 3-2. cntr_account_num = 자사 지급계좌로 설정
        // 3-3. print_content(입금계좌인자내역) = 자사명청 + " " + req_client_name(요청고객성명)
        // 3-4. req_client_num(요청고객회원번호) = memberEntity.getMemberId()
        // 3-5. transfer_purpose(이체용도) = "TR"
        MultiValueMap<String, String> parameters = makeParams(request, member, bankTranId);
        // http 조립
        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://openapi.openbanking.or.kr/v2.0/account/inquiry/recieve";
        Openbanking.ApiRcvAccountCheckResponse result = restTemplate
                .postForEntity(requestUrl, param, Openbanking.ApiRcvAccountCheckResponse.class)
                .getBody();
        // error_code != Null
        if (null != result.getErrorCode()) {
            throw new CustomResponseStatusException(OPENBANKING_ERROR, "메세지");
        }
        // 요청의 은행거래고유번호 != 응답의 거래고유번호(참가은행)
        if (bankTranId != result.getBankTranId()) {
            throw new CustomResponseStatusException(BANK_TANS_ID_NOT_SAME, "메세지");
        }
        // Response 조립
        Openbanking.RcvAccountCheckResponse response = makeResponse(result);
        return response;
    }

    public Openbanking.DepositWithdrawResponse withdraw(
            Openbanking.TransferRequest request,
            String accessToken) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출
        MemberEntity member = memberRepository.findByMemberId(memberId);
        // 1. 은행거래고유번호 생성
        //    은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // 2. 계좌가 출금등록이 되어있는지 확인
        //    계좌번호, 등록기관번호, 조회동의여부, 등록해지구분코드로 조회
        //    2-1. 존재하지 않는다면 Exception
        OpenBankingUsingAccountEntity openBankingUsingAccountEntity = openBankingUsingAccountRepository.
                findOneByAccountNoAndTransferAgreeYnAndDeleteDvsnCdAndRegisterBankCodeStd(
                        request.getAccountNo(),"Y", "N", request.getBankCodeStd)
                .orElseThrow(() -> new CustomResponseStatusException(ACCOUNT_NOT_AGREE, ""));
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);

        // 3. 출금이체요청
        //    이용기관 수납계좌로 이체요청
        // body 조립
        // 3-1. cntr_account_type = "N" 으로 설정
        // 3-2. cntr_account_num = 자사 지급계좌로 설정
        // 3-3. print_content(입금계좌인자내역) = 자사명청 + " " + req_client_name(요청고객성명)
        // 3-4. req_client_name(요청고객성명) = memberEntity.getName()
        // 3-5. req_client_num(요청고객회원번호) = memberEntity.getMemberId()
        // 3-6. transfer_purpose(이체용도) = "TR"
        MultiValueMap<String, String> parameters = makeBody(request, member, bankTranId);
        // http 조립
        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://openapi.openbanking.or.kr/v2.0/transfer/withdraw/acnt_num";
        RestTemplate restTemplate = new RestTemplate();
        Openbanking.ApiWithdrawResponse result = restTemplate
                .postForEntity(requestUrl, param, Openbanking.ApiWithdrawResponse.class)
                .getBody();
        // error_code != Null
        if (null != result.getErrorCode) {
            throw new CustomResponseStatusException(OPENBANKING_ERROR, "메세지");
        }
        // 요청의 은행거래고유번호 != 응답의 거래고유번호(참가은행)
        if (bankTranId != result.getBankTranId()) {
            throw new CustomResponseStatusException(BANK_TANS_ID_NOT_SAME, "메세지");
        }
        // DB에 거래내역 생성
        OpenBankingTransHistory openBankingTransHistory = saveOpenbankingTransHistory(result);
        // Response 조립
        Openbanking.DepositWithdrawResponse response = makeResponse(result);
        return response;
    }

    public Openbanking.DepositWithdrawResponse deposit(
            Openbanking.TransferRequest request,
            String accessToken, String recvBankTranId, String accountHolderName) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출, 계좌 조회
        MemberEntity member = memberRepository.findByMemberId(memberId);
        AccountEntity account =
                accountRepository.findOneByAccountNoAndMemberId(request.getAccountNo(), member.getMemberId())
                        .orElseThrow(() -> new CustomResponseStatusException(ACCOUNT_NOT_EXIST, ""));
        // 1. 은행거래고유번호 생성
        //    은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);

        // 2. 입금체요청
        //    이용기관 지급계좌로 이체요청
        // body 조립
        // 2-1. cntr_account_type = "N" 으로 설정
        // 2-2. cntr_account_num = 자사 지급계좌로 설정
        // 2-3. print_content(입금계좌인자내역) = 자사명청 + " " + req_client_name(요청고객성명)
        // 2-4. req_client_name(요청고객성명) = memberEntity.getName()
        // 2-5. req_client_num(요청고객회원번호) = memberEntity.getMemberId()
        // 2-6. req_client_account_num(요청고객계좌번호) = accountEntity.getAccountNo()
        // 2-7. transfer_purpose(이체용도) = "TR"
        // 2-8. wd_pass_phrase(입금이체용 암호문구) = "NONE"
        // 2-9. recvBankTranId == Null 이면
        //      recv_bank_tran_id(수취조회은행거래고유번호) = Null
        //      name_check_option(수취인성명검증여부) = "on"
        //      recvBankTranId != Null 이면
        //      recv_bank_tran_id = recvBankTranId
        //      name_check_option = "off"
        // 2-10. account_holder_name(입금계좌예금주명) = accountHolderName
        // 2-11. wd_print_content(출금계좌인자내역) = 입금기관명칭 + " " + 입금계좌예금주명
        MultiValueMap<String, String> parameters = makeBody(request, member, bankTranId);
        // http 조립
        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://openapi.openbanking.or.kr/v2.0/transfer/withdraw/acnt_num";
        RestTemplate restTemplate = new RestTemplate();
        Openbanking.ApiDepositResponse result = restTemplate
                .postForEntity(requestUrl, param, Openbanking.ApiDepositResponse.class)
                .getBody();
        // error_code != Null
        if (null != result.getErrorCode()) {
            throw new CustomResponseStatusException(OPENBANKING_ERROR, "메세지");
        }
        // 요청의 은행거래고유번호 != 응답의 거래고유번호(참가은행)
        if (bankTranId != result.getBankTranId()) {
            throw new CustomResponseStatusException(BANK_TANS_ID_NOT_SAME, "메세지");
        }
        // DB에 거래내역 생성
        OpenBankingTransHistory openBankingTransHistory = saveOpenbankingTransHistory(result);
        // Response 조립
        Openbanking.DepositWithdrawResponse response = makeResponse(result);
        return response;
    }

    public Openbanking.CheckTransferResponse checkTransfer(
            Openbanking.TransferRequest request,
            String accessToken, String transId, String sndRcvCd) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출, 계좌 조회
        MemberEntity member = memberRepository.findByMemberId(memberId);
        // 1. 은행거래고유번호 생성
        //    은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);

        // 2. 이체확인요청
        // body 조립
        // 2-1. check_type(입출금구분) = sndRcvCd 으로 설정
        // 2-2. org_bank_tran_date = 거래일 설정
        // 2-3. org_bank_tran_id(원거래 거래고유번호(참가은행)) = transId
        MultiValueMap<String, String> parameters = makeBody(request, member, bankTranId);
        // http 조립
        HttpEntity<MultiValueMap<String, String>> param =
                new HttpEntity<MultiValueMap<String, String>>(parameters, httpHeaders);
        String requestUrl = "https://openapi.openbanking.or.kr/v2.0/transfer/withdraw/acnt_num";
        RestTemplate restTemplate = new RestTemplate();
        Openbanking.ApiDepositResponse result = restTemplate
                .postForEntity(requestUrl, param, Openbanking.ApiDepositResponse.class)
                .getBody();
        // error_code != Null
        if (null != result.getErrorCode()) {
            throw new CustomResponseStatusException(OPENBANKING_ERROR, "메세지");
        }
        // 요청의 은행거래고유번호 != 응답의 거래고유번호(참가은행)
        if (bankTranId != result.getBankTranId()) {
            throw new CustomResponseStatusException(BANK_TANS_ID_NOT_SAME, "메세지");
        }
        // DB에 거래내역 생성
        OpenBankingTransHistory openBankingTransHistory = saveOpenbankingTransHistory(result);
        // Response 조립
        Openbanking.DepositResponse response = makeResponse(result);
        return response;
    }

    public Openbanking.TransferResponse transfer(
            Openbanking.TransferRequest request,
            String accessToken, String recvBankTranId, String accountHolderName) {
        // 0. accessToken 검증 및 토큰으로부터 memberId 추출
        MemberEntity member = memberRepository.findByMemberId(memberId);
        // 1. 은행거래고유번호 생성
        //    은행기관코드(10자리) + "U" + 이용기관부여번호(9자리)
        String bankTranId = getBankTranId(BANK_CODE);
        // 2. 계좌가 출금등록이 되어있는지 확인
        //    계좌번호, 등록기관번호, 조회동의여부, 등록해지구분코드로 조회
        //    2-1. 존재하지 않는다면 Exception
        OpenBankingUsingAccountEntity openBankingUsingAccountEntity = openBankingUsingAccountRepository.
                findOneByAccountNoAndTransferAgreeYnAndDeleteDvsnCdAndRegisterBankCodeStd(
                        request.getAccountNo(),"Y", "N", request.getBankCodeStd)
                .orElseThrow(() -> new CustomResponseStatusException(ACCOUNT_NOT_AGREE, ""));
        // 3. 출금계좌실명조회
        // 4. 수취인조회
        // header 조립
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Authorization", "Bearer " + ACCESS_TOKEN);

        // 5. 출금이체요청
        //    이용기관 수납계좌로 이체요청
        Openbanking.DepositWithdrawResponse withdraw = withdraw(request, accessToken);

        // 6. 입금이체요청
        //    이용기관 지금계좌에서 출금요청
        Openbanking.DepositWithdrawResponse deposit = deposit(request, accessToken, recvBankTranId, accountHolderName);

        // 7. 이체확인 요청
        // 400 오류(입금 처리 중) 이면 재요청
        // 재요청 한계 설정(REQUEST_LIMIT)
        // 재요청 전까지 1초 대기
        requestAgain(request, accessToken, withdraw);
        requestAgain(request, accessToken, deposit);

        // Response 조립
        Openbanking.TransactionHistoryResponse response = makeResponse(result, nextPage);
        return response;
    }

    private void requestAgain(Openbanking.TransferRequest request
            , String accessToken, Openbanking.DepositWithdrawResponse withdraw) throws InterruptedException {
        if(withdraw.getResult() == false) {
            String errorCd = withdraw.getMessage();
            int cnt = 0;
            while(errorCd == "400" && cnt <=REQUEST_LIMIT ){
                if(cnt>0) {
                    Thread.sleep(1000);
                }
                errorCd = checkTransfer(request, accessToken, withdraw.getBankTransId(),"1")
                        .getMessage();
                cnt++;
            }
        } else{
            throw new CustomResponseStatusException(TRANSFER_FAIL, "메세지");
        }
    }
}
