import React, {useEffect, useMemo, useState} from 'react';

import Highcharts from "highcharts/highstock";
import HighchartsReact from "highcharts-react-official";
import {Trade, Side} from "../../api/models";
import {candlesApi} from "../../services/api/services";
// @ts-ignore
import {CandlesApi} from "../../api/apis";
import {Configuration} from "../../api";
import {AnnotationsShapesOptions} from "highcharts";
import moment from "moment-timezone";

require('highcharts/modules/annotations')(Highcharts)

const options = {
    rangeSelector: {
        selected: 1
    },

    title: {
        text: "AAPL Historical"
    },

    yAxis: [
        {
            labels: {
                align: "right",
                x: -3
            },
            title: {
                text: "OHLC"
            },
            height: "100%",
            lineWidth: 2,
            resize: {
                enabled: true
            }
        }
    ],

    tooltip: {
        split: true
    },
    chart: {
        zoomType: 'x'
    },
    series: [
        {
            type: "ohlc",
            name: "AAPL",
            marker: {
                enabled: false
            }
        }
    ],
    annotations: [] as Array<any>
};



export const fetchTicker = (ticker: string, minTs: number, maxTs: number): Promise<Array<[number, number, number, number, number]>> => {
    return candlesApi.candlesRead({symbol: ticker, timeframe: 'Min10', fromTs: minTs, toTs: maxTs}).then(it => {
        return it.map(candle => {
            return [candle.datetime, candle.open, candle.high, candle.low, candle.close]
        })
    })
}


const makeShape = (trade: Trade): AnnotationsShapesOptions => {
    return {
        fill: 'none',
        stroke: trade.side === Side.Sell ? 'red' : 'green',
        strokeWidth: 3,
        dashStyle: 'Dot',
        type: 'path',
        points: [{
            x: trade.openTime,
            y: trade.openPrice,
            xAxis: 0,
            yAxis: 0
        },
            {
                x: trade.closeTime,
                y: trade.closePrice,
                xAxis: 0,
                yAxis: 0
            }],
        markerEnd: 'arrow',
        markerStart: 'circle'
    }
}

export const CandlestickHigh = (props: { trades: Array<Trade>, ticker: string }) => {

    const [ohlc, setOhlc] = useState([] as Array<[number, number, number, number, number]>)

    const [minTs, maxTs] = useMemo(() => {

        let minTs = props.trades[0]?.openTime || 0
        let maxTs = props.trades[props.trades.length - 1]?.closeTime || 0

        props.trades.forEach((trd) => {
            maxTs = Math.max(trd.closeTime, maxTs)
            minTs = Math.min(trd.openTime, minTs)
        })

        return [minTs, maxTs]

    }, [props.trades])


    if (ohlc && ohlc.length > 0 && props.trades && props.trades.length > 0) {

        console.log('first', moment(props.trades[0].openTime))
        console.log('last', moment(props.trades[props.trades.length - 1].closeTime))

        console.log('first oh', moment(ohlc[0][0]))
        console.log('last oh', moment(ohlc[ohlc.length - 1][0]))
    }


    useEffect(() => {
        console.log("requesting", moment(minTs))
        fetchTicker(props.ticker, minTs, maxTs).then(dt => {
            setOhlc(dt)
        })
    }, [props.ticker]);

    const ser = {
        type: "ohlc",
        name: props.ticker,
        marker: {
            enabled: false
        },
        data: ohlc
    }

    const opts = {
        ...options,

        title: {
            text: props.ticker
        },
        series: [ser], annotations: [
            {shapes: props.trades.map(trd => makeShape(trd))}
        ]
    };

    return (
        <HighchartsReact
            highcharts={Highcharts}
            constructorType={"stockChart"}
            options={opts}/>
    );
}