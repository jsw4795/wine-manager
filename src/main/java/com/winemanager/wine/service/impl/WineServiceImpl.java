package com.winemanager.wine.service.impl;

import org.springframework.stereotype.Service;

import com.winemanager.wine.mapper.WineMapper;
import com.winemanager.wine.service.WineService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WineServiceImpl implements WineService{

	private final WineMapper wineMapper;
	
	
	
}
