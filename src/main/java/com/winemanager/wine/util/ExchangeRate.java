package com.winemanager.wine.util;

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
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winemanager.wine.mapper.WineMapper;

@Component
public class ExchangeRate {
	
	private final String apiKey; // final로 지정하면, @RequiredArgsConstructor에 의해 빈을 주입하려해서 오류 발생함 (직접 생성자 작성)
	private final String apiDataArg; // 환율 정보 요청
	private final SSLContext sslContext;
	
	private final WineMapper wineMapper;
	
	private double exchangeRateUSD = 1300;
	
	public ExchangeRate(@Value("${apiDataArg}") String apiDataArg,
						@Value("${koreaexim-exchangeRate-apiKey}") String apiKey,
						TrustManager[] trustAllCerts, //WebConfig 확인
						WineMapper wineMapper
						) throws NoSuchAlgorithmException, KeyManagementException {
		this.apiKey = apiKey;
		this.apiDataArg = apiDataArg;
		this.wineMapper = wineMapper;
		
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
	
	
	
	public double getUSD() {
		return exchangeRateUSD;
	}
	
}
