
$(() => {
	
	// 이미지 변경 버튼 클릭하면 input 클릭 트리거	
	$("#custom-image-btn").on("click", function() {
		$("#custom-image").trigger("click");
	})
	
	// 파일 인풋에 파일 들어오면 썸네일 변경
	$("#custom-image").on("change", function(e) {
		setThumbnail(e);
	})
	
	$("#delete-btn").on("click", function() {
		let result = confirm($("#delete-confirm-text").text());
		if(result){
			$("form").attr("action", "/delete-wine");
			$("form").submit();
		}
	})
	
})

function setThumbnail(e) {
	let reader = new FileReader();

		reader.onload = function(e) {
			$("#wine-thumb").attr("src", e.target.result);
		};

		reader.readAsDataURL(e.target.files[0]);
}
