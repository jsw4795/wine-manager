package com.winemanager.wine.domain;

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
public class WineDto {
	private String userId;
	private int wineId;
	private String wineType; // r, w, s, rs
	private String name;
	private String size; // sp, h, st, m, dm
	private int vintage;
	private String country;
	private String region;
	private double averageRating;
	private int rating;
	private double price;
	private int count;
	private String link;
	private String thumb;
	private int bestYear;
}
