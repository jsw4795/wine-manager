$(()=> {
	let $statsCard = $(".stats-card");
	
	for($card of $statsCard){
		$statsCard.removeClass("mb-10");
		$statsCard.addClass("mb-0");
		$statsCard.removeClass("opacity-0");
		$statsCard.addClass("opacity-100");
		
	}
	$('.count').each(function() {
		$(this).prop('Counter', 0).animate({
			Counter: $(this).text()
		}, {
			duration: 3000,
			easing: 'easeOutExpo',
			step: function(now) {
				$(this).text(Math.ceil(now).toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,"));
				
				// 숫자 잘리지 않게 사이즈 조절
				resize_to_fit($(this));
			}
		});
	});
	
	
})

// 글자 사이즈 컨테이너에 맞게 조절
function resize_to_fit($target){
    let fontsize = $target.css('font-size');
    
    let textWith = $target.width();
    $target.siblings().each(function() {
		textWith += $(this).width();
	})

    if(textWith > $target.parent().width()){
	    $target.parent().css('fontSize', parseFloat(fontsize) - 1);
    }
}
