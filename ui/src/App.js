// @flow
import React, {Component} from 'react';
import './App.css';

import {Button, Col, Row} from 'antd';

import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";


import 'antd/dist/antd.css';
import type {ChartData, ChartMeta, MainStore} from "./types";
import EditMetaComp from "./components/WidgetComponentContainer";
import {connect} from "react-redux";
import {makeAmend} from "./actions";
import {fetchInstruments} from "./repository";

export type AppState = {
    selected: string,
    opts: Array<{ label: string, value: string }>,
    chartData: ?ChartData

}

export type AppCallBack = {
    onAdd: () => void,
    onFetchInstr: () => void,
}

function chunk_array<T>(arr : Array<T>, chunk : number) : Array<Array<T>>{
    const ret = [];
    arr.forEach((val : T, idx : number)=>{
        if(idx % chunk == 0){
            ret.push([])
        }
        ret[ret.length - 1].push(val)
    });
    return ret
}


class App extends Component<MainStore & AppCallBack, AppState> {

    componentDidMount() {
        console.log('');
        this.props.onFetchInstr()
    }

    displayMetas(){
        return chunk_array(this.props.widgets, 2).map(row=>{
            return <Row>
                {row.map(meta=>{
                    return <Col span={8}>
                            <EditMetaComp chartMeta={meta}/>
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
    // Math.random should be unique because of its seeding algorithm.
    // Convert it to base 36 (numbers + letters), and grab the first 9 characters
    // after the decimal.
    return '_' + Math.random().toString(36).substr(2, 9);
};

const dispatchToProps = (dispatch): AppCallBack => {
    return {
        onAdd: () => {
            dispatch(makeAmend((state: MainStore) => {
                const arr : Array<ChartMeta> = Array.from(state.widgets);
                arr.push({
                    codes: [],
                    period: 1,
                    id : ID()
                });
                return {...state, widgets: arr}
            }))
        },
        onFetchInstr: () => {
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

export default connect(l => l, dispatchToProps)(App);
