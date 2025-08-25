package com.pingu.DOTORI.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.dto.TestUserRespDto;

@RestController
public class TestUserController {
	@GetMapping("/api/")
	public TestUserRespDto hello() {
		TestUserRespDto testUserRespDto = new TestUserRespDto();
		testUserRespDto.setStr("Hello");
		return testUserRespDto;
	}
}
