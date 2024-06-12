package com.winemanager.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.winemanager.user.domain.User;
import com.winemanager.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityService implements UserDetailsService{

	private final UserMapper userMapper;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userMapper.selectUserById(User.builder().userId(username).build());
		if(user == null) throw new UsernameNotFoundException("사용자를 찾을 수 없음.");
		return user;
	}
	

}
