$(()=>{
	
	$("main").on("click", "#setting-submit-btn", function() {
		if(confirm("변경사항을 저장 하시겠습니까?")){
			$("#setting-form").submit();
		}
	})
	
})

// 세팅 페이지 요청
function requestSetting(callback) {
	editMainWidth("timeline")
	$.ajax({
		url: "/my-page/setting",
		type: "GET",
		data: {},
		dataType: "HTML",
		success: function(result) {
			
			$("main").html(result);
			
			if(callback)
				callback();
		}
	});
}

