const defaultImage = "/images/wine-default.png";

$(() => {
	// search 버튼 클릭
	$("#search-btn").on("click", function() {	
		search();
	})
	// search-input 에서 엔터 클릭
	$("#search").on("keypress", function(e) {
		if(e.keyCode == 13)
			return false;	
	})
	$("#search").on("keyup", function(e) {
		if(e.keyCode == 13){
			search();
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
	$("#loading-background").on("click", function() {
		return false;
	})
	
	// 검색 모달에서 와인 클릭
	$("#search-modal-content").on("click", ".wine-in-search-result", function() {
		let $dataTag = $(this).find("div.data");
		let isOwnWine = $(this).hasClass("own-wine"); // 등록된 와인 선택 여부
		
		if(isOwnWine){
			setWineInputTags($dataTag, isOwnWine);
		} else {
			getMoreInfo($(this));			
		}
		
		
		
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
	
	// 장소 리스트에서 선택하면 인풋값 바뀜
	$("#buy-place-select").on("input", function() {
		let input = $(this).val();
		let $buyPlaceInput = $("#buy-place-text");
		
		if(input == "DI") {
			$buyPlaceInput.val('');
			$buyPlaceInput.removeAttr("readOnly")
			$buyPlaceInput.removeClass("bg-gray-200");
		} else {
			$buyPlaceInput.val(input);
			$buyPlaceInput.attr("readOnly", '')
			$buyPlaceInput.addClass("bg-gray-200");
		}
		
	})
	
	// 장소 추가 모달에서 엔터키 클릭 처리
	$("#place-add-input").on("keypress", function(e) {
		if(e.keyCode == 13)
			return false;	
	})
	$("#place-add-input").on("keyup", function(e) {
		if(e.keyCode == 13){
			let place = $("#place-add-input").val();
		
			addPlace(place);
		}	
	})
	
	$(".date-btn").on("click", function() {
		let type = $(this).attr("id");
		let $date = $("#date");
		let date = new Date();
		date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
		
		switch(type) {
			case "today-btn":
				$date.val(date.toISOString().substring(0, 10));
				break;
			case "yesterday-btn":
				date.setDate(date.getDate() - 1);
				$date.val(date.toISOString().substring(0, 10));
				break;
			case "2daysAgo-btn":
				date.setDate(date.getDate() - 2);
				$date.val(date.toISOString().substring(0, 10));
				break;
		}
	})
	
	
})

function search() {
	$("#search-modal-content-old").html(getLoadingHTML());	
	$("#search-modal-content-new").html(getLoadingHTML());
	$("#search-modal-background").removeClass("hidden");
	
	let keyword = $("#search").val();
	searchNewWine(keyword);
	searchOwnWine(keyword);
}

function searchOwnWine(keyword) {
	let uuid = uuidv4();
	$.ajax({
		url: "/add-wine/my-wine-list",
		type: "GET",
		data: {keyword, keyword, uuid: uuid},
		dataType: "HTML",
		success: function(resultHTML) {
			if($(resultHTML).find("#uuid").text() == uuid)
				$("#search-modal-content-old").html(resultHTML);
		}
	});
}
var uuid;
function searchNewWine(keyword) {
	let uuid = uuidv4();
	$.ajax({
		url: "/add-wine/search-new",
		type: "GET",
		data: {keyword: keyword, uuid: uuid},
		dataType: "HTML",
		success: function(resultHTML) {
			if($(resultHTML).find("#uuid").text() == uuid)
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
				$("#buy-place-text").removeAttr("readOnly")
				$("#buy-place-text").removeClass("bg-gray-200");
				// 모달 닫기
				$(".modal-background").addClass("hidden");
			}
			
		}
	});
}

function getMoreInfo($this) {
	// 선택된 와인 정보 보내면서 추가 정보 요청		
	$("#loading-background").removeClass("hidden"); // 로딩중 띄움
	$.ajax({
		url: "/add-wine/search-new/more-info",
		type: "GET",
		data: {wineName: $this.find("div.data .wineName").text(),
			   wineCountry: $this.find("div.data .wineCountry").text(),
			   wineRegion: $this.find("div.data .wineRegion").text(),
			   wineAverageRating: $this.find("div.data .wineAverageRating").text(),
			   wineRating: $this.find("div.data .wineRating").text(),
			   wineLink: $this.find("div.data .wineLink").text(),
			   wineThumb: $this.find("div.data .wineThumb").text(),
			   wineThumbBottom: $this.find("div.data .wineThumbBottom").text()},
		dataType: "JSON",
		success: function(wine) {
			// 전체 초기화 
			$("img#wine-thumb").attr("src", defaultImage);
			$("#wine-info input:not([type='radio'])").val(""); // radio타입은 value 없애면 안됨
			$("select#vintage").removeClass("bg-gray-200");
			$("select#vintage").removeClass("border-gray-300");
			$("select#vintage").removeClass("pointer-events-none");
			$("select[name='wineVintage'] option").prop("selected", false);
			$("select[name='wineVintage'] option").eq(0).prop("selected", 'selected');
			$("input[type='radio']").attr("onclick", "");
			$("input[type='radio']").prop("checked", false);
			$(".radio-ul li").removeClass("bg-gray-200");
			$(".radio-ul li").removeClass("border-gray-300");
			
			// 썸네일 바뀌게 설정
			$("img#wine-thumb").attr("src", wine.thumb);
			
			// input태그 값 설정
			$('input[name="wineName"]').val(wine.name);
			$('input[name="wineCountry"]').val(wine.country);
			$('input[name="wineRegion"]').val(wine.region);
			$('input[name="wineAverageRating"]').val(wine.averageRating);
			$('input[name="wineRating"]').val(wine.rating);
			$('input[name="wineAveragePrice"]').val(wine.averagePrice);
			$('input[name="wineThumb"]').val(wine.thumb);
			$('input[name="wineThumbBottom"]').val(wine.thumbBottom);
			$('input[name="wineLink"]').val(wine.link);
			$('input[name="wineAlcohol"]').val(wine.alcohol);
			$('input[name="wineGrape"]').val(wine.grape);
			$('input[name="wineWinery"]').val(wine.winery);
			
			
			// wineType태그(radio) 값 설정
			let $wineTypeInput = $('input[name="wineType"]');
			if(wine.wineType) { // 와인타입 값이 들어왔을 때만
				// 값 설정
				$("input[name='wineType'][value='"+wine.wineType+"'").prop("checked", "chekced");
				// 클릭 안되게 설정
				$wineTypeInput.attr("onclick", "return false;");
				// 회색 처리
				$(".wine-type-ul li").addClass("bg-gray-200");
				$(".wine-type-ul li").removeClass("border-gray-200");
				$(".wine-type-ul li").addClass("border-gray-300");
			}
			
			// 로딩중 제거
			$("#loading-background").addClass("hidden");
			// 모달 닫기
			$(".modal-background").addClass("hidden");
			
		}
	});
}

function setWineInputTags($dataTag, isOwnWine) {
	// 태그에 저장된 데이터 -> input
	for(data of $dataTag.children()){
		let key = $(data).attr("class");
		let value = $(data).text();
		let $input = $("input[name=" + key + "]");
		
		// 사진인 경우
		if(key == "wineThumb")
			$("#wine-thumb").attr("src", $(data).text());
		
		
		// radio 타입인 경우
		if($input.length > 1){
			$input.prop("checked", false); // radio 초기화
			$input.attr("onclick", "");
			$input.attr("onclick", "return false;");
			$("input[type='radio'][value='"+$(data).text()+"'").prop("checked", "chekced");
		} else { // 일반 input인 경우
			$input.val(value);
		}
		
		// select태그 값 설정(빈티지)
		if(key == "wineVintage"){
			$("select[name='wineVintage'] option").prop("selected", false);
			$("select[name='wineVintage'] option[value='"+value+"']").prop("selected", 'selected');			
		}
		
	}
	
	
	// 읽기전용 표시 (빈티지만)
	$("select#vintage").addClass("bg-gray-200");
	$("select#vintage").addClass("border-gray-300");
	$("select#vintage").addClass("pointer-events-none");
	
	$(".radio-ul li").addClass("bg-gray-200");
	$(".radio-ul li").addClass("border-gray-300");
	
	// 모달 닫기
	$(".modal-background").addClass("hidden");
}


// uuid 생성 함수
function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}





function getLoadingHTML() {
	return $("#template .loading-container").clone().html();
}
