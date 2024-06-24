package com.winemanager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

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
	
}
