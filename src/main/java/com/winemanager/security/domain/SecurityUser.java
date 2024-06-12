package com.winemanager.security.domain;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @deprecated
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUser implements UserDetails{


	private String userId;
	private String password;
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("user")); // 유저 권한
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return userId;
	}

	// 왜 다 true로 해야하는거지,,, 내가 영어가부족한가
	
	// 계정 만료 여부
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	// 계정 잠금 여부
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	// 비밀번호 만료 여부
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	// 계정 사용 여부
	@Override
	public boolean isEnabled() {
		// TODO: 추후에 이메일로 인증 메일 보내는 로직 추가하고, 검증 과정 추가
		return true;
	}

}
