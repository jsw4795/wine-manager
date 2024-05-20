package com.winemanager.user.service.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winemanager.user.domain.MainStats;
import com.winemanager.user.domain.SignUpRequest;
import com.winemanager.user.domain.Timeline;
import com.winemanager.user.domain.TimelineRequest;
import com.winemanager.user.domain.User;
import com.winemanager.user.domain.stats.BottlesByPlace;
import com.winemanager.user.domain.stats.SpendByTime;
import com.winemanager.user.domain.stats.StatsRequest;
import com.winemanager.user.domain.stats.StockByTime;
import com.winemanager.user.mapper.UserMapper;
import com.winemanager.user.service.UserService;
import com.winemanager.wine.domain.WineLog;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{
	
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	
	@Transactional(readOnly = true)
	@Override
	public User findByUserId(String userId) {
		return userMapper.selectUserById(User.builder()
											 .userId(userId)
											 .build());
	}

	@Transactional(readOnly = true)
	@Override
	public boolean checkIdExist(String userId) {
		User user = userMapper.selectUserById(User.builder()
												  .userId(userId)
												  .build());
		return user != null;
	}

	@Transactional
	@Override
	public boolean signUp(SignUpRequest signUpRequest) {
		try {
			userMapper.insertUser(User.builder()
					.userId(signUpRequest.getUserId().trim())
					.password(passwordEncoder.encode(signUpRequest.getPassword().trim()))
					.email(signUpRequest.getEmail().trim())
					.nickname(signUpRequest.getNickname().trim())
					.build());			
		} catch (Exception e) {
			System.out.println("에러..! : " + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public MainStats getMainStats(String userId) {
		return userMapper.selectMainStats(userId); 
	}

	@Override
	public List<Timeline> getTimeline(TimelineRequest timelineRequest) {
		return userMapper.selectTimeline(timelineRequest);
	}

	@Override
	public List<SpendByTime> getSpendByTime(StatsRequest statsRequest) {
		return userMapper.selectSpendByMonth(statsRequest);
	}

	@Override
	public List<StockByTime> getStockByTime(StatsRequest statsRequest) {
		// 재고까지 sql에서 뽑아옴
		List<StockByTime> stockByTimeList = userMapper.selectStockByDate(statsRequest);
		return stockByTimeList;
	}

	@Override
	public List<BottlesByPlace> getBottleByPlace(StatsRequest statsRequest) {
		List<BottlesByPlace> bottlesByPlaceList = userMapper.selectBottlesByPlace(statsRequest);
		
		// 기타가 없으면 기타 열 삭제
		if(bottlesByPlaceList.get(bottlesByPlaceList.size()-1).getCount() == 0)
			bottlesByPlaceList.remove(bottlesByPlaceList.size()-1);
		
		return bottlesByPlaceList;
	}
	
	
}
