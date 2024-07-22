var globalPageSize = 5;

var wineDetailData = {
	wineId: -1,
	historyPage: 2,
	reviewPage: 2,
	scroll: 0,
}

window.onpageshow = function(event) {
	// 뒤로가기, 일반적인 방법으로 온 경우
	if (window.performance.navigation.type == 2 || window.performance.navigation.type == 0) {
		console.log(JSON.parse(sessionStorage.getItem("wineDetailData")));
		
		// 세션스토리지에 데이터가 존재하고, 와인 아이디가 일치할 때만 로직 실행
		if(!sessionStorage.getItem("wineDetailData") || JSON.parse(sessionStorage.getItem("wineDetailData")).wineId != $("#wine-id").text()) {
			return;
		}
		
		wineDetailData = JSON.parse(sessionStorage.getItem("wineDetailData"));
		
		// 사파리는 새로 안불러와도 페이지가 유지됨
		if(isSafari){
			myPageData.page++;		
			return;
		}
		
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
	$(window).on("pagehide", function() {
		wineDetailData.historyPage = Math.max(2, wineDetailData.historyPage);
		wineDetailData.reviewPage = Math.max(2, wineDetailData.reviewPage);
		wineDetailData.scroll = $(document).scrollTop();
		
		sessionStorage.setItem("wineDetailData", JSON.stringify(wineDetailData));
	})
	
	// 비고 버튼 클릭 시 보이기
	$("html").on("click", ".note-btn",function() {
		let logId = $(this).attr("data-logId");
		$(".note-" + logId).removeClass("hidden");
		
		// 비고 팝업 외의 요소 클릭 시 비고 숨기기
		$("html").on("click.closePopOver-" + logId, function() {
			$(".note-" + logId).addClass("hidden");
			$("html").off("click.closePopOver-" + logId);
		})
	});
	// 비고 팝업 클릭 시 숨김 무시
	$("html").on("click", ".note",function() {
		return false;
	});
	
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