import * as React from 'react';
import {Component} from 'react';
import Select from "react-virtualized-select";
import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";
// import './static/style.css'
import {MainStore} from "../types";
import {fetchOhlcChart} from "../../repository";
import HOhlcChart from "../OhlcChart/HOhlcChart";
import {CandleStickChartProps} from "../OhlcChart/OhlcChart";
import {connect} from "react-redux";
import {Spin, Row, Col} from "antd";
import {InstrId} from "../../api";


export interface Opt extends InstrId {
    label : string,
    value : string
}

class SeqPanel extends Component<MainStore, { inst?: Opt, loading: boolean, chart?: CandleStickChartProps, timeframe : string }> {

    constructor(props: MainStore) {
        super(props)
        this.state = {loading: false, timeframe : 'Day'}
    }

    onSelect(sel: Opt) {
        if (!sel) {
            return
        }

        this.setState({...this.state, loading: true})
        fetchOhlcChart(sel, this.state.timeframe).then(ch => {
            this.setState({...this.state, inst: sel, loading: false, chart: ch})
        }).catch(e => {
            console.log('err', e)
        })
        console.log('state', this.state)
    }

    onTimeFrameChange(sel: {label : string, value : string}) {
        if (!sel) {
            return
        }
        if(!this.state.inst){
            return
        }
        this.setState({...this.state, loading: true})
        fetchOhlcChart(this.state.inst, sel.value).then(ch => {
            this.setState({...this.state, loading: false, chart: ch, timeframe : sel.value})
        }).catch(e => {
            console.log('err', e)
        })
        console.log('state', this.state)
    }


    getSome() {
        if (this.state.loading) {
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
                <Row>
                    <Col span={4}>
                <Select style={{width: 150}}
                        onChange={this.onSelect.bind(this)}
                    // @ts-ignore
                        value={this.state.inst}
                        options={this.props.instruments.map(r => {
                            return SeqPanel.instrToOpt(r);
                        })}/>
                    </Col>
                    <Col span={4}>
                <Select style={{width: 150}}
                        onChange={this.onTimeFrameChange.bind(this)}
                    // @ts-ignore
                        value={{label: this.state.timeframe, value : this.state.timeframe}}
                        options={[
                            {label : 'Day',value : 'Day'},
                            {label : 'Week',value : 'Week'},
                            {label : 'Min240',value : 'Min240'},
                        ]}/>
                    </Col>
                </Row>

                {
                    this.getSome.bind(this)()
                }


            </div>
        )
    }

    static instrToOpt(r: InstrId) : Opt{
        return {...r,
            label: r.name + `(${r.source})`,
            value: r.code
        }
    }
}


export default connect<MainStore, any>((l: MainStore) => l)(SeqPanel)

