package com.winemanager.wine.domain;

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
public class Wine extends WithThumb {
	private String userId;
	private Integer wineId;
	private String wineType; // red, white, sparkling, rose
	private String name;
	private String size; // split, half, standard, magnum, double magnum
	@Setter
	private String sizeToShow;
	private Integer vintage;
	private String country;
	private String region;
	private Double averageRating;
	private Integer rating;
	private Double averagePrice;
	private int count;
	private String link;
	@Setter
	private String thumb;
	@Setter
	private String thumbBottom;
	private Integer bestYear;
	
	private Double alcohol;
	private String grape; // 품종이 하나면 품종으로, 여러개면 blend
	private String winery;
}
