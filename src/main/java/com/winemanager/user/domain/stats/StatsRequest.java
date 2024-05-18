package com.winemanager.user.domain.stats;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatsRequest {
	String userId; // 공통
	Integer year; // 월별 지출액
	String wineDataType; // 국가별, 지역별
}
