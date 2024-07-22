package com.winemanager.wine.domain;

import java.sql.Date;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DrinkWineRequest {
	@NotNull(message = "You must choice wine.")
	private Integer wineId;
	
	private String wineName;
	private Integer wineVintage; // null인 경우 NV
	private String wineSize;
	private String wineThumb;
	
	
	private Integer wineLogId;
	
	@NotBlank(message = "Place of drinking must be entered.")
	private String drinkPlace;
	@NotNull(message = "Date of drinkning must be entered.")
	private Date drinkDate;
	@NotNull(message = "The drinking quantity must be entered.")
	@Min(value = 1, message = "The number of drinks must be greater than zero.")
	private Integer drinkCount;
	@Size(max = 50, message = "Note must be less than 50 characters long.")
	private String note;
	
	
	private Integer reviewId;
	
	private Double reviewRating;
	private String reviewTitle;
	private String reviewContent;
	private String reviewPhoto;
	
}
