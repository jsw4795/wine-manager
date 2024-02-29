package com.winemanager.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
	private String userId;
	private String password;
	private String email;
	private String nickname;
	private String profilePic;
}
