package com.winemanager.wine.domain;

import java.util.List;

import lombok.Data;

@Data
public class WineDetailResponse {
	private Wine wine;
	private List<WineLog> wineLogList;
	private List<Review> reviewList;
	
	private int userAveragePrice; // 사용자의 구매 평균 가격 (원)
	private Double userAverageRating; // 사용자의 평균 별점
	private int numberOfBuying; // 누적 구매 개수
	private int numberOfDrinking; // 누적 마신 개수
}
