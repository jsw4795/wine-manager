package com.winemanager.security;

import java.io.IOException;

import javax.security.sasl.AuthenticationException;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	
	// 시큐리티 기능을 비활성화 할 요청 설정 (정적 요소들에 한해서 시큐리티 비활성화)
	@Bean
	public WebSecurityCustomizer configure() {
		return (web) -> web.ignoring()
						   .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf((csrfConfig) -> 
					csrfConfig.disable()
				)
				.authorizeHttpRequests((authorizeRequests) ->  // 요청별 허용 범위 설정 (권한은 사용하지 않겠다)
					authorizeRequests
						.requestMatchers("/", "/login/**", "/sign-up/**", "/about").permitAll()
						.anyRequest().authenticated()
				)
				.formLogin((formLoginConfig) ->  // 내가 만든 폼에서 로그인 하겠다
					formLoginConfig
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.usernameParameter("userId")
		                .passwordParameter("password")
						.defaultSuccessUrl("/")
						.successHandler(new AuthenticationSuccessHandler() {
							@Override
							public void onAuthenticationSuccess(HttpServletRequest request,
									HttpServletResponse response, Authentication authentication)
									throws IOException, ServletException {
								System.out.println("authentication : " + authentication.getName());

	                            response.sendRedirect("/");
								
							}
		                })
						.failureUrl("/login")
						.failureHandler(new AuthenticationFailureHandler() {
							@Override
							public void onAuthenticationFailure(HttpServletRequest request,
									HttpServletResponse response,
									org.springframework.security.core.AuthenticationException exception)
									throws IOException, ServletException {
								System.out.println("exception : " + exception.getMessage());

                                response.sendRedirect("/login");
							}
		                })
				)
				.logout((logoutConfig) ->  // 로그아웃 설정
					logoutConfig
						.logoutSuccessUrl("/")
				)
				.build();
		
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
