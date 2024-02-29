package com.winemanager.security.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.winemanager.security.domain.SecurityUser;

@Mapper
public interface SecurityMapper {
	
	public SecurityUser selectUserById(String userId);
	
}
