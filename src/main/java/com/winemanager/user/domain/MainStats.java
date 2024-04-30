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
public class MainStats {
	private Integer numberOfBuying;
	private Integer numberOfDrinking;
	private Integer moneySpent;
}
