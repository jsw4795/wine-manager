var globalPageSize = 10;

var myPageData = {
	type: "timeline",
	page: 1,
	scroll: 0,
}

// 뒤로가기를 통해 페이지에 들어온 경우, 페이지 뿌려주기
window.onpageshow = function(event) {
	// 뒤로가기로 온 경우
	if (window.performance && window.performance.navigation.type == 2) {
		
		if(!sessionStorage.getItem("myPageData"))
			return;
			
		myPageData = JSON.parse(sessionStorage.getItem("myPageData"));
		
		// 사파리는 새로 안불러와도 페이지가 유지됨
		if(isSafari){
			myPageData.page++;		
			return;
		}
		
		switch(myPageData.type){
			case "timeline" : 
				navBtnOn("timeline");
				let scroll = myPageData.scroll; // 미리 저장을 안해놓으면 중간에 값이 바뀌기도함
				requestTimeline(1, myPageData.page * globalPageSize, function() {
					$(document).scrollTop(scroll);
				});
				break;
			case "stats" : 
				navBtnOn("stats");
				break;
			case "settings" : 
				navBtnOn("settings");
				break;
			default:
				navBtnOn("timeline");
				requestTimeline(myPageData.page, globalPageSize, function() {
					$(document).scrollTop(0);
				});
		}
	} else if(window.performance.navigation.type == 1) { // 새로고침
	
		if(!sessionStorage.getItem("myPageData"))
			return;
			
		myPageData = JSON.parse(sessionStorage.getItem("myPageData"));
		myPageData.page = 1;
		switch(myPageData.type){
			case "timeline" : 
				navBtnOn("timeline");
				requestTimeline(myPageData.page, globalPageSize, function() {
					$(document).scrollTop(0);
				});
				break;
			case "stats" : 
				navBtnOn("stats");
				break;
			case "settings" : 
				navBtnOn("settings");
				break;
			default:
				navBtnOn("timeline")
				requestTimeline(myPageData.page, globalPageSize, function() {
					$(document).scrollTop(0);
				});
		}
	} else { // 그 외 (my page 클릭해서 들어온 경우)
		navBtnOn("timeline")
		requestTimeline(myPageData.page, globalPageSize);
	}
}

