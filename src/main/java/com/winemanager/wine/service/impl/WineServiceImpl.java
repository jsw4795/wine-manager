package com.winemanager.wine.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.winemanager.wine.domain.AddWineRequest;
import com.winemanager.wine.domain.DrinkWineRequest;
import com.winemanager.wine.domain.EditReviewRequest;
import com.winemanager.wine.domain.EditWineLogRequest;
import com.winemanager.wine.domain.MyWineRequest;
import com.winemanager.wine.domain.Review;
import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.domain.WineDetailRequest;
import com.winemanager.wine.domain.WineDetailResponse;
import com.winemanager.wine.domain.WineLog;
import com.winemanager.wine.domain.WithThumb;
import com.winemanager.wine.mapper.WineMapper;
import com.winemanager.wine.service.CrawlingService;
import com.winemanager.wine.service.WineService;
import com.winemanager.wine.util.Pagination;

@Service
public class WineServiceImpl implements WineService{

	private final WineMapper wineMapper;
	private final CrawlingService crawlingService;
	private final MessageSource messageSource;
	
	private final String winePicPath;
	
	// 생성자가 1개여서 @AutoWired 생략
	public WineServiceImpl(
			@Value("${winePicPath}") String winePicPath,
			WineMapper wineMapper,
			CrawlingService crawlingService,
			MessageSource messageSource
			) {
		this.winePicPath = winePicPath;
		this.wineMapper = wineMapper;
		this.crawlingService = crawlingService;
		this.messageSource = messageSource;
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
						.alcohol(addWineRequest.getWineAlcohol())
						.grape(addWineRequest.getWineGrape())
						.winery(addWineRequest.getWineWinery())
						.build();
		
		// 입력받은 파일 처리
		
		if(addWineRequest.getCustomImage() != null && !addWineRequest.getCustomImage().isEmpty()) {
			MultipartFile uploadPic = addWineRequest.getCustomImage();
			String originalFileName = uploadPic.getOriginalFilename();
			String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")); // 파일 확장자 (.jpg, .png, webp)
			String randomFileName = "WINE" + "_" + UUID.randomUUID().toString()+ fileExtension; // 파일명 랜덤으로 변경
			
			File saveFile = new File(this.winePicPath, randomFileName);
			
			try {
				uploadPic.transferTo(saveFile);
				
				// 오류 없으면 사진 파일명 등록
				wine.setThumb(randomFileName);
				wine.setThumbBottom(randomFileName);
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
				
				saveFile.delete();
			}
		}

		wineMapper.insertNewWine(wine);
		
		WineLog wineLog = WineLog.builder()
							     .userId(userId)
							     .wineId(wine.getWineId())
							     .type("IN")
							     .place(addWineRequest.getBuyPlace().trim())
							     .date(addWineRequest.getBuyDate())
							     .price(addWineRequest.getBuyPrice())
							     .count(addWineRequest.getBuyCount())
							     .note(addWineRequest.getNote())
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
							     .note(addWineRequest.getNote())
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
		wineList = crawlingService.getWineListByName(keyword);
		// 썸네일 바텀 설정 crawlingService로 옮김
		
		return wineList;
	}
	@Override
	public Wine searchWineMoreInfo(AddWineRequest request) {
		Wine wine = null; 
		wine = crawlingService.getMoreWineInfo(Wine.builder()
														   .name(request.getWineName())
														   .country(request.getWineCountry())
														   .region(request.getWineRegion())
														   .averageRating(request.getWineAverageRating())
														   .rating(request.getWineRating())
														   .thumb(request.getWineThumb())
														   .thumbBottom(request.getWineThumbBottom())
														   .link(request.getWineLink())
														   .build());
		
		return wine;
	}

