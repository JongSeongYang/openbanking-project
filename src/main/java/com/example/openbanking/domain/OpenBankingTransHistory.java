package com.example.openbanking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.lang.reflect.Member;
import java.time.LocalDateTime;

@Entity
@Table(name = "Opbk_Tran_Hist")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OpenBankingTransHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    @NotNull
    private LocalDateTime apiTranId;  // 거래고유번호
    @NotNull
    private LocalDateTime apiTranDt;  // 거래일시
    @NotNull
    private String sendReceiveCd ;  // 송수신구분코드
    @NotNull
    private String inoutType ;  // 입출금구분코드
    @NotNull
    private String tranNo ;  // 거래발생순번(default = 1)
    private String tranPurpose ;  // 이체용도
    @NotNull
    private String tranAmt ;  // 거래금액
    @ManyToOne
    @JoinColumn(name = "memberId")
    private MemberEntity memberEntity ;  // 요청고객 회원번호
    @NotNull
    private String bankTran_Id ;  // (요청기관)은행거래고유번호
    private String fintechUseNum ;  // 요청고객 핀테크번호
    @NotNull
    private String reqClientBankCd ;  // 요청고객계좌개설기관.표준코드(3자리)
    @NotNull
    private String wdBankCodeStd ;  // 출금은행코드
    @NotNull
    private String wdBankCodeSub ;  // 출금은행점별코드
    @NotNull
    private String wdBankName ;  // 출금은행이름
    @NotNull
    private String wdAccountNo ;  // 출금계좌번호
    private String wdPrintContent ;  // 출금통장기재내용
    @NotNull
    private String wdAccountHolderName ;  // 송금인명
    @NotNull
    private String bankCodeStd ;  // 입금은행코드
    private String savingsBankName ;  // 개별저축은행명
    @NotNull
    private String bankCodeSub ;  // 입금은행점별코드
    @NotNull
    private String bankName ;  // 입금은행이름
    @NotNull
    private String accountNo ;  // 입금계좌번호
    private String printContent ;  // 입금통장기재내용
    @NotNull
    private String accountHolderName ;  // 입금계좌고객성명
    @NotNull
    private String reqClientName ;  // 요청고객성명
    private String errorCd; // 오류 코드
    private String errorMsg; // 오류 메세지
    @NotNull
    private LocalDateTime operateDt; // 조작일시
}
