package com.winemanager.user.domain.stats;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatsRequest {
	String userId; // 공통
	Integer year; // 월별 지출액
	String wineDataType; // WineByType-(구매(buy), 소비(drink), 보유(hold)), WineByCountry-(국가별(country), 지역별(region))
}
