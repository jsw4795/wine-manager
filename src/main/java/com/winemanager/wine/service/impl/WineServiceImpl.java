package com.winemanager.wine.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	
	
	private final String apiKey; // final로 지정하면, @RequiredArgsConstructor에 의해 빈을 주입하려해서 오류 발생함 (직접 생성자 작성)
	private final String apiDataArg; // 환율 정보 요청
	private final SSLContext sslContext;

	private final WineMapper wineMapper;
	private final CrawlingService crawlingService;
	private final MessageSource messageSource;
	
	private double exchangeRateUSD = 1300;
	private final String winePicPath;
	
	// 생성자가 1개여서 @AutoWired 생략
	public WineServiceImpl(
			@Value("${koreaexim-exchangeRate-apiKey}") String apiKey,
			@Value("${winePicPath}") String winePicPath,
			@Value("${apiDataArg}") String apiDataArg,
			WineMapper wineMapper,
			CrawlingService crawlingService,
			MessageSource messageSource,
			TrustManager[] trustAllCerts //WebConfig 확인
			) throws NoSuchAlgorithmException, KeyManagementException {
		this.apiKey = apiKey;
		this.winePicPath = winePicPath;
		this.apiDataArg = apiDataArg;
		this.wineMapper = wineMapper;
		this.crawlingService = crawlingService;
		this.messageSource = messageSource;
		
		// 인증서 무시를 위한 사전 작업
		this.sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAllCerts, new SecureRandom());
	}
	
	// 한시간마다 원-달러 환율 업데이트
	@Scheduled(fixedRate = 60 * 60 * 1000) // 1시간에 한번 실행
	public void run() {
		String jsonData = null;
		String requestURL = "https://www.koreaexim.go.kr/site/program/financial/exchangeJSON?authkey=" + apiKey + "&data=" + apiDataArg;
		
		HttpRequest request = HttpRequest.newBuilder()
			    .uri(URI.create(requestURL))
			    .header("accept", "application/json")
			    .method("GET", HttpRequest.BodyPublishers.noBody())
			    .build();
		
		try {
			
			HttpClient client = HttpClient.newBuilder()
										  .sslContext(sslContext)
										  .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)) // 쿠키 허용
										  .followRedirects(Redirect.NORMAL) // 리다이렉트 허용
										  .build();
			
			// 첫 요청에서 Remote host terminated the handshake 오류가 발생하기 때문에 미리 한번 요청 후 처리한다
			try {
				client.send(request, HttpResponse.BodyHandlers.ofString());	
			} catch (Exception e) {
				if(e.getMessage().equals("Remote host terminated the handshake"))
				System.out.println("[환율 업데이트]: Remote host terminated the handshake 오류 발생");
			}
			
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			jsonData = response.body();
			
			ObjectMapper objectMapper = new ObjectMapper();
		    List<Map<String, Object>> result = objectMapper.readValue(jsonData, new TypeReference<List<Map<String,Object>>>() {});
		    
		    if(result == null || result.size() < 1) { // 현재 API는 영업시간 외에 요청하면 null을 리턴함
		    	System.out.println("[환율 업데이트 실패]: 영업시간 외 요청");
		    	return;
		    }
		    
		    for(Map<String, Object> data : result) {
		    	if(data.get("cur_unit").toString().equals("USD")){
		    		double usd = Double.parseDouble(data.get("deal_bas_r").toString().replaceAll(",", ""));
		    		wineMapper.updateExchangeRate(usd);
		    		System.out.println("[환율 업데이트 성공]: " + usd);
		    	}
		    	
		    	// ...추후에 다른 통화 추가 시, 조건 추가
		    }
		} catch (Exception e) {
			System.out.println("[환율 업데이트 중 오류 발생]");
			e.printStackTrace();
		} finally {
			this.exchangeRateUSD = wineMapper.selectExchangeRate();
			System.out.println("[환율 설정]: " + this.exchangeRateUSD);
		}
	}
	
	@Override
	public double getExchangeRateUSD() {
		return exchangeRateUSD;
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
			wine.setSizeToShow(messageSource.getMessage("wine.size." + wine.getSize().toLowerCase(), null, LocaleContextHolder.getLocale()));
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
		
		response.getWine().setSizeToShow(messageSource.getMessage("wine.size." + response.getWine().getSize().toLowerCase(), null, LocaleContextHolder.getLocale()));
		
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
