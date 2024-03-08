package com.winemanager.wine.service;

import java.util.List;

import com.winemanager.wine.domain.AddWineRequest;
import com.winemanager.wine.domain.MyWineRequest;
import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.domain.WineDetailResponse;

public interface WineService {
	public List<String> getBuyPlace(String userId);
	public void insertBuyPlace(String place, String userId);
	public Integer addNewWine(AddWineRequest addWineRequest, String userId);
	public Integer addBuyWineLog(AddWineRequest addWineRequest, String userId);
	public List<Wine> getWineListByWineName(String keyword, String userId);
	public List<Wine> searchWineInVivino(String keyword);
	public List<Wine> getMyWineList(MyWineRequest myWineRequest, String userId);
	public WineDetailResponse getWineDetail(int wineId, String userId);
	public boolean isMyWine(int wineId, String userId);
	public Wine getWine(int wineId);
	
	public double getExchangeRate();
}
