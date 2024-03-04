
$(() => {
	// search 버튼 클릭
	$("#search-btn").on("click", function() {	
		$("#search-modal-content-old").html(getLoadingHTML());	
		$("#search-modal-content-new").html(getLoadingHTML("It can take about 10 seconds"));
		$("#search-modal-background").removeClass("hidden");
		
		let keyword = $("#search").val();
		searchNewWine(keyword);
		searchOwnWine(keyword);
	})
	// search-input 에서 엔터 클릭
	$("#search").on("keypress", function(e) {
		if(e.keyCode == 13)
			return false;	
	})
	$("#search").on("keyup", function(e) {
		if(e.keyCode == 13){
			$("#search-modal-content-old").html(getLoadingHTML());	
			$("#search-modal-content-new").html(getLoadingHTML("It can take about 10 seconds"));
			$("#search-modal-background").removeClass("hidden");
			
			let keyword = $("#search").val();
			searchNewWine(keyword);
			searchOwnWine(keyword);
		}
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
		let isOwnWine = $(this).hasClass("own-wine");
		
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
				$input.attr("checked", false); // radio 초기화
				$input.attr("onclick", "");
				if(isOwnWine){
					$input.attr("onclick", "return false;");
					for(inputTag of $input){
						if($(inputTag).attr("value") == $(data).text()){
							$(inputTag).attr("checked", "");
						}
					}
				}
			} else { // 일반 input인 경우
				$input.removeAttr("readOnly"); 
				if(isOwnWine)
					$input.attr("readOnly", "");
				$input.val(value);
			}
			// 내 와인 검색 시에만 읽기전용 표시
			if(isOwnWine){
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
	
	// 장소 추가 버튼 클릭
	$("#add-place-btn").on("click", function() {
		$("#place-add-input").val('');
		$("#place-modal-error").html('');
		$("#place-modal-background").removeClass("hidden");
	})
	// 모달 안에서 장소 추가 확인 버튼 클릭
	$("#add-place-submit").on("click", function() {
		let place = $("#place-add-input").val();
		
		addPlace(place);
	})
	
})

function searchOwnWine(keyword) {
	$.ajax({
		url: "/add-wine/my-wine-list",
		type: "GET",
		data: {keyword, keyword},
		dataType: "HTML",
		success: function(resultHTML) {
			$("#search-modal-content-old").html(resultHTML);
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
			$("#search-modal-content-new").html(resultHTML);
		}
	});
}

function addPlace(place) {
	$.ajax({
		url: "/add-place",
		type: "POST",
		data: {place: place},
		dataType: "HTML",
		success: function(resultHTML) {
			// 에러가 리턴된 경우
			if($(resultHTML).hasClass("error")){
				$("#place-modal-error").html(resultHTML);
			} else {
				// 리턴된 새로운 option태그로 변경
				$("#buy-place-select").html(resultHTML);
				
				// 모달 닫기
				$(".modal-background").addClass("hidden");
			}
			
		}
	});
}






// HTML Code
function getLoadingHTML(content) {
	return '<div><div id="loading" class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-gray-900 block mx-auto my-5"></div><h3 class="text-xl text-center">'+content+'</h3></div>'
}
