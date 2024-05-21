var spendByTimeChart;
var stockByTimeChart;
var wineByPlaceChart;
var wineByTypeChart;

const colorByWineType = {
	Red: 'rgba(208, 37, 37, 1)',
	White: 'rgba(247, 237, 136, 1)',
	Sparkling: 'rgba(246, 250, 188, 1)',
	Rose: 'rgba(255, 163, 163, 1)',	
};

$(()=>{
	$("main").on("change", "#spend-by-time-select", function() {
		makeSpendByTimeChart($("#spend-by-time-select").val());
	})
	$("main").on("change", "#stock-by-time-select", function() {
		makeStockByTimeChart($("#stock-by-time-select").val());
	})
	$("main").on("change", "#wine-by-type-select", function() {
		makeWineByTypeChart($("#wine-by-type-select").val());
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
			makeWineByPlaceChart();
			makeWineByTypeChart();
			
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
			
			$("#spend-by-time-description").text(getDescription(year));
			
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
				plugins: {
					legend: {
						display: false,
					}
				},
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




function makeStockByTimeChart(year) {
	
	
	if(stockByTimeChart)
		stockByTimeChart.destroy();
	
	$.ajax({
		url: '/my-page/stats/stock-by-time',
		type: "GET",
		data: {year: year},
		dataType: "JSON",
		success: function(result) {
			
			$("#stock-by-time-description").text(getDescription(year));
			
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
				plugins: {
					legend: {
						display: false,
					}
				},
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

function makeWineByPlaceChart() {
	
	if(wineByPlaceChart)
		wineByPlaceChart.destroy();
	
	$.ajax({
		url: '/my-page/stats/wine-by-place',
		type: "GET",
		data: {},
		dataType: "JSON",
		success: function(result) {
			let data = {
				labels: result.map(data => data.place),
				datasets: [
					{
						label: 'bottles',
						data: result.map(data => data.count),
						backgroundColor: [
							'rgba(255, 102, 102, 1)',
							'rgba(255, 218, 107, 1)',
							'rgba(255, 255, 102, 1)',
							'rgba(198, 245, 132, 1)',
							'rgba(112, 255, 186, 1)',
							'rgba(112, 255, 224, 1)',
							'rgba(112, 253, 255, 1)',
							'rgba(112, 207, 255, 1)',
							'rgba(132, 138, 245, 1)',
							'rgba(242, 102, 255, 1)',
							'rgba(168, 168, 168, 1)',
						],
						hoverOffset: 5,
					}
				]
			};
		
		let config = {
			type: 'pie',
			data: data,
			plugins: [ChartDataLabels],
			options: {
				responsive: true,
				plugins: {
					legend: {
						display: false,
					},
					datalabels: {
						font: { 
							weight: 'bold',
							size: '12px',
						},
						color: 'black',
						formatter: function(value, context) {
							return context.chart.data.labels[context.dataIndex];
						},
						display: function(context) {
							if (context.dataIndex < 3) { return 1 }
							else { return 0 }
						},
					}
				},
			}
		};
	
		wineByPlaceChart = new Chart($("#wine-by-place")[0], config);
		}
	})
	
};

function makeWineByTypeChart(wineDataType) {
	
	if(wineByTypeChart)
		wineByTypeChart.destroy();
	
	$.ajax({
		url: '/my-page/stats/wine-by-type',
		type: "GET",
		data: {wineDataType: wineDataType},
		dataType: "JSON",
		success: function(result) {
			
			$("#wine-by-type-description").text(getDescription(wineDataType));
			
			let colorList = [];
			result.forEach((data) => {
				colorList.push(colorByWineType[data.type])
			})
			
			let data = {
				labels: result.map(data => data.type),
				datasets: [
					{
						label: 'bottles',
						data: result.map(data => data.count),
						backgroundColor: colorList,
						hoverOffset: 5,
					}
				]
			};
		
		let config = {
			type: 'pie',
			data: data,
			plugins: [ChartDataLabels],
			options: {
				responsive: true,
				plugins: {
					legend: {
						display: false,
					},
					datalabels: {
						labels:{
							name: {
								align: 'top',
								font: { 
									weight: 'bold',
									size: '12px',
								},
								color: 'black',
								padding: -2,
								formatter: function(value, context) {
									return context.chart.data.labels[context.dataIndex];
								},
							},
							percent: {
								align: 'bottom',
								font: { 
									weight: 'bold',
									size: '12px',
								},
								color: 'black',
								padding: -5,
								formatter: (value, context) => {
									let sum = 0;
									let dataArr = context.chart.data.datasets[0].data;
									dataArr.map(data => {
										sum += data;
									});
									let percentage = (value * 100 / sum).toFixed(1) + "%";
									return percentage;
								},
								display: true,
							}
						},
						
					}
				},
			}
		};
	
		wineByTypeChart = new Chart($("#wine-by-type")[0], config);
		}
	})
	
};



function getDescription(param) {
	if(isNumeric(param)){
		switch(true) {
			case (param > 0):
				return param;
			case (param == 0):
				return 'All time';
			case (param < 0):
				return 'Last ' + (-12 * param) + ' months';
		}		
	} else {
		switch(param) {
			case "hold":
				return 'Wine you have';
			case "buy":
				return 'Wine you bought';
			case "drink":
				return 'Wine you drank';
		}
	}
}

function isNumeric(value) {
    return /^-?\d+$/.test(value);
}
