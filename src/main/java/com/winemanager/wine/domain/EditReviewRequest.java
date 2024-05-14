package com.winemanager.wine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditReviewRequest {
	private int reviewId;
	private int logId;
	private double rating;
	private String title;
	private String content;
	private String photo;
	
	private String wineId;
	
}
