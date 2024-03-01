$(() => {
	// old 검색버튼 클릭
	$("#search-old").on("click", function() {		
		$("#search-modal-content").html(getLoadingHTML());
		$("#search-modal-background").removeClass("hidden");
		searchOwnWine();
		
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
				$("#wine-thumb").attr("src", $(data).text() + "_pb_x600.png");
			
			
			// radio 타입인 경우
			if($input.length > 1){
				$input.removeAttr("checked"); // radio 초기화
				$input.attr("onclick", "return false;");
				for(inputTag of $input){
					if($(inputTag).attr("value") == $(data).text()){
						$(inputTag).attr("checked", "");
					}
				}
			} else { // 일반 input인 경우
				$input.attr("readOnly", "");
				$input.val(value);
			}
			// 읽기전용 표시
			$input.addClass("bg-gray-200");
			$input.addClass("border-gray-300");
			$(".radio-ul li").addClass("bg-gray-200");
			$(".radio-ul li").addClass("border-gray-300");
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


// HTML Code
function getLoadingHTML() {
	return '<div id="loading" class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-gray-900 " ></div>'
}
