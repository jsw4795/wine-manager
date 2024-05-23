package com.winemanager.user.domain.stats;

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
public class WineByPrice {
	private int range0To10;
	private int range10To50;
	private int range50To100;
	private int range100To500;
	private int range500To1000;
	private int rangeFrom1000;
}
