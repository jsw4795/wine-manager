package com.winemanager.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.winemanager.user.domain.MainStats;
import com.winemanager.user.domain.Timeline;
import com.winemanager.user.domain.TimelineRequest;
import com.winemanager.user.domain.User;

@Mapper
public interface UserMapper {

	public User selectUserById(User user);
	public void insertUser(User user);
	
	public MainStats selectMainStats(String userId);
	public List<Timeline> selectTimeline(TimelineRequest timelineRequest);
}
