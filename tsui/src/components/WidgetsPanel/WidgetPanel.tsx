import * as React from 'react';
import {Component} from 'react';
import './App.css';

import {Button, Col, Row} from 'antd';

import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";


import 'antd/dist/antd.css';
import {connect} from "react-redux";
import {MainStore} from "../types";
import WContainer from "./WidgetComponentContainer";
import {Action, makeAmend} from "../../actions";
import {fetchInstruments} from "../../repository";
import {WidgetData} from "./WidgetComponent";
import {RouteComponentProps} from "react-router";


// import type {ChartData, ChartMeta, MainStore} from "./types";


interface AppState {
    selected: string,
    opts: Array<{ label: string, value: string }>,
    // chartData: ChartData

}

interface AppCallBack {
    onAdd: () => void,
    onFetchInstr: () => void,
}

function chunk_array<T>(arr : Array<T>, chunk : number) : Array<Array<T>>{
    const ret = new Array<Array<T>>();
    arr.forEach((val : T, idx : number)=>{
        if(idx % chunk == 0){
            ret.push([])
        }
        ret[ret.length - 1].push(val)
    });
    return ret
}


class WidgetPanel extends Component<MainStore & AppCallBack & RouteComponentProps<any>, AppState> {

    componentDidMount() {
        this.props.onFetchInstr()
    }

    displayMetas(){
    //    this.props.match
        return chunk_array(this.props.widgets, 2).map(row=>{
            return <Row>
                {row.map(meta=>{
                    return <Col span={8}>
                            <WContainer {...meta} />
                        </Col>
                })}
            </Row>
        })
    }

    render() {
        console.log('store', this.props);
        return (
            <div>
                <Button onClick={this.props.onAdd}>Add</Button>
                {this.displayMetas()}
            </div>
        );
    }
}

var ID = function () {
    return '_' + Math.random().toString(36).substr(2, 9);
};

const dispatchToProps = (dispatch : (a : Action)=>void): AppCallBack => {
    return {
        onAdd: () => {
            let amendAction = makeAmend((state: MainStore) => {
                const arr : Array<WidgetData> = Array.from(state.widgets);
                arr.push({
                    id : ID(),
                    period : 10,
                    chartData : new Map(),
                    selectedInstruments : []
                })
                return {...state, widgets: arr}
            });

            dispatch(amendAction)
        },
        onFetchInstr: () => {
            //const val = new Map<string, Array<TimePointTr>>()
            //
            fetchInstruments().then(inst => {
                console.log('inst',inst);
                dispatch(makeAmend((state: MainStore) => {
                    return {...state, instruments: inst}
                }))
            }).catch(er => {
                console.log('err', er)
            })
        }

    }

};

export default connect(l => l, dispatchToProps)(WidgetPanel);
