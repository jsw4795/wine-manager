package com.winemanager.wine.domain;

import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditWineLogRequest {
	
	private Integer wineId;
	
	private String wineName;
	private Integer wineVintage; // null인 경우 NV
	private String wineSize;
	private String wineThumb;
	
	
	private Integer logId;
	private String type;
	@NotBlank(message = "Place must be entered.")
	private String place;
	@NotNull(message = "Date must be entered.")
	private Date date;
	@NotNull(message = "price must be entered.")
	private Integer price;
	@NotNull(message = "Count must be entered.")
	private Integer count;
	@Size(max = 50, message = "Note must be less than 50 characters long.")
	private String note;
	
	private Integer reviewId;
	
	private Double reviewRating;
	private String reviewTitle;
	private String reviewContent;
	private String reviewPhoto;
}
