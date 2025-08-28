package com.pingu.DOTORI.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.service.UserService;
import org.springframework.ui.Model;


@Controller
public class UserController {

	@Autowired
	UserService userService;
	
	@GetMapping("/")
	public String main() {
		
		return "test";
	}
	
	@GetMapping("/test")
	public String input(@RequestParam String test, Model model) {
		Optional<Item> result = userService.find(test);
		System.out.println(result);
		if (result.isPresent()) {
			model.addAttribute("item", result.get());
		} else {
			model.addAttribute("item", null);
		}
		
		return "test";
	}
}
