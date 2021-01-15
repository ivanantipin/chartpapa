import * as React from "react";
import Highcharts from "highcharts";
import HighchartsReact from "highcharts-react-official";
import {calcStats, groupBy, Stats} from "../../services/tradeUtils";
import {Trade} from "../../api/models";
import {CatDescription} from "./FlatFactorsPage";

const opts = {
    title: {
        text: ''
    },
    xAxis: {
        categories: [] as Array<string>,
        title: {
            text: null
        }
    },
    yAxis: [
        {
            title: {
                align: 'high'
            },
            labels: {
                overflow: 'justify'
            },
            lineWidth: 2,
            lineColor: 'blue'

        },
        {
            title: {
                text: 'Count',
                align: 'high'
            },
            opposite: true,
            labels: {
                overflow: 'justify'
            },
            lineWidth: 2,
            lineColor: 'black'
        }
    ],
    plotOptions: {
        bar: {
            dataLabels: {
                enabled: true
            }
        }
    },
    legend: {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'top',
        x: -40,
        y: 80,
        floating: true,
        borderWidth: 1,
        backgroundColor: '#FFFFFF',
        shadow: true
    },
    credits: {
        enabled: false
    },
    series: [{
        name: 'Year 1800',
        data: [107, 31, 635, 203, 2],
        type: 'line',
        yAxis: 0,
        color: 'black'
    }]
};

export const CategoryFactorWidget = (props: { trades: Array<Trade>, title: string, metric: string, groupFunc: (tr: Trade) => string, categories: Array<CatDescription> }) => {

    const {trades, title, groupFunc, categories, metric} = props;

    let map = groupBy(trades, groupFunc);

    let calced: [string, Stats][] = categories.map(cat => {
        return [cat.key, calcStats(map[cat.key] || [])];
    });

    const optsCopy = {
        ...opts,
        title: {
            text: title
        },
        xAxis: {
            categories: categories.map(it => it.description)
        }
    }


    let cat2count: Map<string, number> = new Map(calced.map(it => [it[0], it[1].cnt]));
    let cat2metric = new Map(calced.map(it => [it[0], it[1][metric as keyof Stats]]));

    optsCopy.series = [
        {
            name: 'cnt',
            data: categories.map(cat => cat2count.get(cat.key)!!),
            type: 'line',
            yAxis: 1,
            color: 'black'

        },
        {
            name: metric,
            data: categories.map(cat => cat2metric.get(cat.key)!!),
            type: 'column',
            yAxis: 0,
            color: 'blue'

        },
    ]

    return (<HighchartsReact highcharts={Highcharts} options={optsCopy}/>)
}