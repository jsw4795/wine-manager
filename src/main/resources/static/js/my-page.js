var page = 1;
var pageSize = 10;

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
	
	// timeline 데이터 요청
	requestTimeline()
	
	$("main").on("click", "#timeline-load-btn", function() {
		requestTimeline();
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
function requestTimeline() {
	$.ajax({
		url: "/my-page/timeline",
		type: "GET",
		data: {page: page, pageSize: pageSize},
		dataType: "JSON",
		success: function(result) {
			page++;
			
			renderTimeline(result);
			
			$("#timeline-load-btn").remove();
			
			if(result.length == pageSize){
				$("main").append(loadTimelineBtn);
			}
		}
	});
}

function renderTimeline(jsonList) {
	for(data of jsonList) {
		let html = makeTimelineHTML(data);
		
		if($("time[data-date='"+data.date+"']").length < 1) {
			$("main").append(makeTimelineBlock(data));
		}
		
		$("time[data-date='"+data.date+"']").siblings("ol").append(html);
	}
}

function makeTimelineHTML(data) {
	let html = 
				'<li>'
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
		html += ' and you rated it at <span class="font-medium text-gray-900 ">'+data.rate+'</span></div>';
	} else if(data.rate == null && data.type == "OUT") {
		html += " and haven't rated it yet.</div> ";
	} else if(data.type == "IN") {
		html += ' for <span class="font-medium text-gray-900 ">₩'+data.price+'</span> each.</div>';
	}
	
	if(data.type == "IN"){
		html += '<span class="inline-flex items-center text-xs font-normal text-gray-500 ">'
		                        +'<svg class="w-4 h-4 me-1" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">'
									+'<g xmlns="http://www.w3.org/2000/svg" id="SVGRepo_bgCarrier" stroke-width="0"/>'
									+'<g xmlns="http://www.w3.org/2000/svg" id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"/>'
									+'<g xmlns="http://www.w3.org/2000/svg" id="SVGRepo_iconCarrier"> <path d="M21 5L19 12H7.37671M20 16H8L6 3H3M16 5.5H13.5M13.5 5.5H11M13.5 5.5V8M13.5 5.5V3M9 20C9 20.5523 8.55228 21 8 21C7.44772 21 7 20.5523 7 20C7 19.4477 7.44772 19 8 19C8.55228 19 9 19.4477 9 20ZM20 20C20 20.5523 19.5523 21 19 21C18.4477 21 18 20.5523 18 20C18 19.4477 18.4477 19 19 19C19.5523 19 20 19.4477 20 20Z" stroke="#6B7280" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/> </g>'
		                        +'</svg>'
		                        +'Buy'
		                    +'</span>' 
		                +'</div>'
		            +'</div>'
		       +'</li>'
		       ;
	} else if(data.type == "OUT") {
		html += '<span class="inline-flex items-center text-xs font-normal text-gray-500 ">'
								+'<svg class="w-4 me-1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#6B7280" version="1.1" id="Capa_1" viewBox="0 0 329.257 329.257" xml:space="preserve" stroke="#6B7280" stroke-width="3.6218270000000006">'
									+'<g id="SVGRepo_bgCarrier" stroke-width="0"/>'
									+'<g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"/>'
									+'<g id="SVGRepo_iconCarrier"> <g> <g id="Artwork_30_"> <g id="Layer_5_30_"> <g> <path d="M178.184,47.201c-7.207,0-11.781,8.036-12.862,8.036c-0.947,0-5.334-8.036-12.865-8.036 c-7.325,0-13.307,6.042-13.698,13.357c-0.221,4.132,1.111,7.277,2.989,10.126c3.75,5.691,20.148,19.392,23.598,19.392 c3.522,0,19.773-13.651,23.549-19.392c1.881-2.861,3.211-5.994,2.988-10.126C191.49,53.243,185.509,47.201,178.184,47.201"/> <path d="M136.198,7.898c-5.195,0-8.49,5.791-9.27,5.791c-0.683,0-3.844-5.791-9.271-5.791c-5.279,0-9.59,4.353-9.872,9.626 c-0.159,2.978,0.801,5.245,2.153,7.297c2.703,4.102,14.521,13.976,17.007,13.976c2.539,0,14.251-9.838,16.972-13.976 c1.355-2.063,2.313-4.319,2.153-7.297C145.788,12.251,141.478,7.898,136.198,7.898"/> <path d="M210.531,7.898c-5.194,0-8.491,5.791-9.271,5.791c-0.682,0-3.842-5.791-9.27-5.791c-5.28,0-9.591,4.353-9.873,9.626 c-0.159,2.978,0.801,5.245,2.153,7.297c2.703,4.102,14.521,13.976,17.006,13.976c2.539,0,14.25-9.838,16.973-13.976 c1.356-2.063,2.313-4.319,2.153-7.297C220.122,12.251,215.811,7.898,210.531,7.898"/> <path d="M153.968,114.037c-1.18-7.706-2.733-12.419-7.184-13.708L69.035,77.811c-0.642-0.186-1.295-0.28-1.941-0.28 c-3.727,0-7.147,3.071-11.457,8.027c-3.354,3.86-7.536,9.308-12.093,15.754c-9.083,12.853-17.312,26.286-20.012,32.667 c-6.069,14.335-4.653,31.263,3.986,47.665c6.396,12.146,19.343,24.885,33.303,32.867c-1.354,6.283-4.832,19.74-9.328,35.072 c-7.825,26.69-11.874,35.842-12.879,37.318c-0.506,0.288-2.418,0.885-6.569,0.885c-3.213,0-6.981-0.371-10.898-1.07 l-0.295-0.053c-0.816-0.146-1.771-0.367-2.78-0.602c-2.543-0.589-5.425-1.256-8.199-1.256c-6.405,0-8.825,3.715-9.729,6.83 c-0.245,0.848-0.298,2.178,0.848,3.701c6.518,8.674,64.362,26.016,82,26.016c1.843,0,6.738,0,7.746-3.479 c2.64-9.115-6.633-12.355-12.173-14.293c-0.979-0.342-1.903-0.664-2.675-0.979l-0.272-0.11 c-10.058-4.099-14.341-8.102-14.866-9.178c-0.066-1.801,1.356-11.706,8.805-38.566c4.143-14.941,8.232-28.177,10.402-34.434 c31.063,1.873,70.107-16.889,75.269-48.193c1.127-6.834,1.35-22.587,0.544-38.309 C155.362,125.917,154.739,119.077,153.968,114.037z M144.353,144.211c-0.225,2.083-1.314,2.214-1.963,1.98 c-5.762-2.082-11.756-3.098-18.216-3.098c-9.543,0-18.563,2.201-27.287,4.329c-7.902,1.928-15.367,3.749-22.509,3.75 c-9.005,0-20.859-2.514-32.697-22.124l-0.406-0.669c-0.322-0.613,0.05-2.018,0.831-3.297 c8.42-13.847,19.815-29.134,25.125-34.519c0.523-0.53,1.269-0.439,1.678-0.319c17.016,4.968,64.591,18.771,71.784,20.849 c0.602,0.174,0.845,0.864,0.845,0.864C143.265,117.317,144.595,128.053,144.353,144.211z"/> <path d="M329.113,291.639c-0.903-3.116-3.323-6.831-9.729-6.831c-2.773,0-5.656,0.667-8.198,1.256 c-1.011,0.232-1.965,0.455-2.78,0.602l-0.295,0.053c-3.917,0.699-7.686,1.07-10.898,1.07c-4.199,0-6.107-0.61-6.55-0.863 c-1.027-1.504-5.076-10.657-12.898-37.34c-4.496-15.332-7.974-28.789-9.328-35.07c13.96-7.982,26.906-20.723,33.304-32.868 c8.64-16.403,10.056-33.331,3.986-47.665c-2.699-6.382-10.93-19.815-20.013-32.668c-4.557-6.447-8.738-11.895-12.093-15.755 c-4.311-4.956-7.729-8.027-11.457-8.027c-0.646,0-1.3,0.094-1.941,0.28l-77.749,22.518c-4.451,1.289-6.005,6.002-7.185,13.708 c-0.771,5.04-1.396,11.88-1.802,19.779c-0.808,15.721-0.584,31.474,0.543,38.308c5.162,31.306,44.213,50.076,75.27,48.193 c2.17,6.256,6.259,19.493,10.402,34.434c7.46,26.901,8.875,36.795,8.81,38.539c-0.567,1.141-4.856,5.125-14.868,9.205 l-0.277,0.112c-0.769,0.313-1.693,0.638-2.673,0.979c-5.539,1.938-14.812,5.178-12.173,14.293 c1.009,3.479,5.903,3.479,7.746,3.479c17.639,0,75.482-17.343,82-26.016C329.411,293.817,329.358,292.487,329.113,291.639z M288.113,126.636c0.326,0.585,0.238,1.13,0.113,1.338c-0.081,0.132-0.651,1.074-0.651,1.074 c-11.837,19.607-23.692,22.122-32.698,22.124c-7.142,0-14.607-1.821-22.512-3.75c-8.724-2.128-17.742-4.329-27.283-4.329 c-6.875,0-13.221,1.152-19.325,3.512c-0.276,0.107-0.841,0.053-0.841-0.76c-0.23-15.553,1.186-28.2,2.83-33.989 c0.064-0.227,0.049-0.521,0.798-0.816l71.891-20.822c1.069-0.341,1.297,0.054,1.488,0.243 C267.21,95.68,279.638,112.699,288.113,126.636z"/> </g> </g> </g> </g> </g>'
								+'</svg>'
		                        +'Drink'
		                    +'</span>' 
		                +'</div>'
		            +'</div>'
		        +'</li>'
		        ;
	}
	
	return html;
	
}

function makeTimelineBlock(data) {
	let html = 
				'<div class="p-5 mb-4 border border-gray-100 rounded-lg bg-gray-50 ">'
				    +'<time class="text-lg font-semibold text-gray-900 " data-date="'+data.date+'">'+data.date+'</time>'
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
