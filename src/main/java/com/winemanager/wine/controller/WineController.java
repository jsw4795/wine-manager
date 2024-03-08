package com.winemanager.wine.controller;

import java.security.Principal;
import java.sql.Date;
import java.text.SimpleDateFormat;
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

import com.winemanager.wine.domain.AddWineRequest;
import com.winemanager.wine.domain.MyWineRequest;
import com.winemanager.wine.domain.Wine;
import com.winemanager.wine.domain.WineDetailResponse;
import com.winemanager.wine.service.WineService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class WineController {
	
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
		
		if(addWineRequest.getWineId() != null) {
			Wine wine = wineService.getWine(addWineRequest.getWineId());
			if(wine.getUserId().equals(principal.getName())) {
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
		
		List<String> buyPlaceList = wineService.getBuyPlace(principal.getName());
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("buyPlaceList", buyPlaceList);
		
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
		
		List<Wine> wineList = wineService.getWineListByWineName(keyword, principal.getName());
		
		if(wineList == null || wineList.size() == 0)
			return "wine/emptyResultTemplate-add-wine";
		
		model.addAttribute("wineList", wineList);
		model.addAttribute("type", "old");
		
		return "wine/searchResultTemplate-add-wine";
	}
	@GetMapping("/add-wine/search-new")
	public String searchWineInVivino(String keyword, Model model) {
		if(keyword == null || keyword.trim().length() == 0)
			return "wine/emptyResultTemplate-add-wine";
		
		List<Wine> wineList = wineService.searchWineInVivino(keyword);
		
		if(wineList == null || wineList.size() == 0)
			return "wine/emptyResultTemplate-add-wine";
		
		model.addAttribute("wineList", wineList);
		model.addAttribute("type", "new");
		
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
	public String getDrinkWine(Principal principal, Model model) {
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		// 내 와인이 맞는지 확인 필
		return "wine/drink-wine";
	}
	
	
	@GetMapping("/my-wine")
	public String redirectToMyWine() {
		return "redirect:/my-wine/all?sortBy=reg_desc";
	}
	@GetMapping("/my-wine/{type}")
	public String getMyWine(MyWineRequest myWineRequest, Principal principal, HttpServletRequest request, Model model) {
		
		List<Wine> wineList = wineService.getMyWineList(myWineRequest, principal.getName());
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("myWineRequest", myWineRequest);
		model.addAttribute("wineList", wineList);
		model.addAttribute("pagination", myWineRequest.getPagination());
		
		return "wine/my-wine";
	}
	@GetMapping("/wine/{wineId}")
	public String getWineDetail(@PathVariable(name = "wineId") Integer wineId, 
							Principal principal,
							HttpServletRequest request,
							Model model) {
		// 내 와인이 아닌것은 볼 수 없다
		if(!wineService.isMyWine(wineId, principal.getName())) {
			String errorMessage = "You do not have access.";
			model.addAttribute("errorMessage", errorMessage);
			return "error";
		}
		
		WineDetailResponse response = wineService.getWineDetail(wineId, principal.getName());
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("response", response);
		model.addAttribute("exchangeRate", wineService.getExchangeRate());
		
		return "wine/wine-detail";
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
