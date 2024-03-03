var searchType = "new";
$(() => {
	// old 검색버튼 클릭
	$("#search-old").on("click", function() {		
		$("#search-modal-content").html(getLoadingHTML());
		$("#search-modal-background").removeClass("hidden");
		
		searchType = "old";
		searchOwnWine();
		
	})
	// new 검색버튼 클릭
	$("#search-new").on("click", function() {		
		$("#search-modal-content").html(getLoadingHTML("It can take about 30 seconds"));
		$("#search-modal-background").removeClass("hidden");
		
		searchType = "new";
		let keyword = $("#search").val();
		searchNewWine(keyword);
	})
	
	// 모달 배경 클릭
	$(".modal-background").on("click", function() {
		$(".modal-background").addClass("hidden");
	})
	// 모달 클릭
	$(".modal").on("click", function() {
		return false;
	})
	// 닫기버튼(모달) 클릭
	$(".modal button.close").on("click", function() {
		$(".modal-background").addClass("hidden");
	})
	
	// 검색 모달에서 와인 클릭
	$("#search-modal-content").on("click", ".wine-in-search-result", function() {
		let $dataTag = $(this).find("div.data");
		
		// 태그에 저장된 데이터 -> input
		for(data of $dataTag.children()){
			let key = $(data).attr("class");
			let value = $(data).text();
			let $input = $("input[name=" + key + "]");
			
			// 사진인 경우
			if($(data).attr("class") == "wineThumb")
				$("#wine-thumb").attr("src", $(data).text());
			
			
			// radio 타입인 경우
			if($input.length > 1){
				$input.removeAttr("checked"); // radio 초기화
				$input.attr("onclick", "");
				if(searchType == "old"){
					$input.attr("onclick", "return false;");
					for(inputTag of $input){
						if($(inputTag).attr("value") == $(data).text()){
							$(inputTag).attr("checked", "");
						}
					}
				}
			} else { // 일반 input인 경우
				$input.removeAttr("readOnly"); 
				if(searchType == "old")
					$input.attr("readOnly", "");
				$input.val(value);
			}
			// 내 와인 검색 시에만 읽기전용 표시
			if(searchType == "old"){
				$input.addClass("bg-gray-200");
				$input.addClass("border-gray-300");
				$(".radio-ul li").addClass("bg-gray-200");
				$(".radio-ul li").addClass("border-gray-300");
			} else {
				$input.removeClass("bg-gray-200");
				$input.removeClass("border-gray-300");
				$(".radio-ul li").removeClass("bg-gray-200");
				$(".radio-ul li").removeClass("border-gray-300");
			}
		}
		// 모달 닫기
		$(".modal-background").addClass("hidden");
	})
	
})

function searchOwnWine() {
	$.ajax({
		url: "/add-wine/my-wine-list",
		type: "GET",
		dataType: "HTML",
		success: function(resultHTML) {
			$("#search-modal-content").html(resultHTML);
		}
	});
}
function searchNewWine(keyword) {
	$.ajax({
		url: "/add-wine/search-new",
		type: "GET",
		data: {keyword: keyword},
		dataType: "HTML",
		success: function(resultHTML) {
			$("#search-modal-content").html(resultHTML);
		}
	});
}

// HTML Code
function getLoadingHTML(content) {
	return '<div><div id="loading" class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-gray-900 block mx-auto my-5"></div><h3 class="text-xl">It can take 30 seconds</h3></div>'
}
