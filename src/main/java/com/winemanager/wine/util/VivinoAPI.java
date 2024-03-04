package com.winemanager.wine.util;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winemanager.wine.domain.Wine;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class VivinoAPI implements CommandLineRunner, ApplicationListener<ContextClosedEvent> {
	
	private final ObjectMapper mapper = new ObjectMapper(); // json -> 객체 변환하는 객체 (thread-safe)
	
	private final String rootDir = System.getProperty("user.dir"); // 프로젝트 루트 디렉토리
	private final String nodeDir = "/usr/local/bin/node"; // 설치된 노드 위치 (환경변수를 못읽음)
	private Process browserProcess;
	

	// 서버 실행 시, 브라우저 실행 및 설정
	@Override
	public void run(String... args) throws Exception {
		ProcessBuilder pb = new ProcessBuilder(nodeDir, rootDir + "/src/main/resources/api/vivino-api-main/launch-browser.js"); // 로직 설정
		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		pb.redirectError(ProcessBuilder.Redirect.INHERIT);
		browserProcess = pb.start();
	}
	// 서버 종료 시, 브라우저 종료
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		browserProcess.destroy();
		System.out.println("브라우저 종료");
	}
	
	
	// 검색해서 와인 데이터 가져오는 로직
	public List<Wine> getWineListByName(String keyword) throws IOException{
		ProcessBuilder pb = new ProcessBuilder(nodeDir, rootDir + "/src/main/resources/api/vivino-api-main/vivino.js", "--name=" + keyword); // 로직 설정
		
		String jsonResult = null;
		List<Wine> wineList = null;
		
		long startTime = System.currentTimeMillis(); // 소요 시간 체크용
		
		Process p = pb.start(); // 로직 실행
		
		jsonResult = new String(p.getInputStream().readAllBytes()); // json결과를 저장
		wineList = mapper.readValue(jsonResult, new TypeReference<List<Wine>>() {}); // json -> 객체 변환
		
		long endTime = System.currentTimeMillis(); // 소요 시간 체크용
		System.out.println("검색 시간: " + (endTime - startTime) + "ms"); // 소요 시간 체크용
		
		return wineList;
	}

	
	
}
