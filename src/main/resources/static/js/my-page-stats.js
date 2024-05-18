var spendByTimeChart;
var stockByTimeChart;

$(()=>{
	$("main").on("change", "#spend-by-time-select", function() {
		makeSpendByTimeChart($("#spend-by-time-select").val());
	})
})


function requestStats(callback) {
	editMainWidth("stats");
	
	$.ajax({
		url: "/my-page/stats",
		type: "GET",
		data: {},
		dataType: "HTML",
		success: function(result) {
			
			$("main").html(result);
			
			makeSpendByTimeChart();
			makeStockByTimeChart();
			
			if(callback)
				callback();
		}
	});
}

function makeSpendByTimeChart(year) {
	
	if(spendByTimeChart)
		spendByTimeChart.destroy();
	
	$.ajax({
		url: '/my-page/stats/spend-by-time',
		type: "GET",
		data: {year: year},
		dataType: "JSON",
		success: function(result) {

			let data = {
			datasets: [
				{
					label: 'won',
					data: result,
					borderColor: 'rgba(255, 165, 0, 1)',
					backgroundColor: 'rgba(255, 165, 0, 0.5)',
					pointStyle: 'circle',
					pointRadius: 2,
					pointHoverRadius: 10,
					stepped: false,
				}
			]
		};
		
		let config = {
			type: 'line',
			data: data,
			options: {
				responsive: true,
				plugins: {},
				interaction: {
					intersect: false,
					mode: 'index',
				},
				scales: {
					x: {
						title: {
							display: true,
							text: 'Month'
						}
					},
					y: {
						title: {
							display: true,
							text: 'Money'
						},
						suggestedMin: 0,
						suggestedMax: 10000
					}
			    }
			}
		};
	
		spendByTimeChart = new Chart($("#spend-by-time")[0], config);
		}
	})
	
};




function makeStockByTimeChart() {
	
	if(stockByTimeChart)
		stockByTimeChart.destroy();
	
	$.ajax({
		url: '/my-page/stats/stock-by-time',
		type: "GET",
		data: {},
		dataType: "JSON",
		success: function(result) {
			console.log(result)
			let data = {
			datasets: [
				{
					label: 'bottles',
					data: result,
					borderColor: 'rgba(255, 165, 0, 1)',
					backgroundColor: 'rgba(255, 165, 0, 0.5)',
					pointStyle: 'circle',
					pointRadius: 1,
					pointHoverRadius: 3,
					stepped: true,
				}
			]
		};
		
		let config = {
			type: 'line',
			data: data,
			options: {
				responsive: true,
				plugins: {},
				interaction: {
					intersect: false,
					mode: 'index',
				},
				scales: {
					x: {
						title: {
							display: true,
							text: 'Date'
						}
					},
					y: {
						title: {
							display: true,
							text: 'Bottles'
						},
						suggestedMin: 0,
						suggestedMax: 10,
					}
			    }
			}
		};
	
		stockByTimeChart = new Chart($("#stock-by-time")[0], config);
		}
	})
	
};
