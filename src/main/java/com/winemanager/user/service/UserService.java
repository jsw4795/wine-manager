package com.winemanager.user.service;

import java.util.List;

import com.winemanager.user.domain.MainStats;
import com.winemanager.user.domain.SignUpRequest;
import com.winemanager.user.domain.Timeline;
import com.winemanager.user.domain.TimelineRequest;
import com.winemanager.user.domain.User;

public interface UserService {

	public User findByUserId(String userId);
	public boolean checkIdExist(String userId);
	public boolean signUp(SignUpRequest signUpRequest);
	
	public MainStats getMainStats(String userId);
	public List<Timeline> getTimeline(TimelineRequest timelineRequest);
}
