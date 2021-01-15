import * as React from 'react';
import {useEffect, useState} from 'react';
import {createChart, UTCTimestamp} from 'lightweight-charts';

import {Candle, Trade, Side, OrderStatus} from "../../api/models";
import {candlesApi} from "../../services/api/services";


const getChartOrderMarks = (orders: Array<any>): Array<any> => {
    if (orders === undefined || orders.length === 0) {
        return []
    }
    let ordersMarks: Array<any> = [];
    orders.map(order => {
        const position = order.side === Side.Buy ? 'belowBar' : 'aboveBar';
        const shape = order.status === OrderStatus.Filled ? 'square' : 'circle';
        const color = order.side === Side.Buy ? 'green' : 'red';

        ordersMarks.push({
            time: order.place_time,
            position: position,
            color: color,
            shape: shape,
            id: order.id,
            text: `${order.status}. Price: ${order.price} Execution: ${order.execution_price} Qty: ${order.qty}`
        })
    });

    return ordersMarks
};

const getChartTradesMarks = (trades: Array<Trade>): Array<any> => {
    if (trades === undefined || trades.length === 0) {
        return []
    }

    let tradesMarks: Array<any> = [];
    trades.map(trade => {
        const positionEntry = trade.side === Side.Buy ? 'belowBar' : 'aboveBar';
        const positionExit = trade.side !== Side.Buy ? 'belowBar' : 'aboveBar';
        const shapeEntry = trade.side === Side.Buy ? 'arrowUp' : 'arrowDown';
        const shapeExit = trade.side !== Side.Buy ? 'arrowUp' : 'arrowDown';
        const color = trade.side === Side.Buy ? 'green' : 'red';

        // Open marker
        tradesMarks.push({
            time: trade.openTime / 1000,
            position: positionEntry,
            color: color,
            shape: shapeEntry,
            id: `Entry-${trade.tradeId}`,
            text: `Open ${trade.side} Entry: ${trade.openPrice}  Qty: ${trade.qty}`
        });
        // Close marker
        tradesMarks.push({
            time: trade.closeTime / 1000,
            position: positionExit,
            color: 'black',
            shape: shapeExit,
            id: `Exit-${trade.tradeId}`,
            text: `Close ${trade.tradeId}  Exit: ${trade.closePrice}  PnL: ${trade.pnl}`
        })
    });

    return tradesMarks
};


export const CandlePage = (props: { ticker: string, trades: Array<Trade> }) => {

    const [candles, setCandles] = useState<Array<Candle>>([])

    useEffect(() => {
        candlesApi.candlesRead({symbol: 'SPY', timeframe: 'DAY', fromTs : -1, toTs : -1}).then((it) => {
            setCandles(it)
            return it
        })
    }, [props.ticker])

    return <Candlestick data={candles} trades={props.trades} height={500} width={1600} orders={[]}/>
}

const Candlestick = (props: {
    height: number,
    width: number
    data: Array<Candle>,
    trades: Array<Trade>
    orders: any
    title?: string
}) => {

    const containerId = 'lightweight_chart_container';

    useEffect(() => {
        const chart = createChart(containerId, {width: props.width, height: props.height});

        const barSeries = chart.addCandlestickSeries({});
        // set the data
        barSeries.setData(props.data.map(candle => {
            return {...candle, time: candle.datetime / 1000 as UTCTimestamp}
        }));
        barSeries.setMarkers([...getChartTradesMarks(props.trades), ...getChartOrderMarks(props.orders)]);


        chart.applyOptions({
            watermark: {
                color: 'rgba(11, 94, 29, 0.4)',
                visible: true,
                text: props.title,
                fontSize: 24,
                horzAlign: 'left',
                vertAlign: 'bottom',
            },
        })

        return function () {
            chart.remove();
        }
    })

    return (
        <div
            id={containerId}
            className={'LightweightChart'}
        />
    );
}