package com.winemanager.wine.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WineDetailRequest {
	private int wineId;
	private String userId;	
	private int page;
	private int pageSize;
	
	private String type; // log or review
}
