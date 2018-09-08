// @flow
import React from 'react';
import {storiesOf} from '@storybook/react';
import Chart from '../src/components/Chart';

import type {InstrId} from "../src/repository";
import {WidgetComponent} from "../src/components/WidgetComponent";
import type {ChartData} from "../src/components/WidgetComponent";

const getChart= () : ChartData=>{
    const mp = new Map()
    mp.set('aaa', [1,2,3])
    mp.set('bbb',  [2,7,9])
    const index=[new Date(2015,5,1),new Date(2015,5,2),new Date(2015,5,3)]

    return {chart : mp, index : index}


}

storiesOf('Chart', module)
    .add('with text', () =>{
        {
            return <Chart {...getChart()}/>
        }
    } );

storiesOf('Meta', module)
    .add('emeta', () =>{
        {

            let chart = getChart();

            const instr : Array<InstrId> = Array.from(chart.chart.keys()).map(k=>{
                return {
                    name : k,
                    code : k
                }
            })

            return <WidgetComponent id="rando"
                                    onChange={p=>console.log(p)}
                                    period = {1}
                                    instruments={ Array.from(chart.chart.keys())}
                                    chartData={chart}/>
        }
    } );