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
		$("#timeline-load-btn").remove();
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
				$("#timeline-wrap").append($("#timeline-template .timeline-load-btn-container").clone().attr("id", "timeline-load-btn"));
			}
			
			if(callback)
				callback();
		}
	});
}

function renderTimeline(jsonList, pageSize) {
	let index = 0;
	
	if(jsonList.length < 1) {
		$("#timeline-wrap").html($("#timeline-template .timeline-no-data-container").clone());
		return;
	}
	
	for(data of jsonList) {
		if(index >= pageSize)
			return;
		
		let html = makeTimelineHTML(data);
		
		if($("time[data-date='"+data.date+"']").length < 1) {
			$("#timeline-wrap").append(makeTimelineBlock(data));
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
	let $html = $("#timeline-template .timeline-block-template").clone();
	$html.removeClass("timeline-block-template");
	$html.addClass("timeline-block");
	
	$html.find("time").attr("data-date", data.date);
	$html.find("time").text(data.date);
	
	if(data.spendToday != null){
		$html.find(".spend-today-container").removeClass("hidden");
		$html.find(".spend-today").text(data.spendToday.toLocaleString());
	}
	if(data.drinkToday != null){
		$html.find(".drink-today-container").removeClass("hidden");
		$html.find(".drink-today").text(data.drinkToday.toLocaleString());
	}
	
	return $html;
}
