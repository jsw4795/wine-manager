package com.winemanager.wine.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.domain.WineLog;

@Mapper
public interface WineMapper {
	public List<String> selectPlaceById(String userId);
	public void insertNewWine(Wine wine);
	public void insertWineLog(WineLog wineLog);
	public List<Wine> selectWineListWithAllData(String userId);
}
