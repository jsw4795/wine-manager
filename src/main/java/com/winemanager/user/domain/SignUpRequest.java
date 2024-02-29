package com.winemanager.user.domain;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignUpRequest {
	
	@NotBlank(message = "ID is required.")
	@Pattern(regexp = "^[A-Za-z0-9]{6,15}$", message = "ID must only be in alphabets and numbers and 6 to 15 characters long.")
	private String userId;
	
	@NotBlank(message = "Password is required.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"
			, message = "Password includes English and numbers, and must be at least 8 characters long.")
	private String password;
	
	@NotBlank(message = "Password is required.")
	private String passwordCheck;
	
	@NotBlank(message = "Email is required.")
	@Pattern(regexp = "^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,3}$"
			, message = "Not Valid Email")
	private String email;
	
	@NotBlank(message = "Nickname is required.")
	@Size(max = 20, message = "Nickname must be 20 characters or less.")
	private String nickname;
	
	@NotNull
	@AssertTrue(message = "IdCheck must be done.")
	private boolean idChecked; // 아이디 중복 확인 유무
	
	private String profilePic;
}
