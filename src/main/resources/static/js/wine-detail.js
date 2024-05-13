var globalPageSize = 5;

var wineDetailData = {
	wineId: -1,
	historyPage: 2,
	reviewPage: 2,
	scroll: 0,
}

window.onpageshow = function(event) {
	// 뒤로가기, 일반적인 방법으로 온 경우
	if ((window.performance && window.performance.navigation.type == 2 && sessionStorage.getItem("wineDetailData")) || (window.performance && window.performance.navigation.type == 0 && sessionStorage.getItem("wineDetailData"))) {
		console.log(JSON.parse(sessionStorage.getItem("wineDetailData")));
		
		// 저장된 와인 아이디와 같을 때만 로직 실행
		if(JSON.parse(sessionStorage.getItem("wineDetailData")).wineId != $("#wine-id").text()) {
			return;
		}
		
		wineDetailData = JSON.parse(sessionStorage.getItem("wineDetailData"));
		
		let scroll = wineDetailData.scroll;
		if(wineDetailData.historyPage > 2){
			loadHistory(2, (wineDetailData.historyPage - 1) * globalPageSize, function() {
				$(document).scrollTop(scroll);
			})
		}
		if(wineDetailData.reviewPage > 2){
			loadReview(2, (wineDetailData.reviewPage - 1) * globalPageSize, function() {
				$(document).scrollTop(scroll);
			})
		}
		
	} 
}

$(()=> {
	wineDetailData.wineId = $("#wine-id").text();
	
	$("#history-load-btn").on("click", function() {
		loadHistory(wineDetailData.historyPage, globalPageSize);
	})
	
	$("#review-load-btn").on("click", function() {
		loadReview(wineDetailData.reviewPage, globalPageSize)
	})
	
	// 페이지 나갈때 정보 저장
	$(window).on("beforeunload", function() {
		wineDetailData.historyPage = Math.max(2, wineDetailData.historyPage);
		wineDetailData.reviewPage = Math.max(2, wineDetailData.reviewPage);
		wineDetailData.scroll = $(document).scrollTop();
		
		sessionStorage.setItem("wineDetailData", JSON.stringify(wineDetailData));
	})
})

function loadHistory(page, pageSize, callBack) {
	$("#history-load-btn").addClass("hidden");
	$.ajax({
		url: "/wine/load",
		type: "GET",
		data: {wineId: wineDetailData.wineId, page: page, pageSize: pageSize, type: "log"},
		dataType: "HTML",
		success: function(result) {
			wineDetailData.historyPage++;
			
			htmlForMobile = $(result).find("#wine-log-mobile").html();
			htmlForPC = $(result).find("#wine-log-pc tbody").html();
			
			$("#history-mobile-container").append(htmlForMobile);
			$("#history-pc-container").append(htmlForPC);
				
			if($(result).find("#hasNext").text() == "true")
				$("#history-load-btn").removeClass("hidden");
				
			if(callBack)
				callBack();
		}
	});
}
function loadReview(page, pageSize, callBack) {
	$("#history-load-btn").addClass("hidden");
	$.ajax({
		url: "/wine/load",
		type: "GET",
		data: {wineId: wineDetailData.wineId, page: page+1, pageSize: pageSize, type: "review"},
		dataType: "HTML",
		success: function(result) {
			wineDetailData.reviewPage++;
			
			$("#review-container").append($(result).find("#html-result").html());
				
			if($(result).find("#hasNext").text() == "true")
				$("#history-load-btn").removeClass("hidden");
			
			if(callBack)
				callBack();
		}
	});
}