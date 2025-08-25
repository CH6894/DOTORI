package com.pingu.DOTORI.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pingu.DOTORI.common.TestUserResCode;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestUserRespDto {
	private String str;
	private int code = TestUserResCode.SUCCESS.value();
	private String message;
}