$(()=> {
	let $statsCard = $(".stats-card");
	
	for($card of $statsCard){
		$statsCard.removeClass("mb-10");
		$statsCard.addClass("mb-0");
		$statsCard.removeClass("opacity-0");
		$statsCard.addClass("opacity-100");
	}
	$('.count').each(function() {
		$(this).prop('Counter', 0).animate({
			Counter: $(this).text()
		}, {
			duration: 3000,
			easing: 'easeOutExpo',
			step: function(now) {
				$(this).text(Math.ceil(now).toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,"));
				
				// 숫자 잘리지 않게 사이즈 조절
				resize_to_fit($(this));
			}
		});
	});
	
	// 버벅임이 심해서 트렌지션 끝날때 실행되도록 변경
	/*$(window).on("resize", function() {
		if($(this).width() < 767) { // 작아질때
			$(".count").each(function(idx, obj) {
				resize_to_fit_to_small($(obj))
			})
		} else {
			$(".count").each(function(idx, obj) {
				resize_to_fit_to_large($(obj))
			})
		}
	})*/
	
	$(".stats-card").on("transitionend webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd", function() {
		if($(window).width() < 767) {
			resize_to_fit_to_small($(this).find(".count").eq(0))
			resize_to_fit_to_small($(this).find(".count").eq(1))
		} else {
			resize_to_fit_to_large($(this).find(".count").eq(0))
			resize_to_fit_to_large($(this).find(".count").eq(1))
		}
	})
	
	$("main").on("click", "#timeline-load-btn", function() {
		requestTimeline(myPageData.page, globalPageSize);
	})
	
	
	// my-page 네비게이션 클릭 시 초기화
	$(".my-page-nav").on("click", function() {
		$(".my-page-nav").removeClass("border-2");
		$(".my-page-nav").removeClass("pointer-events-none");
		$(this).addClass("border-2");
		$(this).addClass("pointer-events-none");
		$("main").html('');
		myPageData.page = 1;
	})
	$("#timeline-btn").on("click", function() {
		myPageData.type = "timeline";
		
		requestTimeline(myPageData.page, globalPageSize);
	})
	$("#stats-btn").on("click", function() {
		myPageData.type = "stats";
		
		// stats 버튼 클릭 시 로직
	})
	$("#settings-btn").on("click", function() {
		myPageData.type = "settings";
		
		// settings 버튼 클릭 시 로직
	})
	
	// 페이지를 벗어날 때, 세션스토리지에 데이터 저장
	$(window).on("pagehide", function() {
		// 여기서 페이지는 다음에 불러올 페이지를 저장하고 있기때문에 -1
		myPageData.page = Math.max(1, myPageData.page - 1); 
		myPageData.scroll = $(document).scrollTop();
		sessionStorage.setItem("myPageData", JSON.stringify(myPageData));
	})
})

// 글자 사이즈 컨테이너에 맞게 조절
function resize_to_fit($target){
    let fontsize = $target.css('font-size');
    
    let textWidth = $target.width();
    $target.siblings().each(function() {
		textWidth += $(this).width();
	})

    if(textWidth > $target.parent().width() && $target.height() <= 70){
	    $target.parent().css('fontSize', parseFloat(fontsize) - 1);
    }
}
function resize_to_fit_to_small($target){
	let textWidth = $target.width();
    $target.siblings().each(function() {
		textWidth += $(this).width();
	})
    while(textWidth > $target.parent().width() && $target.height() <= 70){
	    $target.parent().css('fontSize', parseFloat($target.css('font-size')) - 1);
	    
	    textWidth = $target.width();
	    $target.siblings().each(function() {
			textWidth += $(this).width();
		})
    }
}
function resize_to_fit_to_large($target){
    let textWidth = $target.width();
    $target.siblings().each(function() {
		textWidth += $(this).width();
	})
    while(textWidth < $target.parent().width() && $target.height() < 70){
	    $target.parent().css('fontSize', parseFloat($target.css('font-size')) + 1);
	    
	    textWidth = $target.width();
	    $target.siblings().each(function() {
			textWidth += $(this).width();
		})
    }
}

// 타임라인 데이터 요청
function requestTimeline(page, pageSize, callback) {
	$.ajax({
		url: "/my-page/timeline",
		type: "GET",
		data: {page: page, pageSize: pageSize},
		dataType: "JSON",
		success: function(result) {
			myPageData.page++;
			
			renderTimeline(result, pageSize);
			
			$("#timeline-load-btn").remove();
			
			if(result.length == pageSize + 1){
				$("main").append(loadTimelineBtn);
			}
			
			if(callback)
				callback();
		}
	});
}

function renderTimeline(jsonList, pageSize) {
	let index = 0;
	for(data of jsonList) {
		if(index >= pageSize)
			return;
		
		let html = makeTimelineHTML(data);
		
		if($("time[data-date='"+data.date+"']").length < 1) {
			$("main").append(makeTimelineBlock(data));
		}
		
		$("time[data-date='"+data.date+"']").parent().parent().find("ol").append(html);
		index++;
	}
}

function makeTimelineHTML(data) {
	let html = 
				'<li>'
					+'<a href="/wine/'+data.wineId+'">'
		            +'<div data-wineId="'+data.wineId+'" data-logId="'+data.logId+'" class="timeline items-center block p-3 flex hover:bg-gray-100 cursor-pointer">'
		                +'<img class="shrink-0 sm:w-16 h-16 me-5 sm:me-3 rounded" src="'+data.thumb+'" style="object-fit: contain;"/>'
		                +'<div class="text-gray-600 ">'
		                    +'<div class="text-base font-normal">';
		                    
	if(data.type == "IN"){
		html += '<span class="font-medium text-gray-900">Bought</span> ';
	} else if(data.type == "OUT") {
		html += '<span class="font-medium text-gray-900">Drank</span> ';
	}
	html += 
		                    	'<span class="font-medium text-gray-900 ">'+data.count+'</span> bottle of ' 
		                    	+'<span class="font-medium text-gray-900 ">'+data.wineName + (data.vintage != null ? ' ' + data.vintage : '') + (data.wineSize != 'Standard' ? ' ['+data.wineSize+']'  : '') + '</span>'
		                    +'</div>'
		                    +'<div class="text-sm font-normal">at <span class="font-medium text-gray-900 ">'+data.place+'</span> '
		                    ;
		                    
	if(data.rate != null && data.type == "OUT"){
		let num = (Number)(data.rate);
		html += ' and you rated it at <span class="font-medium text-gray-900 ">'+num.toFixed(Math.max(1, (num.toString().split('.')[1] || []).length))+'</span></div>';
	} else if(data.rate == null && data.type == "OUT") {
		html += " and haven't rated it yet.</div> ";
	} else if(data.type == "IN") {
		html += ' for <span class="font-medium text-gray-900 ">₩'+data.price.toLocaleString()+'</span> each.</div>';
	}
	
	if(data.type == "IN"){
		html += 			'<span class="inline-flex items-center text-xs font-normal text-gray-500 ">'
		                        +'<svg class="w-4 h-4 me-1" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">'
									+'<g xmlns="http://www.w3.org/2000/svg" id="SVGRepo_bgCarrier" stroke-width="0"/>'
									+'<g xmlns="http://www.w3.org/2000/svg" id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"/>'
									+'<g xmlns="http://www.w3.org/2000/svg" id="SVGRepo_iconCarrier"> <path d="M21 5L19 12H7.37671M20 16H8L6 3H3M16 5.5H13.5M13.5 5.5H11M13.5 5.5V8M13.5 5.5V3M9 20C9 20.5523 8.55228 21 8 21C7.44772 21 7 20.5523 7 20C7 19.4477 7.44772 19 8 19C8.55228 19 9 19.4477 9 20ZM20 20C20 20.5523 19.5523 21 19 21C18.4477 21 18 20.5523 18 20C18 19.4477 18.4477 19 19 19C19.5523 19 20 19.4477 20 20Z" stroke="#6B7280" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/> </g>'
		                        +'</svg>'
		                        +'Buy'
		                    +'</span>' 
	} else if(data.type == "OUT") {
		html += 			'<span class="inline-flex items-center text-xs font-normal text-gray-500 ">'
								+'<svg class="w-4 me-1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#6B7280" version="1.1" id="Capa_1" viewBox="0 0 329.257 329.257" xml:space="preserve" stroke="#6B7280" stroke-width="3.6218270000000006">'
									+'<g id="SVGRepo_bgCarrier" stroke-width="0"/>'
									+'<g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"/>'
									+'<g id="SVGRepo_iconCarrier"> <g> <g id="Artwork_30_"> <g id="Layer_5_30_"> <g> <path d="M178.184,47.201c-7.207,0-11.781,8.036-12.862,8.036c-0.947,0-5.334-8.036-12.865-8.036 c-7.325,0-13.307,6.042-13.698,13.357c-0.221,4.132,1.111,7.277,2.989,10.126c3.75,5.691,20.148,19.392,23.598,19.392 c3.522,0,19.773-13.651,23.549-19.392c1.881-2.861,3.211-5.994,2.988-10.126C191.49,53.243,185.509,47.201,178.184,47.201"/> <path d="M136.198,7.898c-5.195,0-8.49,5.791-9.27,5.791c-0.683,0-3.844-5.791-9.271-5.791c-5.279,0-9.59,4.353-9.872,9.626 c-0.159,2.978,0.801,5.245,2.153,7.297c2.703,4.102,14.521,13.976,17.007,13.976c2.539,0,14.251-9.838,16.972-13.976 c1.355-2.063,2.313-4.319,2.153-7.297C145.788,12.251,141.478,7.898,136.198,7.898"/> <path d="M210.531,7.898c-5.194,0-8.491,5.791-9.271,5.791c-0.682,0-3.842-5.791-9.27-5.791c-5.28,0-9.591,4.353-9.873,9.626 c-0.159,2.978,0.801,5.245,2.153,7.297c2.703,4.102,14.521,13.976,17.006,13.976c2.539,0,14.25-9.838,16.973-13.976 c1.356-2.063,2.313-4.319,2.153-7.297C220.122,12.251,215.811,7.898,210.531,7.898"/> <path d="M153.968,114.037c-1.18-7.706-2.733-12.419-7.184-13.708L69.035,77.811c-0.642-0.186-1.295-0.28-1.941-0.28 c-3.727,0-7.147,3.071-11.457,8.027c-3.354,3.86-7.536,9.308-12.093,15.754c-9.083,12.853-17.312,26.286-20.012,32.667 c-6.069,14.335-4.653,31.263,3.986,47.665c6.396,12.146,19.343,24.885,33.303,32.867c-1.354,6.283-4.832,19.74-9.328,35.072 c-7.825,26.69-11.874,35.842-12.879,37.318c-0.506,0.288-2.418,0.885-6.569,0.885c-3.213,0-6.981-0.371-10.898-1.07 l-0.295-0.053c-0.816-0.146-1.771-0.367-2.78-0.602c-2.543-0.589-5.425-1.256-8.199-1.256c-6.405,0-8.825,3.715-9.729,6.83 c-0.245,0.848-0.298,2.178,0.848,3.701c6.518,8.674,64.362,26.016,82,26.016c1.843,0,6.738,0,7.746-3.479 c2.64-9.115-6.633-12.355-12.173-14.293c-0.979-0.342-1.903-0.664-2.675-0.979l-0.272-0.11 c-10.058-4.099-14.341-8.102-14.866-9.178c-0.066-1.801,1.356-11.706,8.805-38.566c4.143-14.941,8.232-28.177,10.402-34.434 c31.063,1.873,70.107-16.889,75.269-48.193c1.127-6.834,1.35-22.587,0.544-38.309 C155.362,125.917,154.739,119.077,153.968,114.037z M144.353,144.211c-0.225,2.083-1.314,2.214-1.963,1.98 c-5.762-2.082-11.756-3.098-18.216-3.098c-9.543,0-18.563,2.201-27.287,4.329c-7.902,1.928-15.367,3.749-22.509,3.75 c-9.005,0-20.859-2.514-32.697-22.124l-0.406-0.669c-0.322-0.613,0.05-2.018,0.831-3.297 c8.42-13.847,19.815-29.134,25.125-34.519c0.523-0.53,1.269-0.439,1.678-0.319c17.016,4.968,64.591,18.771,71.784,20.849 c0.602,0.174,0.845,0.864,0.845,0.864C143.265,117.317,144.595,128.053,144.353,144.211z"/> <path d="M329.113,291.639c-0.903-3.116-3.323-6.831-9.729-6.831c-2.773,0-5.656,0.667-8.198,1.256 c-1.011,0.232-1.965,0.455-2.78,0.602l-0.295,0.053c-3.917,0.699-7.686,1.07-10.898,1.07c-4.199,0-6.107-0.61-6.55-0.863 c-1.027-1.504-5.076-10.657-12.898-37.34c-4.496-15.332-7.974-28.789-9.328-35.07c13.96-7.982,26.906-20.723,33.304-32.868 c8.64-16.403,10.056-33.331,3.986-47.665c-2.699-6.382-10.93-19.815-20.013-32.668c-4.557-6.447-8.738-11.895-12.093-15.755 c-4.311-4.956-7.729-8.027-11.457-8.027c-0.646,0-1.3,0.094-1.941,0.28l-77.749,22.518c-4.451,1.289-6.005,6.002-7.185,13.708 c-0.771,5.04-1.396,11.88-1.802,19.779c-0.808,15.721-0.584,31.474,0.543,38.308c5.162,31.306,44.213,50.076,75.27,48.193 c2.17,6.256,6.259,19.493,10.402,34.434c7.46,26.901,8.875,36.795,8.81,38.539c-0.567,1.141-4.856,5.125-14.868,9.205 l-0.277,0.112c-0.769,0.313-1.693,0.638-2.673,0.979c-5.539,1.938-14.812,5.178-12.173,14.293 c1.009,3.479,5.903,3.479,7.746,3.479c17.639,0,75.482-17.343,82-26.016C329.411,293.817,329.358,292.487,329.113,291.639z M288.113,126.636c0.326,0.585,0.238,1.13,0.113,1.338c-0.081,0.132-0.651,1.074-0.651,1.074 c-11.837,19.607-23.692,22.122-32.698,22.124c-7.142,0-14.607-1.821-22.512-3.75c-8.724-2.128-17.742-4.329-27.283-4.329 c-6.875,0-13.221,1.152-19.325,3.512c-0.276,0.107-0.841,0.053-0.841-0.76c-0.23-15.553,1.186-28.2,2.83-33.989 c0.064-0.227,0.049-0.521,0.798-0.816l71.891-20.822c1.069-0.341,1.297,0.054,1.488,0.243 C267.21,95.68,279.638,112.699,288.113,126.636z"/> </g> </g> </g> </g> </g>'
								+'</svg>'
		                        +'Drink'
		                    +'</span>' 
	}
	
	html += 			 '</div>'
		            +'</div>'
		            +'</a>'
		        +'</li>'
		        ;
	
	return html;
	
}

function makeTimelineBlock(data) {
	let html = 
				'<div class="p-5 mb-4 border border-gray-100 rounded-lg bg-gray-50 ">'
					+'<div class="flex justify-between">'
				    	+'<time class="text-lg font-semibold text-gray-900 " data-date="'+data.date+'">'+data.date+'</time>'
				    	+'<div class="">'
				    	
		    	if(data.spendToday != null)
	    			html += '<div class="inline-block">'
		    					//+'<svg class="w-7 h-7 inline-block" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000" height="800px" width="800px" version="1.1" id="Layer_1" viewBox="0 0 512 512" xml:space="preserve">'
									//+'<g>'
										//+'<g>'
											//+'<g>'
												//+'<path d="M273.652,269.14v-8.735c0-4.875-3.952-8.828-8.828-8.828c-4.875,0-8.828,3.952-8.828,8.828v8.752     c-9.699,1.973-18.449,7.146-24.878,14.74c-11.436,13.524-8.844,34.061,5.206,44.975l19.672,15.305v48.12     c-10.284-3.637-17.655-13.441-17.655-24.97c0-4.875-3.952-8.828-8.828-8.828c-4.875,0-8.828,3.952-8.828,8.828     c0,21.352,15.165,39.163,35.31,43.253v7.75c0,4.875,3.952,8.828,8.828,8.828c4.875,0,8.828-3.952,8.828-8.828v-7.767     c9.699-1.973,18.449-7.146,24.878-14.74c11.435-13.523,8.844-34.049-5.202-44.972l-19.676-15.308v-48.12     c10.284,3.637,17.655,13.441,17.655,24.97c0,4.875,3.952,8.828,8.828,8.828c4.875,0,8.828-3.952,8.828-8.828     C308.962,291.041,293.797,273.23,273.652,269.14z M282.488,364.787c6.26,4.869,7.402,13.912,2.564,19.633     c-3.067,3.623-7,6.323-11.4,7.877v-34.384L282.488,364.787z M247.161,314.933c-6.26-4.863-7.402-13.912-2.564-19.633     c3.067-3.623,7-6.323,11.4-7.877v34.384L247.161,314.933z"/>'
												//+'<path d="M503.172,180.966h-64.067c-11.169-16.002-29.705-26.483-50.695-26.483c-20.99,0-39.526,10.48-50.695,26.483h-28.753     v-70.621h35.31c7.643,0,11.674-9.052,6.561-14.733L271.386,7.336c-3.507-3.896-9.616-3.896-13.123,0l-79.448,88.276     c-5.113,5.681-1.081,14.733,6.562,14.733h35.31v70.621H8.828c-4.875,0-8.828,3.952-8.828,8.828v308.966     c0,4.875,3.952,8.828,8.828,8.828h494.345c4.875,0,8.828-3.952,8.828-8.828V189.793C512,184.918,508.048,180.966,503.172,180.966     z M140.532,418.308c-0.038,0.213-0.073,0.427-0.114,0.639c-0.328,1.685-0.75,3.342-1.267,4.962     c-0.041,0.129-0.087,0.255-0.13,0.383c-0.221,0.671-0.457,1.336-0.71,1.994c-0.075,0.196-0.153,0.39-0.23,0.584     c-0.245,0.613-0.505,1.22-0.777,1.82c-0.078,0.172-0.154,0.347-0.234,0.518c-0.715,1.524-1.515,3.004-2.398,4.434     c-0.088,0.142-0.181,0.28-0.27,0.421c-0.359,0.567-0.73,1.126-1.115,1.676c-0.122,0.175-0.246,0.349-0.371,0.522     c-0.401,0.556-0.814,1.102-1.241,1.639c-0.091,0.114-0.178,0.231-0.27,0.344c-1.091,1.344-2.261,2.628-3.509,3.842     c-0.044,0.043-0.09,0.085-0.135,0.129c-0.607,0.586-1.231,1.157-1.874,1.709c-0.026,0.023-0.052,0.046-0.079,0.068     c-1.368,1.171-2.814,2.266-4.334,3.275c-0.038,0.025-0.071,0.053-0.109,0.079c-6.964,4.594-15.3,7.276-24.264,7.276     c-24.37,0-44.138-19.767-44.138-44.138c0-8.964,2.682-17.299,7.276-24.263c0.026-0.037,0.053-0.071,0.078-0.108     c1.011-1.523,2.108-2.972,3.282-4.342c0.018-0.021,0.037-0.042,0.055-0.064c0.557-0.648,1.132-1.277,1.722-1.888     c0.041-0.042,0.081-0.085,0.121-0.127c1.212-1.246,2.494-2.415,3.836-3.504c0.125-0.101,0.253-0.198,0.378-0.297     c0.522-0.414,1.053-0.816,1.592-1.206c0.188-0.136,0.377-0.271,0.567-0.403c0.525-0.367,1.059-0.721,1.599-1.064     c0.166-0.105,0.328-0.214,0.495-0.318c0.712-0.439,1.434-0.862,2.17-1.26c0.03-0.016,0.06-0.03,0.09-0.046     c0.696-0.375,1.405-0.727,2.121-1.064c0.201-0.094,0.405-0.183,0.607-0.275c0.567-0.256,1.138-0.501,1.716-0.733     c0.221-0.089,0.442-0.177,0.665-0.262c0.623-0.238,1.252-0.461,1.886-0.671c0.16-0.053,0.317-0.11,0.477-0.162     c0.818-0.26,1.644-0.501,2.479-0.714c0.011-0.003,0.023-0.005,0.034-0.008c0.795-0.202,1.599-0.377,2.409-0.535     c0.226-0.044,0.455-0.081,0.682-0.122c0.606-0.108,1.216-0.205,1.83-0.288c0.27-0.037,0.541-0.072,0.812-0.103     c0.605-0.071,1.214-0.126,1.825-0.172c0.252-0.019,0.503-0.043,0.756-0.058c0.854-0.049,1.711-0.081,2.574-0.081     c24.37,0,44.138,19.767,44.138,44.138c0,0.863-0.031,1.721-0.081,2.575c-0.014,0.25-0.038,0.498-0.057,0.747     c-0.046,0.616-0.102,1.231-0.173,1.841c-0.031,0.265-0.065,0.529-0.101,0.792C140.741,417.065,140.643,417.688,140.532,418.308z      M148.028,445.465c0.24-0.349,0.475-0.703,0.708-1.057c0.259-0.393,0.513-0.79,0.763-1.19c0.229-0.365,0.458-0.73,0.679-1.101     c0.254-0.426,0.498-0.858,0.741-1.289c0.345-0.61,0.678-1.225,1.003-1.848c0.228-0.438,0.457-0.874,0.675-1.317     c0.21-0.427,0.408-0.86,0.608-1.293c0.171-0.371,0.341-0.742,0.505-1.116c0.197-0.45,0.39-0.901,0.577-1.356     c0.151-0.369,0.295-0.741,0.439-1.113c0.179-0.462,0.361-0.921,0.529-1.388c0.265-0.737,0.517-1.48,0.754-2.229     c0.144-0.455,0.274-0.917,0.408-1.377c0.12-0.411,0.24-0.822,0.351-1.237c0.122-0.455,0.238-0.911,0.35-1.37     c0.11-0.451,0.213-0.904,0.314-1.358c0.094-0.426,0.19-0.851,0.275-1.281c0.153-0.772,0.292-1.548,0.416-2.329     c0.054-0.341,0.099-0.684,0.147-1.027c0.093-0.654,0.178-1.31,0.25-1.969c0.037-0.345,0.069-0.691,0.101-1.037     c0.059-0.64,0.107-1.283,0.147-1.928c0.019-0.307,0.04-0.614,0.054-0.923c0.043-0.947,0.072-1.897,0.072-2.852     c0-34.121-27.672-61.793-61.793-61.793c-0.956,0-1.907,0.029-2.855,0.072c-0.305,0.014-0.608,0.035-0.912,0.053     c-0.651,0.039-1.3,0.088-1.946,0.148c-0.34,0.031-0.68,0.062-1.018,0.099c-0.678,0.074-1.352,0.162-2.024,0.257     c-0.276,0.039-0.553,0.071-0.827,0.114c-0.839,0.131-1.672,0.282-2.5,0.447c-0.407,0.081-0.809,0.172-1.213,0.261     c-0.483,0.106-0.964,0.216-1.443,0.333c-0.435,0.107-0.868,0.216-1.3,0.332c-0.455,0.122-0.906,0.253-1.357,0.386     c-0.421,0.124-0.845,0.242-1.262,0.374c-0.769,0.243-1.532,0.502-2.289,0.775c-0.421,0.152-0.835,0.316-1.252,0.477     c-0.423,0.163-0.845,0.326-1.263,0.498c-0.421,0.173-0.84,0.352-1.256,0.534c-0.42,0.183-0.836,0.373-1.252,0.566     c-0.393,0.182-0.787,0.363-1.175,0.553c-0.555,0.272-1.103,0.556-1.649,0.845c-0.465,0.246-0.926,0.497-1.384,0.754     c-0.5,0.281-0.999,0.563-1.491,0.858c-0.34,0.203-0.675,0.414-1.011,0.624c-0.433,0.27-0.863,0.545-1.289,0.826     c-0.331,0.218-0.66,0.437-0.986,0.661c-0.118,0.081-0.238,0.157-0.355,0.238v-73.044c28.171-2.143,50.655-24.629,52.798-52.798     h214.604c3.257,10.934,9.446,20.606,17.673,28.084c4.753,4.32,10.19,7.893,16.118,10.575c0.976,0.442,1.965,0.858,2.967,1.25     c0.137,0.054,0.274,0.109,0.411,0.162c0.616,0.235,1.236,0.459,1.861,0.675c0.239,0.083,0.479,0.163,0.719,0.243     c0.588,0.195,1.178,0.385,1.773,0.563c0.284,0.085,0.57,0.162,0.855,0.243c0.579,0.164,1.156,0.329,1.742,0.477     c0.187,0.047,0.377,0.085,0.564,0.13c1.044,0.252,2.099,0.476,3.164,0.674c0.503,0.094,1.006,0.188,1.513,0.27     c0.417,0.067,0.837,0.124,1.257,0.183c0.547,0.077,1.095,0.147,1.646,0.209c0.392,0.044,0.784,0.086,1.177,0.122     c0.625,0.058,1.254,0.104,1.884,0.143c0.328,0.02,0.654,0.046,0.983,0.061c0.965,0.045,1.934,0.074,2.909,0.074     c0.001,0,0.001,0,0.002,0s0.001,0,0.002,0c1.148,0,2.287-0.037,3.419-0.099c0.298-0.016,0.594-0.047,0.891-0.068     c0.85-0.059,1.697-0.127,2.537-0.22c0.31-0.034,0.617-0.079,0.926-0.118c0.847-0.106,1.689-0.226,2.525-0.367     c0.263-0.044,0.525-0.093,0.788-0.141c0.915-0.166,1.823-0.352,2.723-0.557c0.172-0.039,0.344-0.078,0.515-0.119     c4.547-1.08,8.9-2.656,12.996-4.68c9.399,8.673,21.475,14.033,34.469,15.027v106.267c-28.166,2.144-50.655,24.632-52.798,52.798     H147.808C147.884,445.685,147.953,445.574,148.028,445.465z M388.41,172.138c16.127,0,30.232,8.66,37.933,21.578     c0.084,0.168,0.153,0.339,0.249,0.505c2.867,4.945,4.74,10.344,5.529,15.977c0.031,0.225,0.059,0.452,0.086,0.678     c0.079,0.634,0.14,1.271,0.191,1.91c0.023,0.295,0.05,0.59,0.067,0.887c0.042,0.714,0.064,1.432,0.071,2.152     c0.002,0.151,0.011,0.3,0.011,0.451c0,15.271-7.763,28.732-19.555,36.658c-0.165,0.094-0.331,0.185-0.491,0.29     c-3.117,2.041-6.455,3.672-9.951,4.861c-0.005,0.002-0.01,0.003-0.015,0.005c-1.322,0.449-2.669,0.829-4.033,1.15     c-0.247,0.058-0.492,0.117-0.741,0.171c-0.326,0.071-0.653,0.136-0.981,0.199c-0.355,0.068-0.712,0.132-1.07,0.191     c-0.261,0.044-0.52,0.09-0.782,0.129c-0.608,0.09-1.219,0.168-1.835,0.233c-0.193,0.021-0.388,0.035-0.582,0.053     c-0.492,0.045-0.986,0.084-1.482,0.113c-0.202,0.012-0.403,0.024-0.605,0.033c-0.668,0.03-1.338,0.051-2.013,0.051     c-0.72,0-1.435-0.02-2.146-0.054c-0.259-0.013-0.516-0.036-0.774-0.053c-0.444-0.029-0.888-0.059-1.329-0.101     c-0.328-0.032-0.653-0.072-0.979-0.111c-0.359-0.042-0.719-0.086-1.076-0.137c-0.37-0.053-0.737-0.113-1.104-0.176     c-0.299-0.051-0.597-0.103-0.894-0.16c-0.408-0.078-0.814-0.161-1.219-0.25c-0.236-0.052-0.471-0.107-0.705-0.163     c-0.452-0.108-0.903-0.219-1.35-0.341c-0.155-0.042-0.309-0.088-0.463-0.132c-0.517-0.147-1.032-0.297-1.541-0.463     c-0.012-0.004-0.024-0.008-0.036-0.012c-15.311-4.979-27.079-18.142-29.85-34.622c-0.048-0.284-0.111-0.563-0.185-0.836     c-0.316-2.13-0.484-4.309-0.484-6.527c0-0.925,0.034-1.846,0.091-2.762c0.009-0.151,0.023-0.301,0.034-0.452     c0.059-0.805,0.137-1.607,0.239-2.404c0.014-0.107,0.027-0.215,0.041-0.322c0.776-5.688,2.657-11.134,5.544-16.114     c0.092-0.159,0.157-0.322,0.238-0.483C358.163,180.808,372.275,172.138,388.41,172.138z M229.514,92.69h-24.317l59.627-66.252     l59.627,66.252h-24.317c-4.875,0-8.828,3.952-8.828,8.828v79.448h-52.965v-79.448C238.341,96.642,234.389,92.69,229.514,92.69z      M494.345,489.931H17.655v-291.31h211.859h70.621h29.057c-0.616,2.07-1.123,4.185-1.522,6.34c-0.026,0.14-0.05,0.281-0.075,0.421     c-0.198,1.111-0.366,2.232-0.504,3.362c-0.033,0.271-0.074,0.541-0.103,0.814c-0.047,0.438-0.083,0.88-0.121,1.321     c-0.045,0.508-0.084,1.018-0.116,1.529c-0.024,0.394-0.048,0.789-0.065,1.185c-0.031,0.725-0.049,1.452-0.055,2.182     c-0.001,0.168-0.013,0.334-0.013,0.502h-220.91c-5.191,0-9.261,4.457-8.791,9.627c0.123,1.355,0.184,2.521,0.184,3.615     c0,21.937-17.785,39.724-39.724,39.724c-1.035,0-2.147-0.06-3.667-0.189c-5.151-0.437-9.574,3.626-9.574,8.796v100.804     c-5.601,9.298-8.828,20.188-8.828,31.831c0,34.121,27.672,61.793,61.793,61.793c11.643,0,22.533-3.227,31.831-8.828h277.355     c5.17,0,9.233-4.423,8.796-9.574c-0.129-1.52-0.189-2.632-0.189-3.667c0-21.934,17.79-39.724,39.724-39.724     c1.035,0,2.147,0.06,3.667,0.189c5.151,0.437,9.574-3.626,9.574-8.796V277.848c0-5.17-4.423-9.233-9.574-8.796     c-1.52,0.129-2.632,0.189-3.667,0.189c-8.785,0-17.091-2.878-23.863-7.984c11.968-11.27,19.453-27.249,19.453-44.981     c0-0.976-0.031-1.948-0.077-2.917c-0.01-0.211-0.023-0.42-0.035-0.631c-0.043-0.755-0.101-1.507-0.172-2.257     c-0.023-0.246-0.042-0.494-0.068-0.739c-0.09-0.841-0.195-1.68-0.32-2.514c-0.071-0.485-0.158-0.965-0.24-1.447     c-0.058-0.332-0.115-0.664-0.178-0.995c-0.116-0.617-0.241-1.23-0.375-1.841c-0.003-0.015-0.007-0.03-0.01-0.045     c-0.319-1.443-0.686-2.867-1.103-4.27h46.716V489.931z"/>'
											//+'</g>'
										//+'</g>'
									//+'</g>'
								//+'</svg>'
		    					+'<span class="text-lg font-semibold text-gray-600"><span class="font-black text-xl align-bottom">₩</span> -'+data.spendToday.toLocaleString()+'</span>'
	    					+'</div>'
	    					
	    		if(data.drinkToday != null)
	    			html += '<div class="inline-block">'
		    					+'<svg class="w-6 h-6 inline-block ms-5 mb-2" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" height="800px" width="800px" version="1.1" id="Capa_1" viewBox="0 0 58.166 58.166" xml:space="preserve">'
									+'<g>'
										+'<g>'
											+'<path style="fill:#4b5563;" d="M33.349,6.666V1.65c0-0.912-0.738-1.65-1.647-1.65h-5.234c-0.912,0-1.65,0.739-1.65,1.65v5.016    H33.349z"/>'
											+'<path style="fill:#4b5563;" d="M35.517,17.009c-1.578-1.264-2.61-2.191-2.61-3.509V9.583h-7.646V13.5    c0,1.318-1.034,2.245-2.61,3.509c-1.349,1.081-2.877,2.306-2.877,4.21v8.613h9.309v15.751h-9.311V56.24    c0,1.063,0.862,1.926,1.926,1.926h14.77c1.063,0,1.926-0.862,1.926-1.926V21.22C38.394,19.315,36.864,18.09,35.517,17.009z"/>'
										+'</g>'
									+'</g>'
								+'</svg>'
		    					+'<span class="text-xl font-semibold text-gray-600">-'+data.drinkToday.toLocaleString()+'</span>'
	    					+'</div>'
    			
		    html +=  	'</div>'
		    		+'</div>'
				    +'<ol class="mt-3 divide-y divider-gray-200 ">'
				    +'</ol>'
			    +'</div>'
			    ;
	
	return html;
}

const loadTimelineBtn = 
			'<div class="text-center">'
				+'<button id="timeline-load-btn" class="inline-flex items-center justify-center whitespace-nowrap text-sm font-medium rounded-full bg-gray-900 px-4 py-2 mt-1 text-white">'
			        +'Load More'
			   +'</button>'
			+'</div>';

			
			
			
			
			
function navBtnOn(type) {
	$(".my-page-nav").removeClass("border-2");
	$(".my-page-nav").removeClass("pointer-events-none");
	
	let btnId;
	switch(type){
		case "timeline": 
			btnId = "timeline-btn"
			break;
		case "stats":
			btnId = "stats-btn"
			break;
		case "settings":
			btnId = "settings-btn"			
			break;
	}
	$("#"+btnId).addClass("border-2");
	$("#"+btnId).addClass("pointer-events-none");
}
