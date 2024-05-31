package com.winemanager.user.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.winemanager.security.domain.SecurityUser;
import com.winemanager.user.domain.MainStats;
import com.winemanager.user.domain.SignUpRequest;
import com.winemanager.user.domain.Timeline;
import com.winemanager.user.domain.TimelineRequest;
import com.winemanager.user.domain.stats.WineByPlace;
import com.winemanager.user.domain.stats.WineByPrice;
import com.winemanager.user.domain.stats.WineByType;
import com.winemanager.user.domain.stats.SpendByTime;
import com.winemanager.user.domain.stats.StatsRequest;
import com.winemanager.user.domain.stats.StockByTime;
import com.winemanager.user.domain.stats.Top3Stats;
import com.winemanager.user.domain.stats.WineByCountry;
import com.winemanager.user.service.UserService;
import com.winemanager.wine.service.WineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {
	
	private final UserService userService;
	private final WineService wineService;
	
	@GetMapping("/login")
	public String getLogin(@ModelAttribute SecurityUser securityUser, Principal principal, Model model, @RequestParam(required = false) String error) {
		// 로그인 상태라면 메인화면으로
		if(principal != null) 
			return "redirect:/";
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("error", error);
		
		return "login/login";
	}
	
	@GetMapping("/sign-up")
	public String getSignUp(@ModelAttribute SignUpRequest signUpRequest, Principal principal, Model model) {
		// 로그인 상태라면 메인화면으로
		if(principal != null) 
			return "redirect:/";
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		
		return "login/sign-up";
	}
	
	// 회원가입 시 아이디 중복 체크
	@PostMapping("/sign-up/id-check")
	@ResponseBody
	public String checkIdValid(String userId) {
		if(!userId.matches("^[A-Za-z0-9]{6,15}$")) 
			return "pattern";
		return userService.checkIdExist(userId) ? "false" : "true";
	}
	// 회원가입 실행
	@PostMapping("/sign-up")
	public String signUp(@Valid @ModelAttribute SignUpRequest signUpRequest, BindingResult result, Principal principal, Model model) {
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		
		// 비밀번호, 비밀번호 확인 미 일치 시, 에러 추가하고 로그인 회원가입 페이지로
		if(!signUpRequest.getPassword().equals(signUpRequest.getPasswordCheck())) 
			result.rejectValue("passwordCheck", "errer.passwordCheck", "Not same with PW.");
		
		if(result.hasErrors()) {
			return "/login/sign-up";
		}

		userService.signUp(signUpRequest);
		
		return "redirect:/login";
	}
	
	@GetMapping("/my-page")
	public String myPage(Principal principal, Model model) {
		MainStats mainStats = userService.getMainStats(principal.getName());
		
		model.addAttribute("userId", principal != null ? principal.getName() : null);
		model.addAttribute("mainStats", mainStats);
		model.addAttribute("exchangeRate", wineService.getExchangeRate());
		
		return "/user/my-page";
	}
	
	@GetMapping("/my-page/timeline")
	@ResponseBody
	public List<Timeline> getTimeline(TimelineRequest timelineRequest, Principal principal) {
		timelineRequest.setUserId(principal.getName());
		List<Timeline> timeLineList = userService.getTimeline(timelineRequest);
		
		setWineImage(timeLineList);
		
		return timeLineList;
	}
	
	@GetMapping("/my-page/stats")
	public String getStats(Principal principal, Model model) {
		Map<String, List<Top3Stats>> top3Stats = userService.getTop3Stats(principal.getName());
		
		model.addAttribute("top3", top3Stats);
		
		return "/user/my-page-stats";
	}
	
	@GetMapping("/my-page/stats/spend-by-time")
	@ResponseBody
	public List<Map<String, Object>> getSpendByTimeData(StatsRequest statsRequest, Principal principal) {
		statsRequest.setUserId(principal.getName());
		List<SpendByTime> dataList = userService.getSpendByTime(statsRequest);
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		dataList.forEach((data) -> {
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("x", data.getMonth());
			dataMap.put("y", data.getMoney());
			
			result.add(dataMap);
		});
		
		return result;
	}
	
	@GetMapping("/my-page/stats/stock-by-time")
	@ResponseBody
	public List<Map<String, Object>> getStockByTimeData(StatsRequest statsRequest, Principal principal) {
		statsRequest.setUserId(principal.getName());
		
		List<StockByTime> dataList = userService.getStockByTime(statsRequest);
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		dataList.forEach((data) -> {
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("x", data.getDate());
			dataMap.put("y", data.getStock());
			
			result.add(dataMap);
		});
		
		return result;
	}

	@GetMapping("/my-page/stats/wine-by-place")
	@ResponseBody
	public List<WineByPlace> getWineByPlaceData(StatsRequest statsRequest, Principal principal) {
		statsRequest.setUserId(principal.getName());
		
		List<WineByPlace> result = userService.getWineByPlace(statsRequest);
		
		return result;
	}
	
	@GetMapping("/my-page/stats/wine-by-type")
	@ResponseBody
	public List<WineByType> getWineByTypeData(StatsRequest statsRequest, Principal principal) {
		statsRequest.setUserId(principal.getName());
		
		List<WineByType> result = userService.getWineByType(statsRequest);
		
		return result;
	}
	
	@GetMapping("/my-page/stats/wine-by-country")
	@ResponseBody
	public List<WineByCountry> getWineByCountryData(StatsRequest statsRequest, Principal principal) {
		statsRequest.setUserId(principal.getName());
		
		List<WineByCountry> result = userService.getWineByCountry(statsRequest);
		
		return result;
	}
	
	@GetMapping("/my-page/stats/wine-by-price")
	@ResponseBody
	public List<WineByPrice> getWineByPriceData(StatsRequest statsRequest, Principal principal) {
		statsRequest.setUserId(principal.getName());
		
		List<WineByPrice> result = userService.getWineByPrice(statsRequest);
		
		return result;
	}
	
	
	
	private void setWineImage(Timeline timeline) {
		if(timeline == null)
			return;
		
		if(!timeline.getThumb().startsWith("https://")) {
			timeline.setThumb("/images/wine/" + timeline.getThumb());
			timeline.setThumbBottom("/images/wine/" + timeline.getThumbBottom());
		}
	}
	private void setWineImage(List<Timeline> timelineList) {
		if(timelineList == null)
			return;
		
		for(Timeline timeline : timelineList) 
			this.setWineImage(timeline);
	}
	
}
