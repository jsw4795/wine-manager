package com.winemanager.wine.controller;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.winemanager.user.domain.User;
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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class WineController {
	
	private final List<String> validFileExtension = new ArrayList<>(Arrays.asList(new String[] {".jpg", ".jpeg", ".png", ".webp"}));
	
	private final WineService wineService;
	private final MessageSource messageSource;
	
	@ModelAttribute
	public void load(Model model, @AuthenticationPrincipal User user){
		if(user == null)
			user = new User();
		model.addAttribute("user", user);

	}
	
	@GetMapping("/")
	public String getIndex(HttpSession session, Model model) {
		return "index";
	}
	
	@GetMapping("/about")
	public String getAbout(Model model) {
		return "about";
	}
		
	
	// 와인 등록 폼
	@GetMapping("/add-wine")
	public String getAddWine(@ModelAttribute AddWineRequest addWineRequest, BindingResult result,
			@AuthenticationPrincipal User user, Model model) {
		
		// 와인 아이디가 주어지면, 내 와인일 때 자동 정보 입력
		if(addWineRequest.getWineId() != null) {
			Wine wine = wineService.getWine(addWineRequest.getWineId()); 
			wineService.setWineImage(wine);
			
			if(wine != null && wine.getUserId().equals(user.getUserId())) {
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
				addWineRequest.setWineAlcohol(wine.getAlcohol());
				addWineRequest.setWineGrape(wine.getGrape());
				addWineRequest.setWineWinery(wine.getWinery());
			} else {
				addWineRequest.setWineId(null);
			}
		}
		
		List<String> placeList = wineService.getBuyPlace(user.getUserId());
		
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		model.addAttribute("currentYear", currentYear);
		
		model.addAttribute("placeList", placeList);
		
		return "wine/add-wine";
	}
	// 와인 등록 실행
	@PostMapping("/add-wine")
	public String addWine(@Valid @ModelAttribute AddWineRequest addWineRequest, BindingResult result,
			@AuthenticationPrincipal User user, Model model) {
		
		if(result.hasErrors())
			return "wine/add-wine";
		
		int wineId = 0;

		if(addWineRequest.getWineId() == null) // 새로운 와인 등록
			wineId = wineService.addNewWine(addWineRequest, user.getUserId());
		 else { // 원래 있던 와인 추가
			 if(wineService.isMyWine(addWineRequest.getWineId(), user.getUserId())) // 본인 와인일 때만 추가함
				 wineId = wineService.addBuyWineLog(addWineRequest, user.getUserId());			 
		 }
		
		
		return "redirect:/wine/" + wineId;
	}
	@GetMapping("/add-wine/my-wine-list")
	public String getMyWineList(@AuthenticationPrincipal User user, String keyword, String uuid, Model model) {
		if(keyword == null || keyword.trim().length() == 0) {
			model.addAttribute("uuid", uuid);
			return "wine/emptyResultTemplate-add-wine";
		}
		
		List<Wine> wineList = wineService.getWineListByWineName(keyword, user.getUserId());
		wineService.setWineImage(wineList);
		
		if(wineList == null || wineList.size() == 0) {
			model.addAttribute("uuid", uuid);			
			return "wine/emptyResultTemplate-add-wine";
		}
		
		model.addAttribute("wineList", wineList);
		model.addAttribute("type", "old");
		model.addAttribute("uuid", uuid);
		
		return "wine/searchResultTemplate-add-wine";
	}
	@GetMapping("/add-wine/search-new")
	public String searchWineInVivino(String keyword, String uuid, Model model) {
		if(keyword == null || keyword.trim().length() == 0) {
			model.addAttribute("uuid", uuid);
			return "wine/emptyResultTemplate-add-wine";
		}
		
		List<Wine> wineList = wineService.searchWineInVivino(keyword);
		wineService.setWineImage(wineList);
		
		if(wineList == null || wineList.size() == 0) {
			model.addAttribute("uuid", uuid);
			return "wine/emptyResultTemplate-add-wine";			
		}
		
		model.addAttribute("wineList", wineList);
		model.addAttribute("type", "new");
		model.addAttribute("uuid", uuid);
		
		return "wine/searchResultTemplate-add-wine";
	}
	@GetMapping("/add-wine/search-new/more-info")
	@ResponseBody
	public Wine getMoreWineInfo(@ModelAttribute AddWineRequest addWineRequest, @AuthenticationPrincipal User user) {
		Wine wine = wineService.searchWineMoreInfo(addWineRequest);
		return wine;
	}
	@PostMapping("/add-place")
	public String addPlace(String place, @AuthenticationPrincipal User user, Model model) {
		boolean isBuyPlaceValid = false;
		String errorMessage = "An unknown error occurred.";
		
		List<String> buyPlaceList = null;
		List<String> buyPlaceListOrigin = null;
		
		// 입력받은 장소 길이 체크
		if(place == null || place.length() < 1 || place.length() > 30) {
			errorMessage = "Place name must be at least 1 character and not more than 30 characters.";
		} else {
			// 이미 등록된 장소인지 확인
			buyPlaceListOrigin = wineService.getBuyPlace(user.getUserId());
			if(buyPlaceListOrigin.contains(place)) {
				errorMessage = "The place already exist.";
			} else {
				isBuyPlaceValid = true;
			}
		}
		
		if(isBuyPlaceValid) {
			// 장소 등록 후, 새로운 값 받아오기
			wineService.insertBuyPlace(place, user.getUserId());
			buyPlaceList = wineService.getBuyPlace(user.getUserId());
		}
		
		model.addAttribute("buyPlaceList", buyPlaceList);
		model.addAttribute("isBuyPlaceValid", isBuyPlaceValid);
		model.addAttribute("errorMessage", errorMessage);
		
		return "wine/placeOptionTemplate-add-wine";
	}
	// 새로운 와인 등록 폼
	@GetMapping("/add-new-wine")
	public String getAddNewWine(@ModelAttribute AddWineRequest addWineRequest, BindingResult result,
			@AuthenticationPrincipal User user, Model model) {
		
		List<String> placeList = wineService.getBuyPlace(user.getUserId());
		
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		model.addAttribute("currentYear", currentYear);
		
		model.addAttribute("placeList", placeList);
		
		return "wine/add-new-wine";
	}
	
	@GetMapping("/drink-wine")
	public String getDrinkWine(@ModelAttribute DrinkWineRequest drinkWineRequest, BindingResult result, 
			@AuthenticationPrincipal User user, Model model) {
		
		if(drinkWineRequest.getWineId() != null) {
			Wine wine = wineService.getWine(drinkWineRequest.getWineId());
			wineService.setWineImage(wine);
			if(wine != null && wine.getUserId().equals(user.getUserId())) {
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
		
		List<String> placeList = wineService.getBuyPlace(user.getUserId());
		
		model.addAttribute("placeList", placeList);
		
		return "wine/drink-wine";
	}
	@PostMapping("/drink-wine")
	public String drinkWine(@Valid @ModelAttribute DrinkWineRequest drinkWineRequest, BindingResult result,
			@AuthenticationPrincipal User user, Model model) {
		
		if (result.hasErrors())
			return "wine/drink-wine";

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
		
		if (wine.getUserId().equals(user.getUserId()))  // 본인 와인일 때만 추가함
			 wineId = wineService.drinkWine(drinkWineRequest, user.getUserId());
		
		return "redirect:/wine/" + wineId;
	}
	
	// 와인 수정 폼
	@GetMapping("/edit-wine/{wineId}")
	public String getEditWine(@ModelAttribute AddWineRequest addWineRequest, BindingResult result, @PathVariable int wineId,
			@AuthenticationPrincipal User user, Model model) {
		
		// 내 와인일 때 만
		Wine wine = wineService.getWine(addWineRequest.getWineId());
		wineService.setWineImage(wine);
		if(wine != null && wine.getUserId().equals(user.getUserId())) {
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
		
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		model.addAttribute("currentYear", currentYear);
		
		return "wine/edit-wine";
	}
	@PostMapping("edit-wine/{wineId}")
	public String editWine(@ModelAttribute AddWineRequest addWineRequest, BindingResult result,
			@AuthenticationPrincipal User user, Model model) {
		
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
				result.rejectValue("customImage", "errer.customImage", "Only extensions of png, jpg and webp are allowed.");
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
		if(wine != null && wine.getUserId().equals(user.getUserId())) {
			wineService.editWine(addWineRequest, user.getUserId());
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
	public String getMyWine(MyWineRequest myWineRequest, @AuthenticationPrincipal User user, Model model) {
		List<Wine> wineList = wineService.getMyWineList(myWineRequest, user.getUserId());
		wineService.setWineImage(wineList);
		
		String reqWineType = messageSource.getMessage("wine.type." + myWineRequest.getType().toLowerCase(), null, LocaleContextHolder.getLocale());
		
		model.addAttribute("reqWineType", reqWineType);
		model.addAttribute("myWineRequest", myWineRequest);
		model.addAttribute("wineList", wineList);
		model.addAttribute("pagination", myWineRequest.getPagination());
		model.addAttribute("exchangeRateUSD", wineService.getExchangeRateUSD());
		
		return "wine/my-wine";
	}
	@GetMapping("/wine/{wineId}")
	public String getWineDetail(@PathVariable(name = "wineId") Integer wineId
					, @AuthenticationPrincipal User user, Model model) {
		// 내 와인이 아닌것은 볼 수 없다
		if(!wineService.isMyWine(wineId, user.getUserId())) {
			String errorMessage = "You do not have access.";
			model.addAttribute("errorMessage", errorMessage);
			return "error";
		}
		
		WineDetailResponse response = wineService.getWineDetail(wineId, user.getUserId());
		wineService.setWineImage(response.getWine());
		
		model.addAttribute("response", response);
		model.addAttribute("exchangeRateUSD", wineService.getExchangeRateUSD());
		
		return "wine/wine-detail";
	}
	@GetMapping("/wine/load")
	public String getWineLogInWineDetail(WineDetailRequest wineDetailRequest, @AuthenticationPrincipal User user
								, Model model) {
		wineDetailRequest.setUserId(user.getUserId());
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
	@Transactional
	public String getEditWineLog(@PathVariable(name = "logId", required = true) Integer logId, 
							@AuthenticationPrincipal User user,
							@ModelAttribute EditWineLogRequest editWineLogRequest, BindingResult result,
							HttpServletRequest request, Model model) {
		WineLog wineLog = wineService.getWineLog(logId, user.getUserId());
		Wine wine = wineService.getWine(wineLog.getWineId());
		wineService.setWineImage(wine);
		boolean hasReview = wineService.getReviewByWineLog(logId, user.getUserId()) != null ? true : false;
		
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
											   .note(wineLog.getNote())
											   .build();
		
		
		List<String> buyPlaceList = wineService.getBuyPlace(user.getUserId());
		
		model.addAttribute(editWineLogRequest);
		model.addAttribute("placeList", buyPlaceList);
		model.addAttribute("hasReview", hasReview);
		
		return "/wine/edit-wine-log";
	}
	
	@PostMapping("/edit-wine-log")
	public String editWineLog(@ModelAttribute EditWineLogRequest editWineLogRequest, BindingResult result,
			@AuthenticationPrincipal User user, Model model) {
		
		if(result.hasErrors())
			return "/wine/edit-wine-log";
		
		wineService.editWineLog(editWineLogRequest, user.getUserId());	
		
		
		return "redirect:/wine/" + editWineLogRequest.getWineId();
	}
	
	@GetMapping("/edit-review/{reviewId}")
	public String getEditReview(@PathVariable(name = "reviewId", required = true) Integer reviewId, 
			@AuthenticationPrincipal User user,
			@ModelAttribute EditReviewRequest editReviewRequest, BindingResult result, Model model) {
		Review review = wineService.getReivew(reviewId, user.getUserId());
		
		model.addAttribute("review", review);
		
		return "/wine/edit-review";
	}
	
	@PostMapping("/edit-review")
	public String editReview(@ModelAttribute EditReviewRequest editReviewRequest, BindingResult result, 
			@AuthenticationPrincipal User user, Model model) {
		
		if(result.hasErrors())
			return "/wine/edit-wine-log";
		
		wineService.editReview(editReviewRequest, user.getUserId());
		
		return "redirect:/wine/" + editReviewRequest.getWineId();
	}
	
	@PostMapping("/delete-wineLog")
	public String deleteWineLog(EditWineLogRequest editWineLogRequest, @AuthenticationPrincipal User user) {
		
		wineService.deleteWineLog(editWineLogRequest, user.getUserId());	
		
		return "redirect:/wine/" + editWineLogRequest.getWineId();
	}
	
	@PostMapping("/delete-wine")
	public String deleteWine(AddWineRequest addWineRequest, @AuthenticationPrincipal User user) {
		
		wineService.deleteWine(addWineRequest, user.getUserId());	
		
		return "redirect:/my-wine";
	}
	
	@PostMapping("/delete-review")
	public String deleteReview(EditReviewRequest editReviewRequest, @AuthenticationPrincipal User user) {
		
		wineService.deleteReview(editReviewRequest, user.getUserId());	
		
		return "redirect:/wine/" + editReviewRequest.getWineId();
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
