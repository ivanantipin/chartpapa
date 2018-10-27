import * as React from 'react';
import {Component} from 'react';
import Select from "react-virtualized-select";
import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";
// import './static/style.css'
import {MainStore} from "../types";
import {InstrId} from "../../repository";
import HOhlcChart from "../OhlcChart/HOhlcChart";
import {parseDate} from "../../dtutils";
import {CandleStickChartProps, OhlcTr} from "../OhlcChart/OhlcChart";
import {MainControllerApi} from "../../api";
import {Label, Ohlc} from "../../api/api";
import {connect} from "react-redux";
import {Spin} from "antd";


const fetchChart = (code: string): Promise<CandleStickChartProps> => {
    let api = new MainControllerApi();
    return Promise.all([api.getOhlcsUsingGET(code), api.getAnnotationsUsingGET(code)]).then(arr => {
        let ohlcs = arr[0].map((oh: Ohlc): OhlcTr => {
            return {
                date: parseDate(oh.dateTime),
                open: oh.open,
                high: oh.high,
                low: oh.low,
                close: oh.close,
            }
        });

        const lb: Map<number, Label> = new Map<number, Label>();

        console.log('ann', arr[1])

        arr[1].labels.forEach((ll: Label) => {
            lb.set(parseDate(ll.time).getTime(), ll)
        })
        return {
            name: code,
            data: ohlcs,
            tsToLabel: lb,
            hlines: arr[1].lines
        }
    })
}


class SeqPanel extends Component<MainStore, { inst?: string, loading: boolean, chart?: CandleStickChartProps }> {

    constructor(props: MainStore) {
        super(props)
        this.state = {loading: false}
    }

    onSelect(sel: { label: string, value: string }) {
        if (!sel) {
            return
        }
        this.setState({...this.state, loading: true})
        fetchChart(sel.label).then(ch => {
            this.setState({inst: sel.label, loading: false, chart: ch})
        }).catch(e => {
            console.log('err', e)
        })
        console.log('state', this.state)
    }

    getSome() {
        if (this.state.loading) {
            console.log('spinning')
            return <Spin style={{top: '50%', left: '50%', position: 'absolute'}} size='large'/>
        }
        if (this.state.chart == null) {
            return <div/>
        } else {
            return <HOhlcChart {...this.state.chart}/>
        }
    }



    render() {

        return (
            <div className="widget">
                <Select style={{width: 150}}
                        onChange={this.onSelect.bind(this)}
                        value={{value: this.state.inst, label: this.state.inst}}
                        options={this.props.instruments.map(r => {
                            return SeqPanel.instrToOpt(r);
                        })}/>
                {
                    this.getSome.bind(this)()
                }

            </div>
        )
    }

    static instrToOpt(r: InstrId) {
        return {
            label: r.code,
            value: r.code
        }
    }
}


export default connect<MainStore, any>((l: MainStore) => l)(SeqPanel)

