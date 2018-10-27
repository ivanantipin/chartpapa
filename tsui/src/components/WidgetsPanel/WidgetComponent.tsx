import * as React from 'react';
import {Component} from 'react';
import Select from "react-virtualized-select";
import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";
// import './static/style.css'
import {MainStore} from "../types";
import {InstrId, TimePointTr} from "../../repository";
import HChart from "../HChart/HChart";


export interface WidgetData {
    period: number,
    id: string,
    chartData : Map<string, Array<TimePointTr>>,
    selectedInstruments: Array<string>
}



export interface WidgetCallbacks {
    onChange: (metaId: string, codes: Array<string>) => void
}


export class WidgetComponent extends Component<WidgetData & WidgetCallbacks & MainStore, any> {

    onSelect(opts: Array<{name : string,value : string}>) {
        console.log("on select",opts);
        this.props.onChange(this.props.id, opts.map(o=>o.value))
    }


    render() {
        console.log('selected', this.props.selectedInstruments)
        return (
            <div className="widget">
                <div>Panel</div>

                <Select
                    onChange={this.onSelect.bind(this)}
                    multi
                    value={this.props.selectedInstruments.map(si=>{
                        return {
                            label : si,
                            value : si
                        }
                    })} options={this.props.instruments.map(r => {
                    return this.instrToOpt(r);
                })}/>

                <HChart  series={this.props.chartData}/>
            </div>
        )
    }

    instrToOpt(r : InstrId) {
        return {
            label: r.code,
            value: r.code
        }
    }
}
