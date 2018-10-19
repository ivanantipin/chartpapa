import {Component, default as React} from "react";
import {CandleStickChartProps} from "./OhlcChart";
import {parseDate} from "../../dtutils";
import Highstock from 'highcharts/highstock'

// Note that HighMaps has to be in the codebase already
let ReactHighstock = require('react-highcharts/ReactHighstock.src');


// let HighchartsReact = require('highcharts-react-official')


// Load Highcharts modules
// require('highcharts/indicators/indicators')(Highcharts)
// require('highcharts/indicators/pivot-points')(Highcharts)
// require('highcharts/indicators/macd')(Highcharts)
// require('highcharts/modules/exporting')(Highcharts)
// require('highcharts/modules/map')(Highcharts)
let Annotations = require('highcharts/modules/annotations')


Annotations(Highstock)

const getOpts = (data: CandleStickChartProps) => {

    const ohlcs = data.data.map(dt => {
        return [dt.date.getTime(), dt.open, dt.high, dt.low, dt.close]
    })

    console.log("LALA",data.data[data.data.length - 1])


    const labels = new Array<any>()

    data.tsToLabel.forEach((value, key) => {
        labels.push({
            point: {
                xAxis: 0,
                yAxis: 0,
                x: key,
                y: value.level
            },
            allowOverlap : true,
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
        //id: 'dataseries',
        name: data.name,
        data: ohlcs,
        zIndex: 1
    })

    data.hlines.forEach(hline => {

        series.push({...hline.attributes,
            data: [
                [parseDate(hline.start).getTime(), hline.level],
                [parseDate(hline.end).getTime(), hline.level],
            ],
            shadow: true,
            tooltip: {
                valueDecimals: 2
            }
        })
    })

    const fn = debounced((event : any)=>{
        let ser  = event.target.series[0];
        ser.chart.yAxis[0].setExtremes(ser.dataMin, ser.dataMax, true,false)
    })

    // Create the chart
    return {
        chart: {
            panKey : 'shift',
            zoomType : 'x',
            height: 700,
        },
        rangeSelector: {
            selected: 4
        },
        title: {
            text: data.name
        },
        xAxis : {
            events : {
                afterSetExtremes : fn
            }

        },
        series: series,
        annotations: [ann]

    }
}

function debounced(fn : (event : any)=>void) : (event : any)=>void{
    let timerId : any;
    return function (event : any) {
        if (timerId) {
            clearTimeout(timerId);
        }
        timerId = setTimeout(() => {
            fn(event);
            timerId = null;
        }, 100);
    }
}


export default class HOhlcChart extends Component<CandleStickChartProps, any> {


    render() {
        let opts = getOpts(this.props);
        return <div>
            <ReactHighstock
                // highcharts={Highstock}
                // constructorType='stockChart'
                config={opts}
            /></div>

    }
}