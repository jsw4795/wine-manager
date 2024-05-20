package com.winemanager.user.service;

import java.util.List;

import com.winemanager.user.domain.MainStats;
import com.winemanager.user.domain.SignUpRequest;
import com.winemanager.user.domain.Timeline;
import com.winemanager.user.domain.TimelineRequest;
import com.winemanager.user.domain.User;
import com.winemanager.user.domain.stats.BottlesByPlace;
import com.winemanager.user.domain.stats.SpendByTime;
import com.winemanager.user.domain.stats.StatsRequest;
import com.winemanager.user.domain.stats.StockByTime;

public interface UserService {

	public User findByUserId(String userId);
	public boolean checkIdExist(String userId);
	public boolean signUp(SignUpRequest signUpRequest);
	
	public MainStats getMainStats(String userId);
	public List<Timeline> getTimeline(TimelineRequest timelineRequest);
	public List<SpendByTime> getSpendByTime(StatsRequest statsRequest);
	public List<StockByTime> getStockByTime(StatsRequest statsRequest);
	public List<BottlesByPlace> getBottleByPlace(StatsRequest statsRequest);
}
