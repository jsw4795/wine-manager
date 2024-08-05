package com.winemanager.wine.service;

import java.util.List;

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
	public Integer drinkWine(DrinkWineRequest drinkWineRequest, String userId);
	public void editWine(AddWineRequest addWineRequest, String userId);
	public WineLog getWineLog(int logId, String userId);
	public void editWineLog(EditWineLogRequest editLogRequest, String userId);
	public List<WineLog> getWineLogList(WineDetailRequest wineDetailRequest);
	public List<Review> getWineReviewList(WineDetailRequest wineDetailRequest);
	public Review getReivew(int reviewId, String userId);
	public void editReview(EditReviewRequest editReviewRequest, String userId);
	public void deleteWineLog(EditWineLogRequest editWineLogRequest, String userId);
	public void deleteWine(AddWineRequest addWineRequest, String userId);
	
	public double getExchangeRate();
}
