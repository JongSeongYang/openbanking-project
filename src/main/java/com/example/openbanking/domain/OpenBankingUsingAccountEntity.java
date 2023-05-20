package com.example.openbanking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Opbk_Usng_Acnt")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@IdClass(OpenBankingUsingAccountId.class)
public class OpenBankingUsingAccountEntity {

    @Id
    private String registerBankCodeStd; // 등록계좌개설기관 표준코드(3글자)
    @Id
    private String registerAccountNum; // 등록계좌번호
    @Id
    private String userSeqNo ;// 사용자일련번호
    @NotNull
    private String apiTranId; // api거래고유번호
    private LocalDateTime apiTranDt;  // 거래일시
    private String registerAccountSeq; //등록계좌상품회차(예금)
    private String accountType; // 등록계좌 종류
    @ManyToOne
    @JoinColumn(name = "accountNo")
    private AccountEntity accountEntity; //당사계좌번호
    private String fintechUseNum; // 핀테크이용번호
    @ManyToOne
    @JoinColumn(name = "memberId")
    private MemberEntity memberEntity; // (userNum)회원번호
    @NotNull
    private String transferAgreeYn; // 출금동의여부
    private String agmtDataType; // 출금동의자료구분
    @NotNull
    private String inquiryAgreeYn; // 조회동의여부
    private String payerNum; // 납부자번호
    private String deleteDvsnCd; // 등록해지구분코드
    private String errorCd; // 오류 코드
    private String errorMsg; // 오류 메세지
    @NotNull
    private LocalDateTime operateDt; // 조작일시

}