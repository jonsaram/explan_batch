

function _lineRateChart(container, categories, series) {
   const chart =  $("#"+container).highcharts({
        chart: {
			type : 'line',
            zoomType: 'xy',
            margin: [50, 0, 40, 45],
        },
        credits: { enabled: false },
        title: {
            text: '',
            align: 'left'
        },
        subtitle: {
            text: '',
            align: 'left'
        },
        xAxis: [{
            labels: {
                style: {
                    color: '#545454',
                    fontFamily: 'Noto Sans KR',
                    fontSize: '11px'
                }
            },
            lineColor: '#dddddd',
            categories: categories,
            crosshair: true
        }],
        yAxis: [{
            labels: {
                format: '{value}%',
                style: {
                    color: '#8A8C92',
                    fontFamily: 'Noto Sans KR',
                    fontSize: '11px'
                }
            },
            title: {
                text: ''
            }
        }, { // Secondary yAxis
            title: {
                text: '',
                style: {}
            },
            labels: {
                format: ' '
            },
            opposite: true
        }],
        tooltip: {
            shared: true
        },
        legend: {
            align: 'right',
            x: 0,
            verticalAlign: 'top',
            y: 0,
            itemStyle: {
                color: '#545454',
                fontFamily: 'Noto Sans KR',
                fontSize: '11px'
            },
            symbolRadius: 0,
            symbolWidth: 10,
            symbolHeight: 10,
            x:  0,
            y: -2,
        },
        plotOptions: {
            line: {
                marker: {
                    enabled: true
                },
	            dataLabels: {
	                enabled: true
	            },
	            enableMouseTracking: false
            }
        },
		series : series
    });			
}	



function _barLineChart (container, categories, series  ) {				
   const chart =  $("#"+container).highcharts({
        chart: {
            zoomType: 'xy'
        },
        title: {
            text: '',
            align: 'left'
        },
        subtitle: {
            text: '',
            align: 'left'
        },
        xAxis: [{
            labels: {
                style: {
                    color: '#545454',
                    fontFamily: 'Noto Sans KR',
                    fontSize: '11px'
                }
            },
            lineColor: '#dddddd', /* x축 라인색 */
            categories: categories,
            crosshair: true
        }],
	    yAxis: [{ // Primary yAxis
	        labels: {
	            format: '{value} 원',
	            style: {
	                color: Highcharts.getOptions().colors[1]
	            }
	        },
	        title: {
	            text: '1주당 주식가액',
	            style: {
	                color: Highcharts.getOptions().colors[1]
	            }
	        }
	    }, { // Secondary yAxis
	        title: {
	            text: '기업가치',
	            style: {
	                color: Highcharts.getOptions().colors[0]
	            }
	        },
	        labels: {
	            format: '{value} 억원',
	            style: {
	                color: Highcharts.getOptions().colors[0]
	            }
	        },
	        opposite: true
	    }],
        tooltip: {
            shared: true
        },
	    legend: {
	        align: 'left',
	        x: 80,
	        verticalAlign: 'top',
	        y: 60,
	        floating: true,
	        backgroundColor:
	            Highcharts.defaultOptions.legend.backgroundColor || // theme
	            'rgba(255,255,255,0.25)'
	    },
        plotOptions: {
            line: {
                marker: {
                    enabled: true
                },
	            dataLabels: {
	                enabled: true
	            },
	            enableMouseTracking: false
            },
            column: {
                marker: {
                    enabled: true
                },
	            dataLabels: {
	                enabled: true
	            },
	            enableMouseTracking: false
            },
        },
		series : series
    });
}
		
function _barLeftChart(container, chartArray  ) {				
   const chart =  $("#"+container).highcharts({
        chart: {
        	type: 'bar'
        },
        title: {
            text: ''
        },
        subtitle: {
            text: '',
        },
        xAxis: [{
            categories: ['플랜 적용 전 기업가치','배당 플랜 후 기업가치','배당&퇴직금 플랜 후 기업가치'],
            title: {
            	text: ''
            },
            gridLineWidth: 1,
            lineWidth: 0,
        }],
	    yAxis: [{ 
	        title: {
	            text: '',
	            align: 'high'
	        },
	        labels: {
	            overflow: 'justify',
	            style: {
	                color: Highcharts.getOptions().colors[1]
	            }					            
	        },
	        gridLineWidth: 0
	    }],
        tooltip: {
        	valueSuffix: ' 억원'
        },
	    legend: {
	        layout: 'horizon',
	        align: 'right',
	        verticalAlign: 'top',
	        x: 0,
	        y: 0,
	        floating: true,
	        borderWidth: 1,
	        backgroundColor:
	            Highcharts.defaultOptions.legend.backgroundColor || '#FFFFFF',
	        shadow: true
	    },
	    plotOptions: {
	        bar: {
	            borderRadius: '50%',
	            dataLabels: {
	                enabled: true
	            },
	        }
	    },
        series: [{
        	data: chartArray,
        	showInLegend: false
        }]
    });
}		


function _1bar2LineChart(container, categories, series) {
   const chart =  $("#"+container).highcharts({
        chart: {
            zoomType: 'xy'
        },
        title: {
            text: '',
            align: 'left'
        },
        subtitle: {
            text: '',
            align: 'left'
        },
        xAxis: [{
            labels: {
                style: {
                    color: '#545454',
                    fontFamily: 'Noto Sans KR',
                    fontSize: '11px'
                }
            },
            lineColor: '#dddddd', /* x축 라인색 */
            categories: categories,
            crosshair: true
        }],
	    yAxis: [{ // Primary yAxis
	        labels: {
	            format: '{value} %',
	            style: {
	                color: Highcharts.getOptions().colors[1]
	            }
	        },
	        title: {
	            text: '부담률',
	            style: {
	                color: Highcharts.getOptions().colors[1]
	            }
	        }
	    }, { // Secondary yAxis
	        title: {
	            text: '10%지분가치',
	            style: {
	                color: Highcharts.getOptions().colors[0]
	            }
	        },
	        labels: {
	            format: '{value} 억원',
	            style: {
	                color: Highcharts.getOptions().colors[0]
	            }
	        },
	        opposite: true
	    }],
        tooltip: {
            shared: true
        },
	    legend: {
	        align: 'left',
	        x: 80,
	        verticalAlign: 'top',
	        y: 60,
	        floating: true,
	        backgroundColor:
	            Highcharts.defaultOptions.legend.backgroundColor || // theme
	            'rgba(255,255,255,0.25)'
	    },
        plotOptions: {
            line: {
                marker: {
                    enabled: true
                },
	            dataLabels: {
	                enabled: true
	            },
	            enableMouseTracking: false
            },
            column: {
                marker: {
                    enabled: true
                },
	            dataLabels: {
	                enabled: true
	            },
	            enableMouseTracking: false
            },
        },
		series : series
    });
}
