package com.winemanager.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.winemanager.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {
	
	private final UserService userService;
	
	
	@GetMapping("/login")
	public String getLogin() {
		return "login/login";
	}
	
	@GetMapping("/sign-up")
	public String getSignUp() {
		return "login/sign-up";
	}
	
}
