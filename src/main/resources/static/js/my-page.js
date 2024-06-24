var globalPageSize = 10;

var myPageData = {
	type: "timeline",
	page: 1,
	scroll: 0,
}

// 뒤로가기를 통해 페이지에 들어온 경우, 페이지 뿌려주기
window.onpageshow = function(event) {
	// 뒤로가기로 온 경우
	if (window.performance && window.performance.navigation.type == 2) {
		
		if(!sessionStorage.getItem("myPageData"))
			return;
			
		myPageData = JSON.parse(sessionStorage.getItem("myPageData"));
		
		// 사파리는 새로 안불러와도 페이지가 유지됨
		if(isSafari){
			myPageData.page++;		
			return;
		}
		
		switch(myPageData.type){
			case "timeline" : 
				navBtnOn("timeline");
				let scroll = myPageData.scroll; // 미리 저장을 안해놓으면 중간에 값이 바뀌기도함
				requestTimelineFirstTime(1, myPageData.page * globalPageSize, function() {
					$(document).scrollTop(scroll);
				});
				break;
			case "stats" : 
				navBtnOn("stats");
				requestStats();
				break;
			case "settings" : 
				navBtnOn("settings");
				break;
			default:
				navBtnOn("timeline");
				requestTimeline(myPageData.page, globalPageSize, function() {
					$(document).scrollTop(0);
				});
		}
	} else if(window.performance.navigation.type == 1) { // 새로고침
	
		if(!sessionStorage.getItem("myPageData"))
			return;
			
		myPageData = JSON.parse(sessionStorage.getItem("myPageData"));
		myPageData.page = 1;
		switch(myPageData.type){
			case "timeline" : 
				navBtnOn("timeline");
				requestTimelineFirstTime(myPageData.page, globalPageSize, function() {
					$(document).scrollTop(0);
				});
				break;
			case "stats" : 
				navBtnOn("stats");
				requestStats();
				break;
			case "settings" : 
				navBtnOn("settings");
				break;
			default:
				navBtnOn("timeline")
				requestTimelineFirstTime(myPageData.page, globalPageSize, function() {
					$(document).scrollTop(0);
				});
		}
	} else { // 그 외 (my page 클릭해서 들어온 경우)
		navBtnOn("timeline")
		requestTimelineFirstTime(myPageData.page, globalPageSize);
	}
}

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
	
	
	
	
	// my-page 네비게이션 클릭 시 초기화
	$(".my-page-nav").on("click", function() {
		$(".my-page-nav").removeClass("border-2");
		$(".my-page-nav").removeClass("pointer-events-none");
		$(this).addClass("border-2");
		$(this).addClass("pointer-events-none");
		$("main").html('');
		myPageData.page = 1;
	})
	$("#timeline-btn").on("click", function() {
		myPageData.type = "timeline";
		
		requestTimelineFirstTime(myPageData.page, globalPageSize);
	})
	$("#stats-btn").on("click", function() {
		myPageData.type = "stats";
		
		requestStats();
	})
	$("#settings-btn").on("click", function() {
		myPageData.type = "settings";
		
		// settings 버튼 클릭 시 로직
	})
	
	// 페이지를 벗어날 때, 세션스토리지에 데이터 저장
	$(window).on("pagehide", function() {
		// 여기서 페이지는 다음에 불러올 페이지를 저장하고 있기때문에 -1
		myPageData.page = Math.max(1, myPageData.page - 1); 
		myPageData.scroll = $(document).scrollTop();
		sessionStorage.setItem("myPageData", JSON.stringify(myPageData));
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


function navBtnOn(type) {
	$(".my-page-nav").removeClass("border-2");
	$(".my-page-nav").removeClass("pointer-events-none");
	
	let btnId;
	switch(type){
		case "timeline": 
			btnId = "timeline-btn"
			break;
		case "stats":
			btnId = "stats-btn"
			break;
		case "settings":
			btnId = "settings-btn"			
			break;
	}
	$("#"+btnId).addClass("border-2");
	$("#"+btnId).addClass("pointer-events-none");
}

function editMainWidth(type) {
	switch(type) {
		case "timeline": 
			$("main").removeClass("md:w-11/12");
			$("main").removeClass("xl:w-10/12");
			
			$("main").addClass("md:w-4/5");
			$("main").addClass("xl:w-3/5");
			break;
		case "stats":
			$("main").removeClass("md:w-4/5");
			$("main").removeClass("xl:w-3/5");
			
			$("main").addClass("md:w-11/12");
			$("main").addClass("xl:w-10/12");
			break;
		case "settings":
			
			break;
	}
}
