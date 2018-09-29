// import '../App.css';

import {Line} from 'react-chartjs-2';
// import './static/style.css'
import * as React from "react";
import {Component} from "react";
import {TimePointTr} from "../../../build/lib/src/repository";



function getRandomColor() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

const defdataset = {
    // label: 'My First dataset',
    fill: false,
    lineTension: 0.1,
    // backgroundColor: 'rgba(75,192,192,0.4)',
    borderColor: getRandomColor(),
    borderCapStyle: 'butt',
    borderDash: [],
    borderDashOffset: 0.0,
    borderJoinStyle: 'miter',
    // pointBorderColor: 'rgba(75,192,192,1)',
    pointBackgroundColor: '#fff',
    pointBorderWidth: 1,
    pointHoverRadius: 5,
    // pointHoverBackgroundColor: 'rgba(75,192,192,1)',
    // pointHoverBorderColor: 'rgba(220,220,220,1)',
    pointHoverBorderWidth: 2,
    pointRadius: 1,
    pointHitRadius: 10,
    // data: [65, 59, 80, 81, 56, 55, 40]
};


class Chart extends Component<{series : Map<string, Array<TimePointTr>>}, any> {

    render() {
        const datasets = Array.from(this.props.series,([key,value]) => {
                const nv = value.map(tp=>{
                    return {
                        x : tp.time,
                        y : tp.value
                    }
                })
                return {...defdataset, label: key, data: nv, borderColor: getRandomColor()}
            });
        if (datasets.length == 0) {
            return <div/>
        }


        return (
            <div className="chart">
                <h2>Line Example</h2>
                <Line data={{datasets: datasets}}/>
            </div>
        );
    }

}


export default Chart



