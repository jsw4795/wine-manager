package com.winemanager.wine.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winemanager.wine.domain.AddWineRequest;
import com.winemanager.wine.domain.DrinkWineRequest;
import com.winemanager.wine.domain.MyWineRequest;
import com.winemanager.wine.domain.Review;
import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.domain.WineDetailResponse;
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
	
	private double exchangeRate = 1300;
	private String winePicPath = "/Users/jsw4795/springboot-workspace/wine-manager-images/wine-pic";
	
	// 한시간마다 원-달러 환율 업데이트
	@Scheduled(fixedRate = 60 * 60 * 1000) // 1시간에 한번 실행
	public void run() {
		String jsonData = null;
		String requestURL = "https://quotation-api-cdn.dunamu.com/v1/forex/recent?codes=FRX.KRWUSD";
		
		HttpRequest request = HttpRequest.newBuilder()
			    .uri(URI.create(requestURL))
			    .header("accept", "application/json")
			    .method("GET", HttpRequest.BodyPublishers.noBody())
			    .build();
		
		try {
			HttpResponse<String> response;
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			jsonData = response.body();
			
			ObjectMapper objectMapper = new ObjectMapper();
		    List<Map<String, Object>> result = objectMapper.readValue(jsonData, new TypeReference<List<Map<String,Object>>>() {});
		    this.exchangeRate = (double)result.get(0).get("basePrice");
		    wineMapper.updateExchangeRate(this.exchangeRate);
		    System.out.println("환율 설정: " + this.exchangeRate);
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public double getExchangeRate() {
		return exchangeRate;
	}

	@Transactional(readOnly = true)
	@Override
	public List<String> getBuyPlace(String userId) {
		return wineMapper.selectPlaceById(userId);
	}
	
	@Transactional
	@Override
	public void insertBuyPlace(String place, String userId) {
		Map<String, Object> param = new HashMap<>();
		param.put("userId", userId);
		param.put("place", place);
		wineMapper.insertPlace(param);
	}

	@Transactional
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

	@Transactional
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

	@Transactional(readOnly = true)
	@Override
	public List<Wine> getWineListByWineName(String keyword, String userId) {
		Map<String, Object> param = new HashMap<>();
		param.put("userId", userId);
		param.put("keyword", keyword);
		List<Wine> wineList = wineMapper.selectWineListByName(param);
		
		return wineList;
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
					wine.setThumb("wine-default.png");
					wine.setThumbBottom("wine-default.png");
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

	@Transactional(readOnly = true)
	@Override
	public List<Wine> getMyWineList(MyWineRequest myWineRequest, String userId) {
		myWineRequest.setUserId(userId);
		
		int count = wineMapper.selectCountOfMyWine(myWineRequest);
		myWineRequest.setPagination(new Pagination(count, myWineRequest));
		
		if(count < 1) return null;
		
		
		List<Wine> wineList = wineMapper.selectMyWine(myWineRequest);
		
		return wineList;
	}

	@Transactional(readOnly = true)
	@Override
	public WineDetailResponse getWineDetail(int wineId, String userId) {
		WineDetailResponse response = wineMapper.selectWineDetail(Wine.builder()
																      .wineId(wineId)
																      .userId(userId)
																      .build());
		
		// 와인 로그에 재고 계산
		List<WineLog> wineLogList = response.getWineLogList();
		int stock = 0;
		for(int i = wineLogList.size() - 1; i >= 0; i--) {
			WineLog wineLog = wineLogList.get(i);
			stock += wineLog.getType().equals("IN") ? wineLog.getCount() : (-1) * wineLog.getCount();
			wineLog.setStock(stock);
		}
		
		return response;
	}

	@Transactional(readOnly = true)
	@Override
	public boolean isMyWine(int wineId, String userId) {
		return userId.equals(wineMapper.selectUserIdByWineId(wineId));
	}

	@Transactional(readOnly = true)
	@Override
	public Wine getWine(int wineId) {
		Wine wine = wineMapper.selectWineById(wineId);
		
		return wine;
	}

	@Transactional
	@Override
	public Integer drinkWine(DrinkWineRequest drinkWineRequest, String userId) {
		WineLog wineLog = WineLog.builder()
							     .userId(userId)
							     .wineId(drinkWineRequest.getWineId())
							     .type("OUT")
							     .place(drinkWineRequest.getDrinkPlace().trim())
							     .date(drinkWineRequest.getDrinkDate())
							     .count(drinkWineRequest.getDrinkCount())
							     .build();

		wineMapper.insertWineLog(wineLog);
		
		// 리뷰가 같이 들어왔으면 리뷰 등록
		if(drinkWineRequest.getReviewRating() != null) {
			Review review = Review.builder()
								  .logId(wineLog.getLogId())
								  .userId(userId)
								  .rating(drinkWineRequest.getReviewRating())
								  .title(drinkWineRequest.getReviewTitle())
								  .content(drinkWineRequest.getReviewContent())
								  .photo(drinkWineRequest.getReviewPhoto())
								  .build();
			
			wineMapper.insertReview(review);
		}
		
		return wineLog.getWineId();
	}

	@Transactional
	@Override
	public void editWine(AddWineRequest addWineRequest, String userId) {
		
		Wine wine = Wine.builder()
						.userId(userId)
						.wineId(addWineRequest.getWineId())
						.name(addWineRequest.getWineName().trim())
						.wineType(addWineRequest.getWineType().trim())
						.size(addWineRequest.getWineSize().trim())
						.vintage(addWineRequest.getWineVintage())
						.country(addWineRequest.getWineCountry().trim())
						.region(addWineRequest.getWineRegion().trim())
						.averageRating(addWineRequest.getWineAverageRating())
						.rating(addWineRequest.getWineRating())
						.averagePrice(addWineRequest.getWineAveragePrice())
						.link(addWineRequest.getWineLink().trim())
						.build();
		
		// 입력받은 파일 처리
		Wine originWine = wineMapper.selectWineById(wine.getWineId());
		
		String originThumb = originWine.getThumb();
		String originThumbBottom = originWine.getThumbBottom();
		
		// 앞에 /images/wine/이 붙어서 넘어오기때문에 이게 없는 원래 값으로 넣어줌
		wine.setThumb(originThumb);
		wine.setThumbBottom(originThumbBottom);
		
		if(!addWineRequest.getCustomImage().isEmpty()) {
			MultipartFile uploadPic = addWineRequest.getCustomImage();
			String originalFileName = uploadPic.getOriginalFilename();
			String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")); // 파일 확장자 (.jpg, .png, webp)
			String randomFileName = "WINE" + "_" + UUID.randomUUID().toString()+ fileExtension; // 파일명 랜덤으로 변경
			
			File saveFile = new File(this.winePicPath, randomFileName);
			
			try {
				uploadPic.transferTo(saveFile);
				
				// 오류 없으면 DB에 사진 이름 변경
				wine.setThumb(randomFileName);
				wine.setThumbBottom(randomFileName);
				
				// TODO: 전에 사진이 사진 파일이었다면 전 사진 파일 삭제..?
				File originThumbFile = new File(this.winePicPath, originThumb); // thumbBottom 사진 파일은 따로 없으므로 생략한다
				originThumbFile.delete();
				
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
				// 오류 발생 시 사진은 원래 있던 걸로 설정, 새로 저장된 사진 삭제
				wine.setThumb(originThumb);
				wine.setThumbBottom(originThumbBottom);
				
				saveFile.delete();
			}
		}
		
		wineMapper.updateWine(wine);
		
	}
	
}
