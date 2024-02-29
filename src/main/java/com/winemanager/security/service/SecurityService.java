package com.winemanager.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.winemanager.security.domain.SecurityUser;
import com.winemanager.security.mapper.SecurityMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityService implements UserDetailsService{

	private final SecurityMapper securityMapper;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		SecurityUser sUser = securityMapper.selectUserById(username);
		if(sUser == null) throw new UsernameNotFoundException("사용자를 찾을 수 없음.");
		return securityMapper.selectUserById(username);
	}
	

}
