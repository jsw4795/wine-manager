package com.winemanager.wine.domain;

import java.sql.Date;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddWineRequest {
	
	private Integer wineId;
	
	@NotBlank(message = "Wine name must be entered.")
	@Size(max = 100, message = "Wine name must be not more than 100 characters long.")
	private String wineName;
	private Integer wineVintage; // null인 경우 NV
	private String wineCountry;
	private String wineRegion;
	private Double wineAverageRating;
	private Integer wineRating;
	@NotNull(message = "Wine size must be entered.")
	private String wineSize;
	@NotNull(message = "Wine type must be entered.")
	private String wineType;
	private Double wineAveragePrice;
	private String wineThumb;
	private String wineThumbBottom;
	private String wineLink;
	private Double wineAlcohol;
	private String wineGrape;
	private String wineWinery;
	
	MultipartFile customImage;
	
	
	@NotBlank(message = "Place of purchase must be entered.")
	private String buyPlace;
	@NotNull(message = "Date of purchase must be entered.")
	private Date buyDate;
	@NotNull(message = "Purchase price must be entered.")
	private Integer buyPrice;
	@NotNull(message = "The purchase quantity must be entered.")
	private Integer buyCount;
	@Size(max = 50, message = "Note must be less than 50 characters long.")
	private String note;
	
}
