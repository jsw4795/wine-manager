package com.winemanager.user.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimelineRequest {
	private String userId;
	private Integer page;
	private Integer pageSize;
	private String type;
}
