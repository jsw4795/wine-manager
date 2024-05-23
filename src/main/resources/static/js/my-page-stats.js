var spendByTimeChart;
var stockByTimeChart;
var wineByPlaceChart;
var wineByTypeChart;
var wineByCountryChart;
var wineByPriceChart;

const colorByWineType = {
	Red: 'rgba(208, 37, 37, 1)',
	White: 'rgba(247, 237, 136, 1)',
	Sparkling: 'rgba(246, 250, 188, 1)',
	Rose: 'rgba(255, 163, 163, 1)',	
};

const labelForPrice = {
	range0To10: '~10$',
	range10To50: '10~50$',
	range50To100: '50~100$',
	range100To500: '100~500$',
	range500To1000: '500~1000$',
	rangeFrom1000: '1000$~'
};

const colorForPrice = {
	range0To10: 'rgba(117, 209, 255, 1)',
	range10To50: 'rgba(109, 235, 0, 1)',
	range50To100: 'rgba(233, 227, 37, 1)',
	range100To500: 'rgba(244, 140, 21, 1)',
	range500To1000: 'rgba(225, 64, 14, 1)',
	rangeFrom1000: 'rgba(155, 0, 194, 1)'
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
	$("main").on("change", "#wine-by-country-select", function() {
		makeWineByCountryChart($("#wine-by-country-select").val());
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
			makeWineByCountryChart();
			makeWineByPriceChart();
			
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

function makeWineByCountryChart(wineDataType) {
	
	if(wineByCountryChart)
		wineByCountryChart.destroy();
	
	$.ajax({
		url: '/my-page/stats/wine-by-country',
		type: "GET",
		data: {wineDataType: wineDataType},
		dataType: "JSON",
		success: function(result) {
			
			$("#wine-by-country-description").text(getDescription(wineDataType));
			
			let data = {
				labels: result.map(data => data.country),
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
								display: function(context) {
									if (context.dataIndex < 3) { return 1 }
									else { return 0 }
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
								display: function(context) {
									if (context.dataIndex < 3) { return 1 }
									else { return 0 }
								},
							}
						},
						
					}
				},
			}
		};
	
		wineByCountryChart = new Chart($("#wine-by-country")[0], config);
		}
	})
	
};

function makeWineByPriceChart() {
	
	if(wineByPriceChart)
		wineByPriceChart.destroy();
	
	$.ajax({
		url: '/my-page/stats/wine-by-price',
		type: "GET",
		data: {},
		dataType: "JSON",
		success: function(result) {
			// 수량 내림차순으로 정렬
			let entries = Object.entries(result[0]);
			entries.sort(function(a, b) {
				return b[1] - a[1];
			})
			
			let labelList = [];
			let dataList = [];
			let colorList = [];
			entries.forEach((entry, index) => {
				labelList.push(labelForPrice[entry[0]]);
				dataList.push(entry[1]);
				colorList.push(colorForPrice[entry[0]]);
			})
			
			let data = {
				labels: labelList,
				datasets: [
					{
						label: 'bottles',
						data: dataList,
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
								display: function(context) {
									if (context.dataIndex < 3) { return 1 }
									else { return 0 }
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
								display: function(context) {
									if (context.dataIndex < 3) { return 1 }
									else { return 0 }
								},
							}
						},
						
					}
				},
			}
		};
	
		wineByPriceChart = new Chart($("#wine-by-price")[0], config);
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
			case "country-hold":
				return "Wine you have by country";
			case "country-buy":
				return "Wine you bought by country";
			case "country-drink":
				return "Wine you drank by country";
			case "region-hold":
				return "Wine you have by region";
			case "region-buy":
				return "Wine you bought by region";
			case "region-drink":
				return "Wine you drank by region";
		}
	}
}

function isNumeric(value) {
    return /^-?\d+$/.test(value);
}
