
import {Component} from 'react';
import Select from "react-virtualized-select";
import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";

import * as React from "react";

// import './static/style.css'
import Chart from "../Chart/Chart";
import {TimePointTr} from "../../../build/lib/src/repository";



export interface WidgetData {
    period: number,
    id: string,
    chartData : Map<string, Array<TimePointTr>>,
    instruments: Array<string>
}



export interface WidgetCallbacks {
    onChange: (metaId: string, codes: Array<string>) => void
}


export class WidgetComponent extends Component<WidgetData & WidgetCallbacks, any> {

    onSelect(opts: Array<{name : string,value : string}>) {
        console.log("on select",opts);
        this.props.onChange(this.props.id, opts.map(o=>o.value))
    }


    render() {
        const selected = this.props.instruments;
        return (
            <div className="widget">
                <Select
                    onChange={this.onSelect.bind(this)}
                    multi
                    value={selected} options={this.props.instruments.map(r => {
                    return this.instrToOpt(r);
                })}/>

                <Chart  series={this.props.chartData}/>
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
