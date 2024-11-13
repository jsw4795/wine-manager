package com.winemanager.wine.service.impl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.service.CrawlingService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CrawlingServiceImpl implements CrawlingService, CommandLineRunner, ApplicationListener<ContextClosedEvent>{
	// 사용할 URL
	private final String VIVINO_BASE_URL = "https://www.vivino.com";
	private final String VIVINO_REQUEST_URL = "https://www.vivino.com/search/wines?q=";
	private final String WINE_SEARCHER_REQUEST_URL = "https://www.wine-searcher.com/find/";
	
	// vivino에서 사용할 selector
	private final String VIVINO_CARDS_SELECTOR = ".card.card-lg"; // div태그 여러개 선택됨
	private final String VIVINO_NAME_SELECTOR = ".wine-card__name";
	private final String VIVINO_COUNTRY_SELECTOR = ".wine-card__region [data-item-type=\"country\"]";
	private final String VIVINO_REGION_SELECTOR = ".wine-card__region .link-color-alt-grey";
	private final String VIVINO_AVERAGE_RATING_SELECTOR = ".average__number";
	private final String VIVINO_RATINGS_SELECTOR = ".average__stars .text-micro";
	private final String VIVINO_RATING_REPLACMENT = "ratings";
	private final String VIVINO_LINK_SELECTOR = "a";
	private final String VIVINO_THUMB_SELECTOR = "figure";
	private final String VIVINO_THUMB_REGEX = "[()]";
	private final String VIVINO_BREADCRUMS_SELECTOR = ".breadCrumbs__breadCrumbs--2pkcX"; // div태그 하나 선택됨
	private final String VIVINO_WINERY_SELECTOR = "[data-cy=\"breadcrumb-winery\"]";
	private final String VIVINO_WINE_TYPE_SELECTOR = "[data-cy=\"breadcrumb-winetype\"]";
	private final String VIVINO_GRAPE_SELECTOR = "[data-cy=\"breadcrumb-grape\"]";
	private final String VIVINO_FACTS_ROWS_SELECTOR = "[data-testid=\"wineFactRow\"]"; // tr태그 여러개 선택됨
	private final String VIVINO_FACTS_HEADER_LABEL_SELECTOR = ".wineFacts__headerLabel--14doB"; 
	private final String VIVINO_FACTS_DATA_SELECTOR = ".wineFacts__fact--3BAsi"; 
	
	// wine-searcher에서 사용할 selector
	private final String WINE_SEARCHER_PRICE_SELECTOR = ".prod-profile__avg-price .price .font-light-bold";
	
	// 크롤링 시 설정할 userAgent
	private final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36";
	private final String cookiesPath = System.getProperty("user.dir") + "/cookies/";
	private final String cookiesNameForWS = "wine-searcher_cookie.properties"; // 파일명은 나중에 yml에 저장
	
	// 쿠키 담아놓을 맵
	private Map<String, String> wsCookies = new ConcurrentHashMap<>(); 
	
	
	// 서버 실행 시, 저장된 파일에서 쿠키 데이터 로드
	@Override
	public void run(String... args) throws Exception {
		loadCookies();
	}
	// 서버 종료 시, 쿠키 데이터 파일에 저장
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		saveCookies();
	}
	
	
	public List<Wine> getWineListByName(String keyword) {
		List<Wine> wineList = new ArrayList<>();
		String requestURL = VIVINO_REQUEST_URL + keyword;
		
		long startTime = System.currentTimeMillis();
		
		Response response;
		Document doc;
		try {
			// 요청 날려서 response 받기
			response = Jsoup.connect(requestURL)
						    .method(Connection.Method.GET)
						    .userAgent(USER_AGENT)
						    .execute();
			System.out.println("[vivino-list]요청 소요시간: " + (System.currentTimeMillis() - startTime) + "ms");

			// response에서 document 얻기
			startTime = System.currentTimeMillis();
			doc = response.parse();
		} catch (IOException e) { // 오류 발생하면 빈 List 리턴
			System.out.println("[vivino-list]오류 발생: " + e.getMessage());
			return wineList;
		}
		
		
		// document에서 원하는 elements 얻기
		Elements elements = doc.select(VIVINO_CARDS_SELECTOR);
		
		// 와인 정보가 담겨있는 elements에서 원하는 값만 뽑아서 객체 만들기
		for(Element element : elements) {
			String name = element.select(VIVINO_NAME_SELECTOR).text().trim();
			String link = VIVINO_BASE_URL + element.select(VIVINO_LINK_SELECTOR).attr("href").trim();
			String thumb = element.select(VIVINO_THUMB_SELECTOR).isEmpty()
					? null
					: "https:" + element.select(VIVINO_THUMB_SELECTOR).attr("style").split(VIVINO_THUMB_REGEX)[1];
			String country = element.select(VIVINO_COUNTRY_SELECTOR).isEmpty()
					? null
					: element.select(VIVINO_COUNTRY_SELECTOR).text().trim();
			String region = element.select(VIVINO_REGION_SELECTOR).size() < 2 // 지역이 없으면 하나만 검색됨 (있으면 지역, 국가 순으로 2개 검색)
					? null
					: element.select(VIVINO_REGION_SELECTOR).get(0).text().trim();
			Double averageRating = element.select(VIVINO_AVERAGE_RATING_SELECTOR).size() < 2 // 평가 점수가 없으면 하나만 검색됨 (있으면 평점, 가격 순으로 2개 검색됨)
					? null
					: Double.parseDouble(element.select(VIVINO_AVERAGE_RATING_SELECTOR).get(0).text().trim());
			Integer ratings = element.select(VIVINO_RATINGS_SELECTOR).isEmpty()
					? null
					: Integer.parseInt(element.select(VIVINO_RATINGS_SELECTOR).text().replaceAll(VIVINO_RATING_REPLACMENT, "").trim());
			
			// averagePrice은 wine-searcher에서 가져올거다
			
			Wine wine = Wine.builder()
							.name(name)
							.link(link)
							.thumb(thumb)
							.country(country)
							.region(region)
							.averageRating(averageRating)
							.rating(ratings)
							.build();
			wineList.add(wine);
		}
		// 여기서 thumbBottom도 설정해서 리턴한다
		setWineImages(wineList);
		
		System.out.println("[vivino-list]데이터 가공 소요시간: " + (System.currentTimeMillis() - startTime) + "ms");
		
		return wineList;
	}
	
	public Wine getMoreWineInfo(Wine wine) {
		// vivino에 요청보내서 response 받음 (비동기)
		CompletableFuture<Response> vivinoFuture = CompletableFuture.supplyAsync(() -> requestVivino(wine.getLink()));
		// wine-searcher에 요청보내서 response 받음 (비동기)
		CompletableFuture<Response> wsFuture = CompletableFuture.supplyAsync(() -> requestWineSearcher(wine.getName()));
		// vivino와 wine-searcher에 요청보낸게 끝나면 각 response에서 원하는 정보만 뽑아서 추가한 새로운 Wine객체 생성
		CompletableFuture<Wine> wineFuture = vivinoFuture.thenCombine(wsFuture, (vivinoResponse, wsResponse) -> parseDataToWine(vivinoResponse, wsResponse, wine));
		
		try {
			return wineFuture.get();
		} catch (InterruptedException | ExecutionException e) { // 예외 발생하면 원래 객체 반환
			e.printStackTrace();
			return wine;
		}
	}
	
	
	private Response requestVivino(String link) {
		long startTime = System.currentTimeMillis();
		Response vivinoResponse = null;
		
		// 요청 날려서 response 받기
		try {
			vivinoResponse = Jsoup.connect(link)
								  .method(Connection.Method.GET)
								  .userAgent(USER_AGENT)
								  .execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("[vivino-detail]데이터 요청 소요시간: " + (System.currentTimeMillis() - startTime) + "ms");
		
		return vivinoResponse;
	}
	private Response requestWineSearcher(String wineName) {
		long startTime = System.currentTimeMillis();

		// 와인이름 인코딩해서 요청url 만듦
		String requestURL;
		try {
			requestURL = WINE_SEARCHER_REQUEST_URL + URLEncoder.encode(wineName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			requestURL = WINE_SEARCHER_REQUEST_URL + wineName; // 에러 발생하면 일단 그냥 붙여서 요청함
		} 
		Response wsResponse = null;
		
		// 요청 날려서 response 받기
		try {
			wsResponse = Jsoup.connect(requestURL)
							  .method(Connection.Method.GET)
							  .userAgent(USER_AGENT)
							  .cookies(wsCookies) // wine-searcher는 쿠키 없으면 403으로 차단당함
							  .execute();
			updateCookies(wsResponse); // 쿠키 업데이트
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("[ws-detail]데이터 요청 소요시간: " + (System.currentTimeMillis() - startTime) + "ms");
		
		return wsResponse;
	}
	private Wine parseDataToWine(Response vivinoResponse, Response wsResponse, Wine originWine) {
		String wineType = null;
		String winery = null;
		String grape = null;
		Double alcohol = null;
		Double averagePrice = null;
		
		long startTime = System.currentTimeMillis();
		
		Document vivinoDocument = null;
		Document wsDocument = null;
		
		// response에서 document 얻기
		if(vivinoResponse != null) {
			try {
				vivinoDocument = vivinoResponse.parse();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(wsResponse != null) {
			try {
				wsDocument = wsResponse.parse();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// vivinoResponse가 제대로 얻어졌으면 vivino 데이터 가공 시작
		if(vivinoDocument != null) {
			// document에서 원하는 elements 얻기
			Elements vivinoBreadcrumbs = vivinoDocument.select(VIVINO_BREADCRUMS_SELECTOR); // 상단에 있는 태그 elements
			Elements vivinoFactRows = vivinoDocument.select(VIVINO_FACTS_ROWS_SELECTOR);
			
			wineType = vivinoBreadcrumbs.select(VIVINO_WINE_TYPE_SELECTOR).isEmpty()
					? null
					: vivinoBreadcrumbs.select(VIVINO_WINE_TYPE_SELECTOR).text().split(" ")[0].trim(); // Red wine => Red
			winery = vivinoBreadcrumbs.select(VIVINO_WINERY_SELECTOR).isEmpty()
					? null
					: vivinoBreadcrumbs.select(VIVINO_WINERY_SELECTOR).text().trim();
			grape = vivinoBreadcrumbs.select(VIVINO_GRAPE_SELECTOR).isEmpty()
					? null
					: vivinoBreadcrumbs.select(VIVINO_GRAPE_SELECTOR).text().trim();
			for(Element factRow : vivinoFactRows) {
				if(factRow.select(VIVINO_FACTS_HEADER_LABEL_SELECTOR).text().trim().equalsIgnoreCase("Alcohol content")) {
					alcohol = Double.valueOf(factRow.select(VIVINO_FACTS_DATA_SELECTOR).text().replaceAll("%", "").trim());
				}
			}
		}
		
		// 와인서쳐 response가 제대로 얻어졌으면 와인서쳐 데이터 가공 시작
		if(wsDocument != null) {
			Elements wsPriceElement = wsDocument.select(WINE_SEARCHER_PRICE_SELECTOR); // 하나만 검색됨
			averagePrice = wsPriceElement.size() < 1 
						? null
						: Double.valueOf(wsPriceElement.get(0).text().replaceAll(",", "").trim());
		}
		
		System.out.println("[vivino/ws-detail]데이터 가공 소요시간: " + (System.currentTimeMillis() - startTime) + "ms");
		
		return Wine.builder()
			  	   .name(originWine.getName())
				   .link(originWine.getLink())
				   .thumb(originWine.getThumb())
				   .thumbBottom(originWine.getThumbBottom())
				   .country(originWine.getCountry())
				   .region(originWine.getRegion())
				   .averageRating(originWine.getAverageRating())
				   .rating(originWine.getRating())
				   .wineType(wineType)
				   .averagePrice(averagePrice)
				   .alcohol(alcohol)
				   .grape(grape)
				   .winery(winery)
				   .build();
	}
	
	private void setWineImages(List<Wine> wineList) {
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
	}
	
	
	
	
	
	
	
	// 파일에 저장된 쿠키 불러오기
	private void loadCookies() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(cookiesPath + cookiesNameForWS));
			for (String key : properties.stringPropertyNames()) {
				wsCookies.put(key, properties.get(key).toString());
			}
			System.out.println("쿠키 로드 완료: " + wsCookies);
		} catch (IOException e) {
			System.out.println("[쿠키 로드중 오류 발생] : " + e.getMessage());
		}
	}
	// 쿠키 정보 업데이트
	private void updateCookies(Response wsRepoonse) {
		Map<String, String> newCookies = wsRepoonse.cookies();
		
		for(String key : newCookies.keySet()) {
			wsCookies.put(key, newCookies.get(key));
		}
		System.out.println("쿠키 업데이트 완료");
	}
	// 쿠키 정보를 파일로 저장
	private void saveCookies() {
		Properties properties = new Properties();
		properties.putAll(wsCookies);
		try {
			properties.store(new FileOutputStream(cookiesPath + cookiesNameForWS), null);
			System.out.println("쿠키 저장 완료");
		} catch (IOException e) {
			System.out.println("[쿠키 저장중 오류 발생] : " + e.getMessage());
		}
	}
	
}
