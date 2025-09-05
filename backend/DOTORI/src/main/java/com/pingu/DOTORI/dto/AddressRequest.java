package com.pingu.DOTORI.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
    private String receiver; // 받는 사람 이름
    private String phone; // 전화번호
    private String addr1; // 기본 주소
    private String addr2; // 상세 주소
}
