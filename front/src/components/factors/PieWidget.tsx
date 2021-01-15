import * as React from "react";
import Highcharts from "highcharts";
import PieChart from "highcharts-react-official";
import {calcStats, groupBy, Stats} from "../../services/tradeUtils";
import {Trade} from "../../api/models";
import {CatDescription} from "./FlatFactorsPage";


const pieOpt = {
    chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie'
    },
    title: {
        text: 'Browser market shares in January, 2018'
    },
    tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
    },
    accessibility: {
        point: {
            valueSuffix: '%'
        }
    },
    plotOptions: {
        pie: {
            allowPointSelect: true,
            cursor: 'pointer',
            dataLabels: {
                enabled: true,
                format: '<b>{point.name}</b>: {point.percentage:.1f} %'
            }
        }
    },
    series: [] as Array<any>,
}


export const PieWidget = (props: { trades: Array<Trade>, title: string, metric: string, groupFunc: (tr: Trade) => string, categories: Array<CatDescription> }) => {

    const {trades, title, groupFunc, categories, metric} = props

    let map: { [p: string]: Array<Trade> } = groupBy(trades, groupFunc);

    const dataPoint = categories.map(cat => {
        return {
            name: cat.description,
            y: calcStats(map[cat.key] || [])[metric as keyof Stats]
        }
    }).filter(it => it.y);

    if (dataPoint.length === 0) {
        return <div/>
    }


    const optsCopy = {...pieOpt}
    optsCopy.title = {
        text: title
    };

    optsCopy.series = [
        {
            name: metric,
            colorByPoint: true,
            data: dataPoint
        }
    ]

    //<PieChart highcharts={Highcharts} options={options} />

    return (<PieChart highcharts={Highcharts} options={optsCopy}/>)
}