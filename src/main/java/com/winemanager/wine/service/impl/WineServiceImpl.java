package com.winemanager.wine.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.winemanager.wine.domain.AddWineRequest;
import com.winemanager.wine.domain.MyWineRequest;
import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.domain.WineLog;
import com.winemanager.wine.mapper.WineMapper;
import com.winemanager.wine.service.WineService;
import com.winemanager.wine.util.Pagination;
import com.winemanager.wine.util.VivinoAPI;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WineServiceImpl implements WineService{

	private final WineMapper wineMapper;
	private final VivinoAPI vivinoAPI;

	@Override
	public List<String> getBuyPlace(String userId) {
		return wineMapper.selectPlaceById(userId);
	}
	
	@Override
	public void insertBuyPlace(String place, String userId) {
		Map<String, Object> param = new HashMap<>();
		param.put("userId", userId);
		param.put("place", place);
		wineMapper.insertPlace(param);
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
						.thumb(addWineRequest.getWineThumb().trim())
						.thumbBottom(addWineRequest.getWineThumbBottom().trim())
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
	public List<Wine> getWineListByWineName(String keyword, String userId) {
		Map<String, Object> param = new HashMap<>();
		param.put("userId", userId);
		param.put("keyword", keyword);
		return wineMapper.selectWineListByName(param);
	}

	@Override
	public List<Wine> searchWineInVivino(String keyword) {
		
		List<Wine> wineList = null;
		try {
			wineList = vivinoAPI.getWineListByName(keyword);
			
			for(Wine wine : wineList) {
				String thumbOrigin = wine.getThumb();
				
				// vivino에 사진이 없으면, 자체 기본 이미지로 변경
				if(thumbOrigin.contains("default_label")) {
					wine.setThumb("/images/wine-default.png");
					wine.setThumbBottom("/images/wine-default.png");
					continue;
				}
				
				int idx1 = thumbOrigin.lastIndexOf("_");
				int idx2 = thumbOrigin.lastIndexOf(".");
				String thumbBody = thumbOrigin.substring(0, idx1); 
				String thumbSize = "x600"; // 이미지 사이즈
				String thumbBottomSize = "80x80"; 
				String thumbExtention = thumbOrigin.substring(idx2 + 1); // jpg or png
				
				String thumb = thumbBody + "_" + thumbSize + "." + thumbExtention;
				String thumbBottom = thumbBody + "_" + thumbBottomSize + "." + thumbExtention;
				
				thumbBottom = thumbBottom.replace("_pb_", "_pl_"); // 하반부 사진으로 변경 (있을 때만)
				
				wine.setThumb(!thumbExtention.equals("png") ? thumbOrigin : thumb); // 확장자가 png일 때만 사이즈 바꾸기
				wine.setThumbBottom(thumbBottom);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return wineList;
	}

	@Override
	public List<Wine> getMyWineList(MyWineRequest myWineRequest, String userId) {
		myWineRequest.setUserId(userId);
		
		int count = wineMapper.selectCountOfMyWine(myWineRequest);
		myWineRequest.setPagination(new Pagination(count, myWineRequest));
		
		if(count < 1) return null;
		
		
		return wineMapper.selectMyWine(myWineRequest);
	}

	
	
}
