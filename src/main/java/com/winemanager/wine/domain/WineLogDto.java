package com.winemanager.wine.domain;

import java.sql.Date;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WineLogDto {
	private String userId;
	private int wineId;
	
	private String type; // 로그 종류 / IN(구매) or OUT(소비)
	
	private int placeId;
	private Date date;
	private int price;
	private int count;
}
