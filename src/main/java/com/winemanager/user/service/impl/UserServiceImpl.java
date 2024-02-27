package com.winemanager.user.service.impl;

import org.springframework.stereotype.Service;

import com.winemanager.user.mapper.UserMapper;
import com.winemanager.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{
	
	private final UserMapper userMapper;
	
	
}
