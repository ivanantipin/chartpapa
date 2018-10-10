import {Component, default as React} from "react";
import {CandleStickChartProps} from "./OhlcChart";
import {parseDate} from "../../dtutils";
import Highcharts from 'highcharts/highstock'

let HighchartsReact = require('highcharts-react-official')


// Load Highcharts modules
require('highcharts/indicators/indicators')(Highcharts)
require('highcharts/indicators/pivot-points')(Highcharts)
require('highcharts/indicators/macd')(Highcharts)
require('highcharts/modules/exporting')(Highcharts)
require('highcharts/modules/map')(Highcharts)
require('highcharts/modules/annotations')(Highcharts)


const getOpts = (data: CandleStickChartProps) => {

    const ohlcs = data.data.map(dt => {
        return [dt.date.getTime(), dt.open, dt.high, dt.low, dt.close]
    })


    const labels = new Array<any>()

    data.tsToLabel.forEach((value, key) => {
        labels.push({

            point: {
                xAxis: 0,
                yAxis: 0,
                x: key,
                y: value.level
            },
            text: value.text,
            backgroundColor: value.color,
            verticalAlign: value.drawOnTop ? 'bottom' : 'top',
            distance : value.drawOnTop ? 10 : -30,
            //y: -15

        })
    })

    const ann = {
        labels: labels
    }


    const series = new Array<any>()

    series.push({
        type: 'ohlc',
        id: 'dataseries',
        name: data.name,
        data: ohlcs,
        zIndex: 1
    })

    data.hlines.forEach(hline => {
        series.push({
            // name: 'Trend Line',
            data: [
                [parseDate(hline.start).getTime(), hline.level],
                [parseDate(hline.end).getTime(), hline.level],
            ],
            dashStyle: 'dot',
            shadow: true,
            tooltip: {
                valueDecimals: 2
            }
        })
    })


    // Create the chart
    return {
        chart: {
            height: 700,
        },
        rangeSelector: {
            selected: 4
        },
        title: {
            text: data.name
        },
        series: series,
        annotations: [ann]

    }
}


export default class HOhlcChart extends Component<CandleStickChartProps, any> {


    render() {
        let opts = getOpts(this.props);
        return <div>
            <HighchartsReact
                highcharts={Highcharts}
                constructorType='stockChart'
                options={opts}
            /></div>

    }
}