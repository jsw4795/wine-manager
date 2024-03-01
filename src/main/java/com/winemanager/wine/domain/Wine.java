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
public class Wine {
	private String userId;
	private int wineId;
	private String wineType; // red, white, sparkling, rose
	private String name;
	private String size; // split, half, standard, magnum, double magnum
	private Integer vintage;
	private String country;
	private String region;
	private Double averageRating;
	private Integer rating;
	private Double averagePrice;
	private int count;
	private String link;
	private String thumb;
	private Integer bestYear;
}
