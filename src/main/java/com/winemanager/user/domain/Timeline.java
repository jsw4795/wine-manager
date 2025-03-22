package com.winemanager.user.domain;

import java.util.Date;

import com.winemanager.wine.domain.WithThumb;

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
public class Timeline extends WithThumb {
	private Integer wineId; // wine
	private Integer logId; // wine_log
	//@JsonFormat(pattern = "MMMM d, yyyy (EEE)", locale = "en-US", timezone = "Asia/Seoul")
	private Date date; // wine_log
	private String type; // wine_log
	private String wineName; // wine
	private Integer vintage; // wine
	private String wineSize; // wine
	private Integer count; // wine_log
	private Double price; // wine_log
	private String place; // wine_log
	private Double rate; // review
	@Setter
	private String thumb; // wine
	@Setter
	private String thumbBottom; // wine 
	
	private Double spendToday;
	private Integer buyToday;
	private Integer drinkToday;
	
	
	// 원화 -> 다른 통화 변경 (모든 통화에 적용가능)
	public void applyCurrency(double exchangeRate) {
		if(this.price != null)
			this.price = Math.round(this.price / exchangeRate * 100) / 100.0;
		if(this.spendToday != null)
			this.spendToday = Math.round(this.spendToday / exchangeRate * 100) / 100.0;
	}
}
