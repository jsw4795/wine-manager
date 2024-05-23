package com.winemanager.user.domain.stats;

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
public class StockByTime {
	private String Date;
	private int count;
	private int countIn;
	private int countOut;
	@Setter
	private int stock;
}
