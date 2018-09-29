// @flow
import React from 'react';
import {storiesOf} from '@storybook/react';
import Chart from '../src/components/Chart';

import type {InstrId} from "../src/repository";
import {WidgetComponent} from "../src/components/WidgetComponent";
import type {ChartData} from "../src/components/WidgetComponent";
import OhlcChart, {Ohlc} from '../src/components/OhlcChart'

import {MainControllerApi} from "../src/api/api";

import type {CandleStickChartProps} from "../src/components/OhlcChart";
import * as d3 from "d3-time-format";
import {timeParse} from "d3-time-format";
import type {Annotations} from "../src/api";

const getChart = (): ChartData => {
    const mp = new Map();
    mp.set('aaa', [1, 2, 3]);
    mp.set('bbb', [2, 7, 9]);
    const index = [new Date(2015, 5, 1), new Date(2015, 5, 2), new Date(2015, 5, 3)];

    return {chart: mp, index: index}


};

storiesOf('Chart', module)
    .add('with text', () => {
        {
            return <Chart {...getChart()}/>
        }
    });

storiesOf('Meta', module)
    .add('emeta', () => {

        let chart = getChart();

        const instr: Array<InstrId> = Array.from(chart.chart.keys()).map(k => {
            return {
                name: k,
                code: k
            }
        });

        return <WidgetComponent id="rando"
                                onChange={p => console.log(p)}
                                period={1}
                                instruments={Array.from(chart.chart.keys())}
                                chartData={chart}/>
    });


storiesOf('OhlcChart', module)

    .add('Ohlc', () => {
        const promise = Promise.all([MainControllerApi().getChartUsingGET("tatn"),MainControllerApi().getAnnotationsForChartUsingGET("tatn")]).then(values=>{
            const ohlcs = values[0];
            const an : Annotations = values[1];//2018-08-23T00:00:00


            console.log("aaa", an);
            let parser = timeParse("%Y-%m-%dT%H:%M:%S");

            let anParser = timeParse("%Y-%m-%dT%H:%M");
            //2018-08-30T00:00

            const lab = new Map();

            an.labels.forEach(lb=>{
                console.log("lb",lb);
                lab.set(parser(lb.time).getTime(),lb)
            });

            console.log("ohlcs",ohlcs);

            let mapped = ohlcs.map(oh => {

                return {
                    date: parser(oh.dateTime),
                    open: oh.open,
                    high: oh.high,
                    low: oh.low,
                    close: oh.close,
                    volume: 0
                }
            });

            console.log('mapped',mapped);

            const prop: CandleStickChartProps = {
                data: mapped,
                width: 600,
                ratio: 0,
                type: "svg",
                tsToLabel : lab,
                hlines : an.lines
            };
            return prop
        }).catch(ee => {
            console.log(ee)
        });

        const AAA = withProperties(OhlcChart, promise);

        return <AAA/>
    });


function withProperties<T>(Comp: React.Component<T>, promise: Promise<T>) {
    return class extends React.Component<any, { data: T }> {
        constructor() {
            super();
            promise.then(t => {
                this.setState({data: t})
            })
        }


        render() {
            if (this.state) {
                return <Comp {...this.state.data}/>;
            }
            return <div/>
        }
    };
}

