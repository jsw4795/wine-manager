var historyPage = 1;
var reviewPage = 1;
var pageSize = 5;
var wineId;

$(()=> {
	wineId = $("#wine-id").text();
	
	$("#history-load-btn").on("click", function() {
		historyPage++;
		$(this).addClass("hidden");
		loadHistory();
	})
	
	$("#review-load-btn").on("click", function() {
		reviewPage++;
		$(this).addClass("hidden");
		
	})
})

function loadHistory() {
	$.ajax({
		url: "/wine/load",
		type: "GET",
		data: {wineId: wineId, page: historyPage, pageSize: pageSize, type: "log"},
		dataType: "HTML",
		success: function(result) {
			let loadedNumber = $(result).find(".history").length;
			
			htmlForMobile = $(result).find("#wine-log-mobile").html();
			htmlForPC = $(result).find("#wine-log-pc tbody").html();
			
			$("#history-mobile-container").append(htmlForMobile);
			$("#history-pc-container").append(htmlForPC);
			
			// 꽉 채워서 가져왔으면 로드 버튼 생성
			if(loadedNumber == pageSize * 2) // 모바일과 pc버전 두개를 가져오기 때문
				$("#history-load-btn").removeClass("hidden");
		}
	});
}
function loadReview() {
	$.ajax({
		url: "/wine/load",
		type: "GET",
		data: {wineId: wineId, page: historyPage, pageSize: pageSize, type: "review"},
		dataType: "HTML",
		success: function(result) {
			let loadedNumber = $(result).find(".review").length;
			
			$("#review-container").append(result);
			
			// 꽉 채워서 가져왔으면 로드 버튼 생성
			if(loadedNumber == pageSize) 
				$("#history-load-btn").removeClass("hidden");
		}
	});
}