package com.winemanager.wine.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.winemanager.wine.service.WineService;

import jakarta.servlet.http.HttpServletRequest;
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
		
	@GetMapping("/add-wine")
	public String getAddWine(Principal principal, Model model) {
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		return "wine/add-wine";
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
	
}
