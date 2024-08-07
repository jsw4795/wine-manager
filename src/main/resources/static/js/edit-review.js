$(()=> {
	$("#delete-btn").on("click", function() {
		let result = confirm($("#delete-confirm-text").text());
		if(result){
			$("form").attr("action", "/delete-review");
			$("form").submit();
		}
	})
})