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
	
	// 버벅임이 심해서 트렌지션 끝날때 실행되도록 변경
	/*$(window).on("resize", function() {
		if($(this).width() < 767) { // 작아질때
			$(".count").each(function(idx, obj) {
				resize_to_fit_to_small($(obj))
			})
		} else {
			$(".count").each(function(idx, obj) {
				resize_to_fit_to_large($(obj))
			})
		}
	})*/
	
	$(".stats-card").on("transitionend webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd", function() {
		if($(window).width() < 767) {
			resize_to_fit_to_small($(this).find(".count").eq(0))
			resize_to_fit_to_small($(this).find(".count").eq(1))
		} else {
			resize_to_fit_to_large($(this).find(".count").eq(0))
			resize_to_fit_to_large($(this).find(".count").eq(1))
		}
	})
	
})

// 글자 사이즈 컨테이너에 맞게 조절
function resize_to_fit($target){
    let fontsize = $target.css('font-size');
    
    let textWidth = $target.width();
    $target.siblings().each(function() {
		textWidth += $(this).width();
	})

    if(textWidth > $target.parent().width() && $target.height() <= 70){
	    $target.parent().css('fontSize', parseFloat(fontsize) - 1);
    }
}
function resize_to_fit_to_small($target){
	let textWidth = $target.width();
    $target.siblings().each(function() {
		textWidth += $(this).width();
	})
    while(textWidth > $target.parent().width() && $target.height() <= 70){
	    $target.parent().css('fontSize', parseFloat($target.css('font-size')) - 1);
	    
	    textWidth = $target.width();
	    $target.siblings().each(function() {
			textWidth += $(this).width();
		})
    }
}
function resize_to_fit_to_large($target){
    let textWidth = $target.width();
    $target.siblings().each(function() {
		textWidth += $(this).width();
	})
    while(textWidth < $target.parent().width() && $target.height() < 70){
	    $target.parent().css('fontSize', parseFloat($target.css('font-size')) + 1);
	    
	    textWidth = $target.width();
	    $target.siblings().each(function() {
			textWidth += $(this).width();
		})
    }
}

