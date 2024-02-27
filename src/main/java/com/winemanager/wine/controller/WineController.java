package com.winemanager.wine.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.winemanager.wine.service.WineService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class WineController {
	
	private final WineService wineService;
	
	@GetMapping("/")
	public String getIndex() {
		return "index";
	}
	
	@GetMapping("/about")
	public String getAbout() {
		return "about";
	}
		
	@GetMapping("/add-wine")
	public String getAddWine() {
		return "wine/add-wine";
	}
	
	@GetMapping("/my-wine")
	public String redirectToMyWine() {
		return "redirect:/my-wine/all";
	}
	
	@GetMapping("/my-wine/{type}")
	public String getMyWine(@PathVariable(name = "type") String type, 
							@RequestParam(name = "sort_by", required = false, defaultValue = "reg_desc") String sortBy, 
							Model model) {
		
		model.addAttribute("type", type);
		model.addAttribute("sortBy", sortBy);
		
		return "wine/my-wine";
	}
	
}
