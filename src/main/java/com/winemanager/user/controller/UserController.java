package com.winemanager.user.controller;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winemanager.user.domain.MainStats;
import com.winemanager.user.domain.SignUpRequest;
import com.winemanager.user.domain.Timeline;
import com.winemanager.user.domain.TimelineRequest;
import com.winemanager.user.domain.User;
import com.winemanager.user.domain.stats.SpendByTime;
import com.winemanager.user.domain.stats.StatsRequest;
import com.winemanager.user.domain.stats.StockByTime;
import com.winemanager.user.domain.stats.Top3Stats;
import com.winemanager.user.domain.stats.WineByCountry;
import com.winemanager.user.domain.stats.WineByPlace;
import com.winemanager.user.domain.stats.WineByPrice;
import com.winemanager.user.domain.stats.WineByType;
import com.winemanager.user.service.UserService;
import com.winemanager.wine.service.WineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {
	
	private final ObjectMapper objectMapper;
	private final DateFormat englishDateFormat;
	private final DateFormat koreanDateFormat;
	
	private final UserService userService;
	private final WineService wineService;
	
	@ModelAttribute
	public void load(Model model, @AuthenticationPrincipal User user){
		if(user == null)
			user = new User();
		model.addAttribute("user", user);

	}
	
	@GetMapping("/login")
	public String getLogin(@AuthenticationPrincipal User user, Model model, 
					@RequestParam(required = false) String error) {
		// 로그인 상태라면 메인화면으로
		if(user != null) 
			return "redirect:/";
		
		model.addAttribute("error", error);
		
		return "login/login";
	}
	
	@GetMapping("/sign-up")
	public String getSignUp(@ModelAttribute SignUpRequest signUpRequest, 
			@AuthenticationPrincipal User user, Model model) {
		// 로그인 상태라면 메인화면으로
		if(user != null) 
			return "redirect:/";
				
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
	public String signUp(@Valid @ModelAttribute SignUpRequest signUpRequest, BindingResult result, 
			 Model model) {
		
		
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
	public String myPage(@AuthenticationPrincipal User user, Model model) {
		MainStats mainStats = userService.getMainStats(user.getUserId());
		
		model.addAttribute("mainStats", mainStats);
		model.addAttribute("exchangeRate", wineService.getExchangeRate());
		
		return "/user/my-page";
	}
	
	@GetMapping("/my-page/timeline")
	@ResponseBody
	public String getTimeline(TimelineRequest timelineRequest, @AuthenticationPrincipal User user) throws JsonProcessingException {
		timelineRequest.setUserId(user.getUserId());
		List<Timeline> timeLineList = userService.getTimeline(timelineRequest);
		
		setWineImage(timeLineList);
		
		String jsonResult = null;
		switch (LocaleContextHolder.getLocale().getLanguage()) {
		case "en":
			jsonResult = objectMapper.writer(englishDateFormat).writeValueAsString(timeLineList);
			break;
			
		case "ko":
			jsonResult = objectMapper.writer(koreanDateFormat).writeValueAsString(timeLineList);			
			break;
			
		default:
			jsonResult = objectMapper.writer(englishDateFormat).writeValueAsString(timeLineList);
			break;
		}
		
		return jsonResult;
	}
	
	@GetMapping("/my-page/timeline/template")
	public String getTimelineTemplate(@AuthenticationPrincipal User user) {
		
		return "/user/my-page-timeline-template";
	}
	
	@GetMapping("/my-page/stats")
	public String getStats(@AuthenticationPrincipal User user, Model model) {
		Map<String, List<Top3Stats>> top3Stats = userService.getTop3Stats(user.getUserId());
		
		model.addAttribute("top3", top3Stats);
		
		return "/user/my-page-stats";
	}
	
	@GetMapping("/my-page/stats/spend-by-time")
	@ResponseBody
	public List<Map<String, Object>> getSpendByTimeData(StatsRequest statsRequest, @AuthenticationPrincipal User user) {
		statsRequest.setUserId(user.getUserId());
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
	public List<Map<String, Object>> getStockByTimeData(StatsRequest statsRequest, @AuthenticationPrincipal User user) {
		statsRequest.setUserId(user.getUserId());
		
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
	public List<WineByPlace> getWineByPlaceData(StatsRequest statsRequest, @AuthenticationPrincipal User user) {
		statsRequest.setUserId(user.getUserId());
		
		List<WineByPlace> result = userService.getWineByPlace(statsRequest);
		
		return result;
	}
	
	@GetMapping("/my-page/stats/wine-by-type")
	@ResponseBody
	public List<WineByType> getWineByTypeData(StatsRequest statsRequest, @AuthenticationPrincipal User user) {
		statsRequest.setUserId(user.getUserId());
		
		List<WineByType> result = userService.getWineByType(statsRequest);
		
		return result;
	}
	
	@GetMapping("/my-page/stats/wine-by-country")
	@ResponseBody
	public List<WineByCountry> getWineByCountryData(StatsRequest statsRequest, @AuthenticationPrincipal User user) {
		statsRequest.setUserId(user.getUserId());
		
		List<WineByCountry> result = userService.getWineByCountry(statsRequest);
		
		return result;
	}
	
	@GetMapping("/my-page/stats/wine-by-price")
	@ResponseBody
	public List<WineByPrice> getWineByPriceData(StatsRequest statsRequest, @AuthenticationPrincipal User user) {
		statsRequest.setUserId(user.getUserId());
		
		List<WineByPrice> result = userService.getWineByPrice(statsRequest);
		
		return result;
	}
	
	
	
	private void setWineImage(Timeline timeline) {
		if(timeline == null)
			return;
		
		if(timeline.getThumb() == null || timeline.getThumb().length() < 1) {
			timeline.setThumb("wine-default.png");
			timeline.setThumbBottom("wine-default.png");
		}
		
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
