package com.winemanager.user.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.winemanager.security.domain.SecurityUser;
import com.winemanager.user.domain.SignUpRequest;
import com.winemanager.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {
	
	private final UserService userService;
	
	@GetMapping("/login")
	public String getLogin(@ModelAttribute SecurityUser securityUser, Principal principal, Model model, @RequestParam(required = false) String error) {
		// 로그인 상태라면 메인화면으로
		if(principal != null) 
			return "redirect:/";
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("error", error);
		
		return "login/login";
	}
	
	@GetMapping("/sign-up")
	public String getSignUp(@ModelAttribute SignUpRequest signUpRequest, Principal principal, Model model) {
		// 로그인 상태라면 메인화면으로
		if(principal != null) 
			return "redirect:/";
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		
		return "login/sign-up";
	}
	
	// 회원가입 시 아이디 중복 체크
	@PostMapping("/sign-up/id-check")
	@ResponseBody
	public String checkIdValid(String userId) {
		if(!userId.matches("^[A-Za-z0-9]{6,15}$")) 
			return "pattern";
		return userService.checkIdExist(userId) ? "false" : "true";
	}
	// 회원가입 실행
	@PostMapping("/sign-up")
	public String signUp(@Valid @ModelAttribute SignUpRequest signUpRequest, BindingResult result, Principal principal, Model model) {
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		
		// 비밀번호, 비밀번호 확인 미 일치 시, 에러 추가하고 로그인 회원가입 페이지로
		if(!signUpRequest.getPassword().equals(signUpRequest.getPasswordCheck())) 
			result.rejectValue("passwordCheck", "errer.passwordCheck", "Not same with PW.");
		
		if(result.hasErrors()) {
			return "/login/sign-up";
		}

		userService.signUp(signUpRequest);
		
		return "redirect:/login";
	}
	
}
