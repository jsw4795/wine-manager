package com.winemanager;

import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebConfig {
	
	@Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);  // 기본 값 설정
        return localeResolver;
    }
	
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper;
	}
	@Bean
	public DateFormat englishDateFormat() {
		SimpleDateFormat df = new SimpleDateFormat("MMMM d, yyyy (EEE)", Locale.ENGLISH);
		df.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
		return df;
	}
	@Bean
	public DateFormat koreanDateFormat() {
		SimpleDateFormat df = new SimpleDateFormat("MMM d일, yyyy (EEE)", Locale.KOREAN);
		df.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
		return df;
	}
	
	// API 요청 시 인증서 무시하기 위한 TrustManager
	@Bean
	public TrustManager[] trustAllCerts() {
		return new TrustManager[]{
			    new X509TrustManager() {
			        public X509Certificate[] getAcceptedIssuers() {
			            return null;
			        }
			        public void checkClientTrusted(X509Certificate[] certs, String authType) {
			        }
			        public void checkServerTrusted(X509Certificate[] certs, String authType) {
			        }
			    }
			};
	}
	
}
