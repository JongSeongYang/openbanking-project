package com.example.openbanking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Own_Acnt")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AccountEntity {

    @Id
    private String accountNo;
    private String accountType; // 상품 종류
    @ManyToOne
    @JoinColumn(name = "memberId")
    private MemberEntity memberEntity;
    private String instituteCode;
    private LocalDateTime enrollTime;
    private Long deposit;
    private LocalDateTime updateDepositTime;
    private String isDelete;  // 삭제여부
}
