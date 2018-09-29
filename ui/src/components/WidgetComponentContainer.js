// @flow

import React from 'react';
import type {MainStore} from "../types";
import {connect} from "react-redux";

import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";
import {makeAmend} from "../actions";
import type {ChartData, WidgetCallbacks} from "./WidgetComponent";
import {WidgetComponent} from "./WidgetComponent";
import {fetchChart} from "../repository";

const dispatchCallbacks = (dispatch) : WidgetCallbacks=>{
    return {
        onChange : (metaId : string, codes : Array<string>)=>{
            fetchChart(codes).then(ms=>{
                dispatch(makeAmend((store : MainStore)=>{
                    const nmetas = store.widgets.map(meta=>{
                        if(meta.id == metaId){
                            const chartData : ChartData = {
                                index : ms.index,
                                chart : ms.closes
                            };
                            return {...meta, chartData : chartData,  codes : codes}
                        }else{
                            return meta
                        }
                    });

                    return {...store, widgets : nmetas}
                }))
            })
        },
    }
};

const WContainer = connect(l=>l, dispatchCallbacks)(WidgetComponent)


export default WContainer