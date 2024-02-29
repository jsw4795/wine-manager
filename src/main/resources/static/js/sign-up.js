$(() => {
	if($("#id-check").val() == "true"){
		$("#userId").attr("readonly", "");
		$("#userId").addClass("bg-gray-100");
		$("#id-check-btn").attr("disabled", "");
		$("#id-check-btn").addClass("bg-green-200");
	}
		
	// 아이디 중복 확인 버튼 이벤트
	$("#id-check-btn").on("click", idCheckBtnClick);
	
	$("input").on("focus", function() {
		$(this).removeClass("border-red-600")
	})
})


var isIdValid = false;
// 아이디 중복 체크 버튼 클릭 시 실행될 함수
function idCheckBtnClick() {
	let $userIdInput = $("#userId"); 
	let $idCheckBtn = $("#id-check-btn");
	
	// 인풋, 버튼 비활성화
	$userIdInput.attr("readonly", "");
	$userIdInput.addClass("bg-gray-100");
	$idCheckBtn.attr("disabled", "");
	$idCheckBtn.addClass("bg-gray-200");
	
	idCheck($userIdInput.val(), function() {
		console.log("isIdValid : " + isIdValid)
		if(isIdValid == "true") {
			$idCheckBtn.addClass("bg-green-200");
			$("#id-check").val("true");
			alert("Valid!");
		} else if(isIdValid == "false"){
			// 인풋, 버튼 다시 활성화
			$userIdInput.removeAttr("readonly");
			$userIdInput.removeClass("bg-gray-100");
			$userIdInput.removeClass("border-red-600");
			$idCheckBtn.removeAttr("disabled");
			$idCheckBtn.removeClass("bg-gray-300");
			
			$idCheckBtn.addClass("bg-red-200");	
			$("#id-check").val("false");
			alert("This ID is already exist.");
		} else if(isIdValid == "pattern"){
			// 인풋, 버튼 다시 활성화
			$userIdInput.removeAttr("readonly");
			$userIdInput.removeClass("bg-gray-100");
			$idCheckBtn.removeAttr("disabled");
			$idCheckBtn.removeClass("bg-gray-300");
			
			$idCheckBtn.addClass("bg-red-200");	
			$("#id-check").val("false");
			alert("ID must only be in alphabets and numbers and 6 to 15 characters long.");
		} else {
			alert("Error...!")
		}
	});
}

// 아이디 중복 체크
function idCheck(userId, callBack) {
	$.ajax({
		url: "/sign-up/id-check",
		type: "POST",
		data: {userId: userId},
		dataType: "text",
		success: function(result) {
			isIdValid = result;
		},
		complete: callBack
	});
}