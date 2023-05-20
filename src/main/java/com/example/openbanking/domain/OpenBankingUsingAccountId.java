package com.example.openbanking.domain;


import lombok.*;

import javax.persistence.Id;
import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
@Setter
@Getter
@ToString
public class OpenBankingUsingAccountId implements Serializable {
    private String registerBankCodeStd; // 등록계좌개설기관 표준코드(3글자)
    private String registerAccountNum; // 등록계좌번호
    private String userSeqNo ;// 사용자일련번호
}
