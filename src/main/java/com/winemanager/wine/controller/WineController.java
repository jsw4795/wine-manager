package com.winemanager.wine.controller;

import java.security.Principal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.winemanager.wine.domain.AddWineRequest;
import com.winemanager.wine.domain.DrinkWineRequest;
import com.winemanager.wine.domain.EditReviewRequest;
import com.winemanager.wine.domain.EditWineLogRequest;
import com.winemanager.wine.domain.MyWineRequest;
import com.winemanager.wine.domain.Review;
import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.domain.WineDetailRequest;
import com.winemanager.wine.domain.WineDetailResponse;
import com.winemanager.wine.domain.WineLog;
import com.winemanager.wine.service.WineService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class WineController {
	
	private final List<String> validFileExtension = new ArrayList<>(Arrays.asList(new String[] {".jpg", ".jpeg", ".png", ".webp"}));
	
	private final WineService wineService;
	
	@GetMapping("/")
	public String getIndex(Principal principal, Model model) {
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		return "index";
	}
	
	@GetMapping("/about")
	public String getAbout(Principal principal, Model model) {
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		return "about";
	}
		
	
	// 와인 등록 폼
	@GetMapping("/add-wine")
	public String getAddWine(@ModelAttribute AddWineRequest addWineRequest, BindingResult result,
							Principal principal, Model model) {
		
		// 와인 아이디가 주어지면, 내 와인일 때 자동 정보 입력
		if(addWineRequest.getWineId() != null) {
			Wine wine = wineService.getWine(addWineRequest.getWineId()); 
			setWineImage(wine);
			
			if(wine != null && wine.getUserId().equals(principal.getName())) {
				addWineRequest.setWineId(wine.getWineId());
				addWineRequest.setWineName(wine.getName());
				addWineRequest.setWineVintage(wine.getVintage());
				addWineRequest.setWineCountry(wine.getCountry());
				addWineRequest.setWineRegion(wine.getRegion());
				addWineRequest.setWineAverageRating(wine.getAverageRating());
				addWineRequest.setWineRating(wine.getRating());
				addWineRequest.setWineSize(wine.getSize());
				addWineRequest.setWineType(wine.getWineType());
				addWineRequest.setWineAveragePrice(wine.getAveragePrice());
				addWineRequest.setWineThumb(wine.getThumb());
				addWineRequest.setWineThumbBottom(wine.getThumbBottom());
				addWineRequest.setWineLink(wine.getLink());
			} else {
				addWineRequest.setWineId(null);
			}
		}
		
		List<String> placeList = wineService.getBuyPlace(principal.getName());
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("placeList", placeList);
		
		return "wine/add-wine";
	}
	// 와인 등록 실행
	@PostMapping("/add-wine")
	public String addWine(@Valid @ModelAttribute AddWineRequest addWineRequest, BindingResult result,
						Principal principal, Model model) {
		
		if(result.hasErrors())
			return "wine/add-wine";
		
		String userId = principal.getName();
		int wineId = 0;

		if(addWineRequest.getWineId() == null) // 새로운 와인 등록
			wineId = wineService.addNewWine(addWineRequest, userId);
		 else { // 원래 있던 와인 추가
			 if(wineService.isMyWine(addWineRequest.getWineId(), userId)) // 본인 와인일 때만 추가함
				 wineId = wineService.addBuyWineLog(addWineRequest, userId);			 
		 }
		
		
		return "redirect:/wine/" + wineId;
	}
	@GetMapping("/add-wine/my-wine-list")
	public String getMyWineList(Principal principal, String keyword, Model model) {
		if(keyword == null || keyword.trim().length() == 0)
			return "wine/emptyResultTemplate-add-wine";
		
		List<Wine> wineList = wineService.getWineListByWineName(keyword, principal.getName());
		setWineImage(wineList);
		
		if(wineList == null || wineList.size() == 0)
			return "wine/emptyResultTemplate-add-wine";
		
		model.addAttribute("wineList", wineList);
		model.addAttribute("type", "old");
		
		return "wine/searchResultTemplate-add-wine";
	}
	@GetMapping("/add-wine/search-new")
	public String searchWineInVivino(String keyword, String uuid, Model model) {
		if(keyword == null || keyword.trim().length() == 0) {
			model.addAttribute("uuid", uuid);
			return "wine/emptyResultTemplate-add-wine";
		}
		
		List<Wine> wineList = wineService.searchWineInVivino(keyword);
		setWineImage(wineList);
		
		if(wineList == null || wineList.size() == 0)
			return "wine/emptyResultTemplate-add-wine";
		
		model.addAttribute("wineList", wineList);
		model.addAttribute("type", "new");
		model.addAttribute("uuid", uuid);
		
		return "wine/searchResultTemplate-add-wine";
	}
	@PostMapping("/add-place")
	public String addPlace(String place, Principal principal, Model model) {
		boolean isBuyPlaceValid = false;
		String errorMessage = "An unknown error occurred.";
		
		List<String> buyPlaceList = null;
		List<String> buyPlaceListOrigin = null;
		
		// 입력받은 장소 길이 체크
		if(place == null || place.length() < 1 || place.length() > 30) {
			errorMessage = "Place name must be at least 1 character and not more than 30 characters.";
		} else {
			// 이미 등록된 장소인지 확인
			buyPlaceListOrigin = wineService.getBuyPlace(principal.getName());
			if(buyPlaceListOrigin.contains(place)) {
				errorMessage = "The place already exist.";
			} else {
				isBuyPlaceValid = true;
			}
		}
		
		if(isBuyPlaceValid) {
			// 장소 등록 후, 새로운 값 받아오기
			wineService.insertBuyPlace(place, principal.getName());
			buyPlaceList = wineService.getBuyPlace(principal.getName());
		}
		
		model.addAttribute("buyPlaceList", buyPlaceList);
		model.addAttribute("isBuyPlaceValid", isBuyPlaceValid);
		model.addAttribute("errorMessage", errorMessage);
		
		return "wine/placeOptionTemplate-add-wine";
	}
	
	
	@GetMapping("/drink-wine")
	public String getDrinkWine(@ModelAttribute DrinkWineRequest drinkWineRequest, BindingResult result, Principal principal, Model model) {
		
		if(drinkWineRequest.getWineId() != null) {
			Wine wine = wineService.getWine(drinkWineRequest.getWineId());
			setWineImage(wine);
			if(wine != null && wine.getUserId().equals(principal.getName())) {
				drinkWineRequest.setWineId(wine.getWineId());
				drinkWineRequest.setWineName(wine.getName());
				drinkWineRequest.setWineVintage(wine.getVintage());
				drinkWineRequest.setWineSize(wine.getSize());
				drinkWineRequest.setWineThumb(wine.getThumb());
				
				model.addAttribute("wineCount", wine.getCount());
			} else {
				drinkWineRequest.setWineId(null);
			}
		}
		
		List<String> placeList = wineService.getBuyPlace(principal.getName());
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("placeList", placeList);
		
		return "wine/drink-wine";
	}
	@PostMapping("/drink-wine")
	public String drinkWine(@Valid @ModelAttribute DrinkWineRequest drinkWineRequest, BindingResult result,
						Principal principal, Model model) {
		
		if (result.hasErrors())
			return "wine/drink-wine";

		String userId = principal.getName();
		int wineId = 0;
		
		Wine wine = wineService.getWine(drinkWineRequest.getWineId());
		
		if(wine == null) {
			result.rejectValue("wineId", "errer.wineId", "Wine not found.");
			return "wine/drink-wine";
		}
		
		if(drinkWineRequest.getDrinkCount() > wine.getCount()) {
			result.rejectValue("drinkCount", "errer.drinkCount", "The wine you drank can't be more than stocked.");
			return "wine/drink-wine";
		}
		
		if (wine.getUserId().equals(userId))  // 본인 와인일 때만 추가함
			 wineId = wineService.drinkWine(drinkWineRequest, userId);
		
		return "redirect:/wine/" + wineId;
	}
	
	// 와인 수정 폼
	@GetMapping("/edit-wine/{wineId}")
	public String getEditWine(@ModelAttribute AddWineRequest addWineRequest, BindingResult result, @PathVariable int wineId,
							Principal principal, Model model) {
		
		// 내 와인일 때 만
		Wine wine = wineService.getWine(addWineRequest.getWineId());
		setWineImage(wine);
		if(wine != null && wine.getUserId().equals(principal.getName())) {
			addWineRequest.setWineId(wine.getWineId());
			addWineRequest.setWineName(wine.getName());
			addWineRequest.setWineVintage(wine.getVintage());
			addWineRequest.setWineCountry(wine.getCountry());
			addWineRequest.setWineRegion(wine.getRegion());
			addWineRequest.setWineAverageRating(wine.getAverageRating());
			addWineRequest.setWineRating(wine.getRating());
			addWineRequest.setWineSize(wine.getSize());
			addWineRequest.setWineType(wine.getWineType());
			addWineRequest.setWineAveragePrice(wine.getAveragePrice());
			addWineRequest.setWineThumb(wine.getThumb());
			addWineRequest.setWineThumbBottom(wine.getThumbBottom());
			addWineRequest.setWineLink(wine.getLink());
		} else {
			model.addAttribute("errorMessage", "You do not have access.");
			return "error";
		}
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		
		return "wine/edit-wine";
	}
	@PostMapping("edit-wine/{wineId}")
	public String editWine(@ModelAttribute AddWineRequest addWineRequest, BindingResult result,
							Principal principal, Model model) {
		
		// 파일 체크
		if(!addWineRequest.getCustomImage().isEmpty()) {
			MultipartFile inputFile = addWineRequest.getCustomImage();
			// 파일 확장자 체크
			String fileExtension = inputFile.getOriginalFilename().substring(inputFile.getOriginalFilename().lastIndexOf("."));
			
			boolean isExtensionValid = false;
			for(String validFileExtension : this.validFileExtension) {
				if(fileExtension.equalsIgnoreCase(validFileExtension))
					isExtensionValid = true;
			}
			
			if(!isExtensionValid) {
				result.rejectValue("customImage", "errer.customImage", "Only extensions of jpg, jpeg, and png are allowed.");
				return "wine/edit-wine";
			}
			
			
			// 파일 크기 체크
			if(inputFile.getSize() > 15 * 1024 * 1024) {
				result.rejectValue("customImage", "errer.customImage", "The file size is up to 15MB.");
				return "wine/edit-wine";
			}
			
		}
		
		// 내 와인인지 체크 후, 로직 실행
		Wine wine = wineService.getWine(addWineRequest.getWineId());
		if(wine != null && wine.getUserId().equals(principal.getName())) {
			wineService.editWine(addWineRequest, principal.getName());
		} else {
			model.addAttribute("errorMessage", "You do not have access.");
			return "error";
		}
		
		return "redirect:/wine/" + addWineRequest.getWineId();
	}
	
	
	@GetMapping("/my-wine")
	public String redirectToMyWine() {
		return "redirect:/my-wine/all?sortBy=price_asc";
	}
	@GetMapping("/my-wine/{type}")
	public String getMyWine(MyWineRequest myWineRequest, Principal principal, HttpServletRequest request, Model model) {
		
		List<Wine> wineList = wineService.getMyWineList(myWineRequest, principal.getName());
		setWineImage(wineList);
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("myWineRequest", myWineRequest);
		model.addAttribute("wineList", wineList);
		model.addAttribute("pagination", myWineRequest.getPagination());
		
		return "wine/my-wine";
	}
	@GetMapping("/wine/{wineId}")
	public String getWineDetail(@PathVariable(name = "wineId") Integer wineId, Principal principal,
							 Model model) {
		// 내 와인이 아닌것은 볼 수 없다
		if(!wineService.isMyWine(wineId, principal.getName())) {
			String errorMessage = "You do not have access.";
			model.addAttribute("errorMessage", errorMessage);
			return "error";
		}
		
		WineDetailResponse response = wineService.getWineDetail(wineId, principal.getName());
		setWineImage(response.getWine());
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("response", response);
		model.addAttribute("exchangeRate", wineService.getExchangeRate());
		
		return "wine/wine-detail";
	}
	@GetMapping("/wine/load")
	public String getWineLogInWineDetail(WineDetailRequest wineDetailRequest, Principal principal, Model model) {
		wineDetailRequest.setUserId(principal.getName());
		model.addAttribute("wineDetailRequest", wineDetailRequest);
		
		if(wineDetailRequest.getType().equalsIgnoreCase("log")) {
			List<WineLog> wineLogList = wineService.getWineLogList(wineDetailRequest);
			model.addAttribute("wineLogList", wineLogList);
			return "/wine/wineLogResultTemplate-wine-detail";
			
		} else if (wineDetailRequest.getType().equalsIgnoreCase("review")) {
			List<Review> reviewList = wineService.getWineReviewList(wineDetailRequest);
			model.addAttribute("reviewList", reviewList);
			return "/wine/reviewResultTemplate-detail";
			
		}
		
		return null;
	}
	
	@GetMapping("/edit-wineLog/{logId}")
	public String getEditWineLog(@PathVariable(name = "logId", required = true) Integer logId, Principal principal,
							@ModelAttribute EditWineLogRequest editWineLogRequest, BindingResult result,
							HttpServletRequest request, Model model) {
		WineLog wineLog = wineService.getWineLog(logId, principal.getName());
		Wine wine = wineService.getWine(wineLog.getWineId());
		setWineImage(wine);
		
		editWineLogRequest = EditWineLogRequest.builder()
											   .wineId(wine.getWineId())
											   .wineName(wine.getName())
											   .wineVintage(wine.getVintage())
											   .wineSize(wine.getSize())
											   .wineThumb(wine.getThumb())
											   .logId(wineLog.getLogId())
											   .type(wineLog.getType())
											   .place(wineLog.getPlace())
											   .date(wineLog.getDate())
											   .count(wineLog.getCount())
											   .price(wineLog.getPrice())
											   .build();
		
		
		List<String> buyPlaceList = wineService.getBuyPlace(principal.getName());
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute(editWineLogRequest);
		model.addAttribute("placeList", buyPlaceList);
		
		return "/wine/edit-wine-log";
	}
	
	@PostMapping("/edit-wine-log")
	public String editWineLog(@ModelAttribute EditWineLogRequest editWineLogRequest, BindingResult result,
			Principal principal, Model model) {
		
		if(result.hasErrors())
			return "/wine/edit-wine-log";
		
		wineService.editWineLog(editWineLogRequest, principal.getName());	
		
		
		return "redirect:/wine/" + editWineLogRequest.getWineId();
	}
	
	@GetMapping("/edit-review/{reviewId}")
	public String getEditReview(@PathVariable(name = "reviewId", required = true) Integer reviewId, Principal principal,
			@ModelAttribute EditReviewRequest editReviewRequest, BindingResult result, Model model) {
		Review review = wineService.getReivew(reviewId, principal.getName());
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("review", review);
		
		return "/wine/edit-review";
	}
	
	@PostMapping("/edit-review")
	public String editReview(@ModelAttribute EditReviewRequest editReviewRequest, BindingResult result, Principal principal, Model model) {
		
		if(result.hasErrors())
			return "/wine/edit-wine-log";
		
		wineService.editReview(editReviewRequest, principal.getName());
		
		return "redirect:/wine/" + editReviewRequest.getWineId();
	}
	
	
	
	
	private void setWineImage(Wine wine) {
		if(wine == null)
			return;
		
		if(!wine.getThumb().startsWith("https://")) {
			wine.setThumb("/images/wine/" + wine.getThumb());
			wine.setThumbBottom("/images/wine/" + wine.getThumbBottom());
		}
	}
	private void setWineImage(List<Wine> wineList) {
		if(wineList == null)
			return;
		
		for(Wine wine : wineList) 
			this.setWineImage(wine);
	}
	
	
	// String으로 넘어오는 Date 바인딩
	@InitBinder
	private void dateBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new
	    SimpleDateFormat("yyyy-MM-dd"); //날짜 형식은 알아서 정하기
	    CustomDateEditor editor = new CustomDateEditor(dateFormat, true);
	    binder.registerCustomEditor(Date.class, editor);
	}
	
}
