// @flow

import React, {Component} from 'react';
import Select from "react-virtualized-select";
import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";
import Chart from "./Chart";
import type {InstrId} from "../repository";

import './static/style.css'


export type ChartData ={
    index: Array<Date>,
    chart: Map<string, Array<number>>,
}

export type WidgetData = {
    period: number,
    id: string,
    chartData : ChartData,
    instruments: Array<string>
}

export type WidgetCallbacks = {
    onChange: (metaId: string, codes: Array<string>) => void
}


export class WidgetComponent extends Component<WidgetData & WidgetCallbacks, any> {

    constructor() {
        super()
        this.state = {}

    }


    onSelect(opts: Array<{name : string,value : string}>) {
        console.log("on select",opts)
        this.props.onChange(this.props.id, opts.map(o=>o.value))
    }


    render() {
        const selected = this.props.instruments
        return (
            <div className="widget">
                <Select
                    onChange={this.onSelect.bind(this)}
                    defaultValue={selected}
                    multi
                    value={selected} options={this.props.instruments.map(r => {
                    return this.instrToOpt(r);
                })}/>

                <Chart {...this.props.chartData}/>
            </div>
        )
    }

    instrToOpt(r : string) {
        return {
            label: r,
            value: r
        }
    }
}
