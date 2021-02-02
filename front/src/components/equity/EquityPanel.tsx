import * as React from 'react';
import {useState} from 'react';
import {calcEquity} from "../../services/tradeUtils";
import {DisplaySettings, DisplaySettingsComp} from "./DisplaySettingsComp";
import {Trade} from "../../api";
import Boost from "highcharts/modules/boost";
import Highcharts, {SeriesOptionsType} from "highcharts";
import HighchartsReact from "highcharts-react-official";

let _ = require('lodash');

Boost(Highcharts);

export const EquityPanel = (props: { trades: Array<Trade> }) => {
    const {trades} = props

    const [displaySettings, setDisplaySettings] = useState<DisplaySettings>({xAsTradeNo: false, splitBySide: false})

    return <div style={{paddingRight : "20px"}}>
        <DisplaySettingsComp displaySettings={displaySettings} onChange={(it) => {
            setDisplaySettings(it)
        }}/>
        <HighchartsReact highcharts={Highcharts} options={formOpts(trades, displaySettings)}/>
    </div>
};


export const formOpts = (trades: Array<Trade>, displaySettings: DisplaySettings): Highcharts.Options => {

    const dict: { [key: string]: Array<Trade> } = displaySettings.splitBySide ? _.groupBy(trades, (it: Trade) => it.side) : {'total': trades}

    const type = displaySettings.xAsTradeNo ? 'linear' : 'datetime'

    return {
        title: {
            text: 'Equity'
        },
        xAxis: {
            type: type
        },

        chart: {
            zoomType: 'x',
            marginRight : 100
        },
        series: Object.entries(dict).map(value => {
            let eq = calcEquity(value[1], displaySettings.xAsTradeNo)
            return getSeriesOpts(value[0], eq)
        })
    }
}


export const getSeriesOpts = (label: string, data: Array<[number, number]>): SeriesOptionsType => {
    return {
        type: 'line',
        name: label,
        //borderWidth: 1,
        data: data,
        boostThreshold: 10000
    }
}