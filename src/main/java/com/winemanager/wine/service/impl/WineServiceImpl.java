package com.winemanager.wine.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	public List<Wine> getWineListWithAllData(String userId) {
		return wineMapper.selectWineListWithAllData(userId);
	}

	@Override
	public List<Wine> searchWineInVivino(String keyword) {
		
		String rootDir = System.getProperty("user.dir");
		String nodeDir = "/usr/local/bin/node"; // 설치된 노드 위치 (환경변수를 못읽음)
		
		ProcessBuilder pb = new ProcessBuilder(nodeDir, rootDir + "/src/main/resources/api/vivino-api-main/vivino.js", "--name=piper"); // path를 못찾는다 절대경로로 지정해주자
		//pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		//pb.redirectError(ProcessBuilder.Redirect.INHERIT);
		
		String jsonResult = null;
		ObjectMapper mapper = new ObjectMapper();
		List<Wine> wineList = null;
		try {
			Process p = pb.start();
			jsonResult = new String(p.getInputStream().readAllBytes());
			wineList = mapper.readValue(jsonResult, new TypeReference<List<Wine>>() {});
			for(Wine wine : wineList) {
				String thumb = wine.getThumb();
				
				// vivino에 사진이 없으면, 자체 기본 이미지로 변경
				if(thumb.contains("default_label")) {
					wine.setThumb("/images/wine-default.png");
					wine.setThumbBottom("/images/wine-default.png");
					continue;
				}
				
				int idx1 = thumb.lastIndexOf("_");
				int idx2 = thumb.lastIndexOf(".");
				String thumbBody = thumb.substring(0, idx1); 
				String thumbSize = "80x80"; // 이미지 사이즈
				String thumbExtention = thumb.substring(idx2 + 1); // jpg or png
				
				thumb = thumbBody + "_" + thumbSize + "." + thumbExtention;
				thumb = thumb.replace("_pb_", "_pl_"); // 하반부 사진으로 변경
				wine.setThumbBottom(thumb);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return wineList;
	}
	
}
