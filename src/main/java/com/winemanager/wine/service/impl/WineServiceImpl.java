package com.winemanager.wine.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.winemanager.wine.domain.AddWineRequest;
import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.domain.WineLog;
import com.winemanager.wine.mapper.WineMapper;
import com.winemanager.wine.service.WineService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WineServiceImpl implements WineService{

	private final WineMapper wineMapper;

	@Override
	public List<String> getBuyPlace(String userId) {
		return wineMapper.selectPlaceById(userId);
	}

	@Override
	public Integer addNewWine(AddWineRequest addWineRequest, String userId) {
		Wine wine = Wine.builder()
						.userId(userId)
						.name(addWineRequest.getWineName().trim())
						.wineType(addWineRequest.getWineType().trim())
						.size(addWineRequest.getWineSize().trim())
						.vintage(addWineRequest.getWineVintage())
						.country(addWineRequest.getWineCountry().trim())
						.region(addWineRequest.getWineRegion().trim())
						.averageRating(addWineRequest.getWineAverageRating())
						.rating(addWineRequest.getWineRating())
						.averagePrice(addWineRequest.getWineAveragePrice())
						.count(0)
						.link(addWineRequest.getWineLink().trim())
						.thumb(addWineRequest.getWineThumb().trim().split("_")[0])
						.build();
		
		wineMapper.insertNewWine(wine);
		
		WineLog wineLog = WineLog.builder()
							     .userId(userId)
							     .wineId(wine.getWineId())
							     .type("IN")
							     .place(addWineRequest.getBuyPlace().trim())
							     .date(addWineRequest.getBuyDate())
							     .price(addWineRequest.getBuyPrice())
							     .count(addWineRequest.getBuyCount())
							     .build();
		
		wineMapper.insertWineLog(wineLog);
			
		
		
		return wine.getWineId();
	}

	@Override
	public Integer addBuyWineLog(AddWineRequest addWineRequest, String userId) {
		WineLog wineLog = WineLog.builder()
			     .userId(userId)
			     .wineId(addWineRequest.getWineId())
			     .type("IN")
			     .place(addWineRequest.getBuyPlace().trim())
			     .date(addWineRequest.getBuyDate())
			     .price(addWineRequest.getBuyPrice())
			     .count(addWineRequest.getBuyCount())
			     .build();

		wineMapper.insertWineLog(wineLog);
		
		return wineLog.getWineId();
	}

	@Override
	public List<Wine> getWineListWithAllData(String userId) {
		return wineMapper.selectWineListWithAllData(userId);
	}
	
}
