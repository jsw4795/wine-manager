package com.winemanager.security;

import java.io.IOException;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.winemanager.user.domain.Language;
import com.winemanager.user.domain.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final UserDetailsService detailsService;
	
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
								
								// 세션에 언어 정보 저장
								request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, Language.valueOf(((User)authentication.getPrincipal()).getLanguage()).getLocale());
								
	                            response.sendRedirect("/");
								
							}
		                })
						.failureUrl("/login")
						.failureHandler(new AuthenticationFailureHandler() {
							@Override
							public void onAuthenticationFailure(HttpServletRequest request,
									HttpServletResponse response,
									AuthenticationException exception)
									throws IOException, ServletException {
								
								String error;
								
								if (exception instanceof UsernameNotFoundException) {
									error = "notExist";
								} else if(exception instanceof BadCredentialsException) {
									error = "notMatch";
								} else if (exception instanceof InternalAuthenticationServiceException) {
									error = "systemError";
								} else if (exception instanceof AuthenticationCredentialsNotFoundException) {
									error = "refused";
								} else {
									error = "error";
								}

                                response.sendRedirect("/login?error=" + error);
							}
		                })
				)
				.logout((logoutConfig) ->  // 로그아웃 설정
					logoutConfig
						.logoutSuccessUrl("/")
				)
				.rememberMe(rememberMeConfig -> {
					rememberMeConfig
						.key("UK") // 쿠키에 사용되는 값을 암호화 할때 사용할 키
						.alwaysRemember(true)
						.tokenValiditySeconds(3600 * 24 * 365)
						.authenticationSuccessHandler(new AuthenticationSuccessHandler() {
							@Override
							public void onAuthenticationSuccess(HttpServletRequest request,
									HttpServletResponse response, Authentication authentication)
									throws IOException, ServletException {
								System.out.println("auto authentication : " + authentication.getName());
								
								// 세션에 언어 정보 저장
								request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, Language.valueOf(((User)authentication.getPrincipal()).getLanguage()).getLocale());
								
								response.sendRedirect(request.getRequestURI());
							}
		                });
						
						
				})
				.build();
		
	}
	
	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(detailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setHideUserNotFoundExceptions(false); // 아이디가 존재하지 않을 때 존재하지 않는 아이디 예외 출력
        return authenticationProvider;
    }
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
