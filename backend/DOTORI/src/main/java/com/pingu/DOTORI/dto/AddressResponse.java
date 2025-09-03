package com.pingu.DOTORI.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long addressId; // 주소 ID
    private String receiver; // = users.userName
    private String phone; // = users.phone
    private String mainAddress; // = address.mainAddress
}