package com.winemanager.wine.service;

import java.util.List;

import com.winemanager.wine.domain.Wine;

public interface CrawlingService {
	/**
	 * 
	 * @param keyword
	 * @return 키워드에 따른 와인 리스트 반환 (예외 발생 시 빈 리스트 리턴)
	 */
	public List<Wine> getWineListByName(String keyword);
	
	/**
	 * 
	 * @param wine
	 * @return 와인에 정보를 추가하여 반환 (예외 발생 시 원래 wine객체 리턴)
	 */
	public Wine getMoreWineInfo(Wine wine);
}
