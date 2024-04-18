package com.winemanager.wine.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.winemanager.wine.domain.MyWineRequest;
import com.winemanager.wine.domain.Review;
import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.domain.WineDetailResponse;
import com.winemanager.wine.domain.WineLog;

@Mapper
public interface WineMapper {
	public List<String> selectPlaceById(String userId);
	public void insertPlace(Map<String, Object> param);
	public void insertNewWine(Wine wine);
	public void insertWineLog(WineLog wineLog);
	public List<Wine> selectWineListByName(Map<String, Object> param);
	public int selectCountOfMyWine(MyWineRequest myWineRequest);
	public List<Wine> selectMyWine(MyWineRequest myWineRequest);
	public WineDetailResponse selectWineDetail(Wine wine);
	public String selectUserIdByWineId(int wineId);
	public Wine selectWineById(int wineId);
	public void insertReview(Review review);
	public void updateExchangeRate(Double usd);
	public void updateWine(Wine wine);
	public WineLog selectWineLog(WineLog wineLog);
	public void updateWineLog(WineLog wineLog);
}
