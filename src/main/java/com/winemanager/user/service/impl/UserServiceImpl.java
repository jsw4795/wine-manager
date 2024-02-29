package com.winemanager.user.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.winemanager.user.domain.SignUpRequest;
import com.winemanager.user.domain.User;
import com.winemanager.user.mapper.UserMapper;
import com.winemanager.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{
	
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	public User findByUserId(String userId) {
		return userMapper.selectUserById(User.builder()
											 .userId(userId)
											 .build());
	}

	@Override
	public boolean checkIdExist(String userId) {
		User user = userMapper.selectUserById(User.builder()
												  .userId(userId)
												  .build());
		return user != null;
	}

	@Override
	public boolean signUp(SignUpRequest signUpRequest) {
		try {
			userMapper.insertUser(User.builder()
					.userId(signUpRequest.getUserId().trim())
					.password(passwordEncoder.encode(signUpRequest.getPassword().trim()))
					.email(signUpRequest.getEmail().trim())
					.nickname(signUpRequest.getNickname().trim())
					.build());			
		} catch (Exception e) {
			System.out.println("에러..! : " + e.getMessage());
			return false;
		}
		return true;
	}
	
	
}
