package com.winemanager.user.domain;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timeline {
	private Integer wineId; // wine
	private Integer logId; // wine_log
	@JsonFormat(pattern = "MMMM d, yyyy (EEE)", locale = "en-US")
	private Date date; // wine_log
	private String type; // wine_log
	private String wineName; // wine
	private Integer vintage; // wine
	private String wineSize; // wine
	private Integer count; // wine_log
	private Integer price; // wine_log
	private String place; // wine_log
	private Double rate; // review
	@Setter
	private String thumb; // wine
	@Setter
	private String thumbBottom; // wine
}
