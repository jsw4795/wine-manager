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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.winemanager.wine.domain.AddWineRequest;
import com.winemanager.wine.domain.Wine;
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
		 else // 원래 있던 와인 등록
			wineId = wineService.addBuyWineLog(addWineRequest, userId);
		
		
		return "redirect:/wine/" + wineId;
	}
	@GetMapping("/add-wine/my-wine-list")
	public String getMyWineList(Principal principal, Model model) {
		
		List<Wine> wineList = wineService.getWineListWithAllData(principal.getName());
		
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
	
	
	@GetMapping("/drink-wine")
	public String getDrinkWine(Principal principal, Model model) {
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		return "wine/drink-wine";
	}
	
	
	@GetMapping("/my-wine")
	public String redirectToMyWine(Principal principal, Model model) {
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		return "redirect:/my-wine/all?sort_by=reg_desc";
	}
	@GetMapping("/my-wine/{type}")
	public String getMyWine(@PathVariable(name = "type") String type, 
							@RequestParam(name = "sort_by", required = false, defaultValue = "reg_desc") String sortBy, 
							Principal principal,
							HttpServletRequest request,
							Model model) {
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("type", type);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("uri", request.getRequestURI());
		
		return "wine/my-wine";
	}
	@GetMapping("/wine/{wineId}")
	public String getWineDetail(@PathVariable(name = "wineId") String wineId, 
							Principal principal,
							HttpServletRequest request,
							Model model) {
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("wineId", wineId);
		model.addAttribute("uri", request.getRequestURI());
		
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
