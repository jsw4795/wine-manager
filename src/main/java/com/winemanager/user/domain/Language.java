package com.winemanager.user.domain;

import java.util.Locale;

import lombok.Getter;

@Getter
public enum Language {
	en("English", "en", Locale.ENGLISH, "$"), ko("한국어", "ko", Locale.KOREA, "₩");

    private String lang;
    private String code;
    private Locale locale;
    private String currencySign; // 통화 기호 (₩, $)

    Language(String lang, String code, Locale locale, String currencySign) {
        this.lang = lang;
        this.code = code;
        this.locale = locale;
        this.currencySign = currencySign;
    }
}
