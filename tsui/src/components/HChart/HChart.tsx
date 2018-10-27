import {Component, default as React} from "react";
import * as Highcharts from "highcharts";
import {TimePointTr} from "repository";

let HighchartsReact = require('highcharts-react-official')


const getOpts=(data : Map<string, Array<TimePointTr>>)=>{
    const dt : Array<any> = []

    //let cnt = 0
    data.forEach((val : Array<TimePointTr>,key)=>{

        dt.push(
            {
                name : key,
                data : val.map(p=>[p.time.getTime(), p.value])
            }
        )

    })

    return {
    chart: {
        type: 'spline'
    },
    title: {
        text: 'Snow depth at Vikjafjellet, Norway'
    },
    subtitle: {
        text: 'Irregular time data in Highcharts JS'
    },
    xAxis: {
        type: 'datetime',
/*
        dateTimeLabelFormats: {
            day: '%d. '

        },

        title: {
            text: 'Date'
        }
*/
    },
    yAxis: {
        title: {
            text: 'Snow depth (m)'
        },
        min: 0
    },
    tooltip: {
        headerFormat: '<b>{series.name}</b><br>',
        pointFormat: '{point.x:%e. %b}: {point.y:.2f} m'
    },

    plotOptions: {
        spline: {
            marker: {
                enabled: true
            }
        }
    },

    colors: ['#6CF', '#39F', '#06C', '#036', '#000'],

    // Define the data points. All series have a dummy year
    // of 1970/71 in order to be compared on the same x axis. Note
    // that in JavaScript, months start at 0 for January, 1 for February etc.
    series: dt
}}


export default class HChart extends Component<{series : Map<string, Array<TimePointTr>>}, any> {


    render() {
        return(
            <div style={{width : 500}}>
        <HighchartsReact
            highcharts={Highcharts}
            options={getOpts(this.props.series)}
        /></div>)
    }
}