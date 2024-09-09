$(()=>{
	
	$("main").on("click", "#timeline-load-btn", function() {
		requestTimeline(myPageData.page, globalPageSize, myPageData.timelineType);
	})
	
	$("main").on("click", ".type-btn", function() {
		let type = $(this).attr("data-type");
		
		// 설정값 수정
		myPageData.page = 1;
		myPageData.timelineType = type;
		
		// 타입 버튼 활성화 상태 변경
		$(".type-btn").removeClass("bg-gray-200 pointer-events-none");
		$(".type-btn").addClass("hover:bg-gray-100");
		$(this).addClass("bg-gray-200 pointer-events-none");
		$(this).removeClass("hover:bg-gray-100")
		
		// 기존 타임라인 삭제 후 정보 요청
		$(".timeline-block").remove();
		$(".timeline-load-btn").remove();
		requestTimeline(myPageData.page, globalPageSize, myPageData.timelineType);
	})
	
})

function requestTimelineFirstTime(page, pageSize, timelineType, callback) {
	// 템플릿 불러오고, 데이터 요청
	$.ajax({
		url: "/my-page/timeline/template",
		type: "GET",
		data: {},
		dataType: "HTML",
		success: function(result) {
			$("main").html(result);
			
			// 타입 버튼 활성화 상태 변경
			$(".type-btn[data-type='"+myPageData.timelineType+"']").addClass("bg-gray-200 pointer-events-none");
			
			requestTimeline(page, pageSize, timelineType, callback);
		}
	});
	
}


// 타임라인 데이터 요청
function requestTimeline(page, pageSize, timelineType, callback) {
	editMainWidth("timeline")
	$.ajax({
		url: "/my-page/timeline",
		type: "GET",
		data: {page: page, pageSize: pageSize, type: timelineType},
		dataType: "JSON",
		success: function(result) {
			myPageData.page++;
			
			renderTimeline(result, pageSize);
			
			$("#timeline-load-btn").remove();
			
			if(result.length == pageSize + 1){
				$("main").append($("#timeline-template .timeline-load-btn-container").clone().attr("id", "timeline-load-btn"));
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
	let $html = '';
	if (data.type == "IN")
		$html = $($("#timeline-template .timeline-template-IN").clone());
	else if (data.type == "OUT")
		$html = $($("#timeline-template .timeline-template-OUT").clone());
	else 
		return '';
		
	$html.find("a").attr("href", "/wine/" + data.wineId);
	$html.find("div.timeline").attr("data-wineId", data.wineId);
	$html.find("div.timeline").attr("data-logId", data.logId);
	$html.find("img.wine-thumb").attr("src", data.thumb);
	$html.find("span.timeline-count").text(data.count);
	$html.find("span.timeline-name").text(data.wineName + (data.vintage != null ? ' ' + data.vintage : '') + (data.wineSize != 'Standard' ? ' ['+data.wineSize+']'  : ''))
	$html.find("span.timeline-place").text(data.place);
	
	if (data.type == "IN"){
		$html.find("span.timeline-price").text(data.price.toLocaleString());		
	} else if (data.rate != null){
		$html.find("span.timeline-rating").text(data.rate.toFixed(Math.max(1, (data.rate.toString().split('.')[1] || []).length)));		
		$html.find(".timeline-rated").removeClass("hidden");
	} else {
		$html.find(".timeline-not-rated").removeClass("hidden");
	}
	
	
	return $html;
	
}

function makeTimelineBlock(data) {
	let html = 
				'<div class="timeline-block p-5 mb-4 border border-gray-100 rounded-lg bg-gray-50 ">'
					+'<div class="flex justify-between">'
				    	+'<time class="text-lg font-semibold text-gray-900 " data-date="'+data.date+'">'+data.date+'</time>'
				    	+'<div class="">'
				    	
		    	if(data.spendToday != null)
	    			html += '<div class="inline-block">'
		    					+'<svg class="w-0 h-0 inline-block" height="800px" width="800px" >'
									+'just for height'
								+'</svg>'
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
