package com.winemanager.wine.service;

import java.util.List;

import com.winemanager.wine.domain.AddWineRequest;
import com.winemanager.wine.domain.Wine;

public interface WineService {
	public List<String> getBuyPlace(String userId);
	public Integer addNewWine(AddWineRequest addWineRequest, String userId);
	public Integer addBuyWineLog(AddWineRequest addWineRequest, String userId);
	public List<Wine> getWineListByWineName(String keyword, String userId);
	public List<Wine> searchWineInVivino(String keyword);
}
