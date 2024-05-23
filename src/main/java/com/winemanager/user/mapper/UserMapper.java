package com.winemanager.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.winemanager.user.domain.MainStats;
import com.winemanager.user.domain.Timeline;
import com.winemanager.user.domain.TimelineRequest;
import com.winemanager.user.domain.User;
import com.winemanager.user.domain.stats.WineByPlace;
import com.winemanager.user.domain.stats.WineByPrice;
import com.winemanager.user.domain.stats.WineByType;
import com.winemanager.user.domain.stats.SpendByTime;
import com.winemanager.user.domain.stats.StatsRequest;
import com.winemanager.user.domain.stats.StockByTime;
import com.winemanager.user.domain.stats.WineByCountry;

@Mapper
public interface UserMapper {

	public User selectUserById(User user);
	public void insertUser(User user);
	
	public MainStats selectMainStats(String userId);
	public List<Timeline> selectTimeline(TimelineRequest timelineRequest);
	public List<SpendByTime> selectSpendByMonth(StatsRequest statsRequest);
	public List<StockByTime> selectStockByDate(StatsRequest statsRequest);
	public List<WineByPlace> selectWineByPlace(StatsRequest statsRequest);
	public List<WineByType> selectWineByType(StatsRequest statsRequest);
	public List<WineByCountry> selectWineByCountry(StatsRequest statsRequest);
	public List<WineByPrice> selectWineByPrice(StatsRequest statsRequest);
}
