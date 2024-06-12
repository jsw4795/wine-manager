package com.winemanager.user.domain;

import java.util.Locale;

import lombok.Getter;

@Getter
public enum Language {
	en("English", Locale.ENGLISH), ko("한국어", Locale.KOREA);

    private String lang;
    private Locale locale;

    Language(String lang, Locale locale) {
        this.lang = lang;
        this.locale = locale;
    }
}
