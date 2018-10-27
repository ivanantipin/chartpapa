import * as React from 'react';
import {Component} from 'react';
import Select from "react-virtualized-select";
import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";
// import './static/style.css'
import {MainStore} from "../types";
import {fetchOhlcChart, InstrId} from "../../repository";
import HOhlcChart from "../OhlcChart/HOhlcChart";
import {CandleStickChartProps} from "../OhlcChart/OhlcChart";
import {connect} from "react-redux";
import {Spin} from "antd";


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
        fetchOhlcChart(sel.value).then(ch => {
            this.setState({inst: sel.value, loading: false, chart: ch})
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
            label: r.name,
            value: r.code
        }
    }
}


export default connect<MainStore, any>((l: MainStore) => l)(SeqPanel)

