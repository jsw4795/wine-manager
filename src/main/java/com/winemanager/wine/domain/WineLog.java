package com.winemanager.wine.domain;

import java.sql.Date;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WineLog {
	private int logId;
	private String userId;
	private int wineId;
	
	private String type; // 로그 종류 / IN(구매) or OUT(소비)
	
	private String place;
	private Date date;
	private Integer price;
	private int count;
	private String note;
	
	@Setter
	private int stock;
}
