package com.pingu.DOTORI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDTO {
    private Long addressId;
    private String mainAddress;

    // 프론트에서 보내주는 데이터
    private String receiver;
    private String phone;
    private String postcode;
    private String addr1;
    private String addr2;
}