	@Transactional(readOnly = true)
	@Override
	public List<Wine> getMyWineList(MyWineRequest myWineRequest, String userId) {
		myWineRequest.setUserId(userId);
		
		int count = wineMapper.selectCountOfMyWine(myWineRequest);
		myWineRequest.setPagination(new Pagination(count, myWineRequest));
		
		if(count < 1) return null;
		
		
		List<Wine> wineList = wineMapper.selectMyWine(myWineRequest);
		
		for(Wine wine : wineList) {
			wine.setSizeToShow(messageSource.getMessage("wine.size." + wine.getSize().toLowerCase().replaceAll("double magnum", "doubleMagnum"), null, LocaleContextHolder.getLocale()));
		}
		
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
		
		response.getWine().setSizeToShow(messageSource.getMessage("wine.size." + response.getWine().getSize().toLowerCase().replaceAll("double magnum", "doubleMagnum"), null, LocaleContextHolder.getLocale()));
		
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
							     .note(drinkWineRequest.getNote())
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
						.alcohol(addWineRequest.getWineAlcohol())
						.grape(addWineRequest.getWineGrape())
						.winery(addWineRequest.getWineWinery())
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

	@Override
	public WineLog getWineLog(int logId, String userId) {
		return wineMapper.selectWineLog(WineLog.builder()
											   .logId(logId)
											   .userId(userId)
											   .build());
	}

	@Override
	public void editWineLog(EditWineLogRequest editWineLogRequest, String userId) {
		wineMapper.updateWineLog(WineLog.builder()
										.logId(editWineLogRequest.getLogId())
										.userId(userId)
										.wineId(editWineLogRequest.getWineId())
										.place(editWineLogRequest.getPlace())
										.date(editWineLogRequest.getDate())
										.price(editWineLogRequest.getPrice())
										.count(editWineLogRequest.getCount())
										.note(editWineLogRequest.getNote())
										.build());
		
		if(editWineLogRequest.getReviewRating() == null) //등록할 리뷰 없으면 리턴
			return;
		
		wineMapper.insertReview(Review.builder()
									  .logId(editWineLogRequest.getLogId())
									  .userId(userId)
									  .rating(editWineLogRequest.getReviewRating())
									  .title(editWineLogRequest.getReviewTitle())
									  .content(editWineLogRequest.getReviewContent())
									  .photo(editWineLogRequest.getReviewPhoto())
									  .build());
		
		
	}

	@Override
	public List<WineLog> getWineLogList(WineDetailRequest wineDetailRequest) {
		return wineMapper.selectWineLogWithPagination(wineDetailRequest);
	}

	@Override
	public List<Review> getWineReviewList(WineDetailRequest wineDetailRequest) {
		return wineMapper.selectWineReviewWithPagination(wineDetailRequest);
	}

	@Override
	public Review getReivew(int reviewId, String userId) {
		return wineMapper.selectReview(Review.builder()
											 .reviewId(reviewId)
											 .userId(userId)
											 .build());
	}

	@Override
	public void editReview(EditReviewRequest editReviewRequest, String userId) {
		wineMapper.updateReview(Review.builder()
									  .reviewId(editReviewRequest.getReviewId())
									  .logId(editReviewRequest.getLogId())
									  .userId(userId)
									  .rating(editReviewRequest.getRating())
									  .title(editReviewRequest.getTitle())
									  .content(editReviewRequest.getContent())
									  .photo(editReviewRequest.getPhoto())
									  .build());
	}

	@Override
	public void deleteWineLog(EditWineLogRequest editWineLogRequest, String userId) {
		wineMapper.deleteWineLog(WineLog.builder()
										.logId(editWineLogRequest.getLogId())
										.wineId(editWineLogRequest.getWineId())
										.userId(userId)
										.build());
	}

	@Override
	public void deleteWine(AddWineRequest addWineRequest, String userId) {
		// 파일이 있으면 파일 삭제
		Wine wine = wineMapper.selectWineById(addWineRequest.getWineId());
		
		if(wine.getThumb() != null && !wine.getThumb().startsWith("https://")) {
			File thumbFile = new File(winePicPath, wine.getThumb());
			thumbFile.delete();
		}
		
		wineMapper.deleteWine(Wine.builder()
								 .wineId(addWineRequest.getWineId())
								 .userId(userId)
								 .build());
		
	}

	@Override
	public void deleteReview(EditReviewRequest editReviewRequest, String userId) {
		wineMapper.deleteReview(Review.builder()
									  .reviewId(editReviewRequest.getReviewId())
								      .wineId(editReviewRequest.getWineId())
								      .userId(userId)
								      .build());
		
	}

	@Override
	public Review getReviewByWineLog(int logId, String userId) {
		return wineMapper.selectReviewByLogId(WineLog.builder()
													 .logId(logId)
													 .userId(userId)
													 .build());
	}
	
	public void setWineImage(WithThumb object) {
		if(object == null)
			return;
		
		if(object.getThumb() == null || object.getThumb().length() < 1) {
			object.setThumb("wine-default.png");
			object.setThumbBottom("wine-default.png");
		}
		
		if(!object.getThumb().startsWith("https://")) {
			object.setThumb("/images/wine/" + object.getThumb());
			object.setThumbBottom("/images/wine/" + object.getThumbBottom());
		}
	}
	public void setWineImage(List<? extends WithThumb> objectList) {
		if(objectList == null)
			return;
		
		for(WithThumb object : objectList) 
			this.setWineImage(object);
	}
	
}
