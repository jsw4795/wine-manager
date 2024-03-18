
$(() => {
	// search 버튼 클릭
	$("#search-btn").on("click", function() {	
		if($("#search").val() == '') return;
		$("#search-modal-content-old").html(getLoadingHTML(''));	
		$("#search-modal-background").removeClass("hidden");
		
		let keyword = $("#search").val();
		searchOwnWine(keyword);
	})
	// search-input 에서 엔터 클릭
	$("#search").on("keypress", function(e) {
		if(e.keyCode == 13)
			return false;	
	})
	$("#search").on("keyup", function(e) {
		if($(this).val() == '') return;
		if(e.keyCode == 13){
			$("#search-modal-content-old").html(getLoadingHTML());	
			$("#search-modal-background").removeClass("hidden");
			
			let keyword = $("#search").val();
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
			
			// 와인 재고
			if(key == "wineCount"){
				$("#wine-stock").text(value);
				$("#stock-message").removeClass("hidden");		
				$("input[name='drinkCount']").attr("max", value);
			}
			
			// 사진인 경우
			if($(data).attr("class") == "wineThumb")
				$("#wine-thumb").attr("src", $(data).text());
			
			
			// radio 타입인 경우
			if($input.length > 1){
				$input.attr("checked", false); // radio 초기화
				if(isOwnWine){
					$input.attr("onclick", "return false;");
					for(inputTag of $input){
						if($(inputTag).attr("value") == $(data).text()){
							$(inputTag).attr("checked", "");
						}
					}
				}
			} else { // 일반 input인 경우 (빈티지만 직접 입력 가능하다)
				if($input.attr("id") == "vintage"){
					if(isOwnWine) $input.attr("readOnly", "");
				}
				$input.val(value);
			}
			// 내 와인 검색 시에만 읽기전용 표시 (빈티지만)
			if(isOwnWine){
				if($input.attr("id") == "vintage"){
					$input.addClass("bg-gray-200");
					$input.addClass("border-gray-300");
				}
				$(".radio-ul li").addClass("bg-gray-200");
				$(".radio-ul li").addClass("border-gray-300");
			} 
		}
		// 모달 닫기
		$(".modal-background").addClass("hidden");
	})
	
	// 리뷰랑 같이 작성 버튼(체크박스) 클릭
	$("#review-on-btn").on("click", function() {
		if($(this).prop("checked")){
			$("#review-wrap").removeClass("hidden");
			$("#review-wrap input").removeAttr("disabled");
			$("#review-wrap textarea").removeAttr("disabled");
		} else {
			$("#review-wrap").addClass("hidden");
			$("#review-wrap input").attr("disabled", "");
		}
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


// HTML Code
function getLoadingHTML(content) {
	return '<div><div id="loading" class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-gray-900 block mx-auto my-5"></div><h3 class="text-xl text-center">'+content+'</h3></div>'
}
