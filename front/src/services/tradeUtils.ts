import {all, create} from 'mathjs'
import moment from "moment-timezone";
import {QuantinizeFunction} from "./quantizationUtils";
import {Order, Side, Trade} from "../api"

const math = create(all, {})!
let _ = require('lodash');


export class EquityAndStats {
    equity: Array<[number, number]>
    title: string

    constructor(equity: Array<[number, number]>, title: string) {
        this.equity = equity;
        this.title = title;
    }
}

export const groupBy = function <T>(trades: Array<T>, extr: (tr: T) => string): { [index: string]: Array<T> } {
    return _.groupBy(trades, extr);
};


export const calcStats = (trades: Array<Trade>): Stats => {
    if (trades.length == 0) {
        return {
            avgPnl: 0,
            avg_percent: 0,
            biggest_trade: 0,
            cnt: 0,
            drawdown: 0,
            drawdown_percent: 0,
            lowest_trade: 0,
            pf: 0,
            pnl: 0,
            recovery_factor: 0,
            run_up: 0,
            sharpe: 0,
            sortino: 0,
            std: 0,
            median: 0,
            holdHoursMean: 0
        }
    }

    let run_up = 0
    let max_drawdown = 1000_000_000
    let max_drawdown_percent = 1000_000_000

    let last_point = 0
    let last_point_long = 0
    let last_point_short = 0

    let win_sum = 0
    let loss_sum = 0
    let biggest_trade = -1000_000_000
    let lowest_trade = 1000_000_000

    let pnls: Array<number> = trades.map(it => it.pnl);


    pnls.forEach((pnl, idx) => {
        lowest_trade = math.min!(lowest_trade, pnl)
        biggest_trade = math.max!(biggest_trade, pnl)
        if (pnl > 0) {
            win_sum += pnl
        } else {
            loss_sum += pnl
        }

        if (trades[idx].side === Side.Buy) {
            last_point_long += pnl
        } else {
            last_point_short += pnl
        }

        last_point += pnl

        if (last_point > run_up) {
            run_up = last_point
        }

        let drawdown = last_point - run_up

        if (drawdown < max_drawdown) {
            max_drawdown = drawdown

            let percent_drawdown = run_up == 0 ? 0 : (last_point - run_up) * 100 / run_up

            if (percent_drawdown < max_drawdown_percent) {
                max_drawdown_percent = percent_drawdown
            }
        }
    })

    const mean_trade = trades.length > 0 ? (loss_sum + win_sum) / trades.length : 0

    const std = math.std!(pnls, 'unbiased')

    const median = math.median!(pnls)

    let shape = mean_trade / std

    return {
        cnt: trades.length,
        std: std,
        pnl: win_sum + loss_sum,
        run_up: run_up,
        drawdown: max_drawdown,
        drawdown_percent: max_drawdown_percent,
        sharpe: shape,
        pf: loss_sum == 0 ? 0 : math.abs!(win_sum / loss_sum),
        sortino: 0,
        avgPnl: mean_trade,
        avg_percent: 0,
        recovery_factor: 0,
        biggest_trade: biggest_trade,
        lowest_trade: lowest_trade,
        median: median,
        holdHoursMean: math.mean!(trades.map(it => (it.closeTime - it.openTime) / 1000 / 3600))
    }
}

export interface Stats {
    cnt: number
    std: number
    pnl: number
    run_up: number
    drawdown: number
    drawdown_percent: number
    sharpe: number
    pf: number
    sortino: number
    avgPnl: number
    avg_percent: number
    recovery_factor: number
    biggest_trade: number
    lowest_trade: number
    median: number,
    holdHoursMean: number
}

export function calcEquity(trades: Array<Trade>, byIndex: boolean): Array<[number, number]> {
    let currentPnl = 0
    return trades.map((trd, idx) => {
        currentPnl += trd.pnl
        return [byIndex ? idx : trd.openTime, currentPnl]
    });
}

export function chunk<T>(arr: Array<T>, chunk: number): Array<Array<T>> {
    var i, j;
    const ret = []
    for (i = 0, j = arr.length; i < j; i += chunk) {
        ret.push(arr.slice(i, i + chunk))
    }
    return ret
}

function getGroupedOrdersInfoForDate(groupedOrders: { [key: string]: Array<Order> }, dt: string): groupedOrdersInfo {
    let count: number = 0
    let exposure: number = 0
    const ordersArr = groupedOrders[dt]
    if (ordersArr !== undefined) {
        count = ordersArr.length
        ordersArr.map(o => {
            exposure += o.price! * o.qty
        })
    }

    return {count, exposure}
}

interface groupedOrdersInfo {
    count: number
    exposure: number
}

export const groupByDate = function (trades: Array<Trade>, orders: Array<Order>): Array<DateStat> {

    const atStart = (ts: number): string => {
        return moment(ts).format('YYYY-MM-DD')
    }

    const groupedByCloseTime = _.groupBy(trades, (e: Trade) => {
        return atStart(e.closeTime)
    })

    const groupedByOpenTime = _.groupBy(trades, (e: Trade) => {
        return atStart(e.openTime)
    })


    const ordersByStatus = _.groupBy(orders, (e: Order) => {
        return e.status.toLowerCase()
    })
    const allOrdersByPlaceDate = _.groupBy(orders, (e: Order) => {
        return atStart(e.placeTime)
    })

    const filledOrdersByUpdateDate = _.groupBy(ordersByStatus.filled, (e: Order) => {
        return atStart(e.updateTime)
    })
    const canceledOrdersByUpdateDate = _.groupBy(ordersByStatus.canceled, (e: Order) => {
        return atStart(e.updateTime)
    })

    const closeDates = Object.keys(groupedByCloseTime)
    const openDates = Object.keys(groupedByOpenTime)

    const minDate = new Date(Math.min(...openDates.map(e => {
        return new Date(e).getTime()
    })))

    const maxDate = new Date(Math.max(...closeDates.map(e => {
        return new Date(e).getTime()
    })))

    let curDate = minDate

    let totalOpenPositions: number = 0
    let activeOrders: number = 0
    let positionExposure: number = 0
    let eodOrdersExposure: number = 0
    let equity: number = 0

    let byDate: Array<DateStat> = []

    while (curDate <= maxDate) {
        const dt: string = moment(curDate).format('YYYY-MM-DD')
        let newOpenPositions: number = 0
        let closedPnL: number = 0
        let closedPositions: number = 0


        const openPositionsArr = groupedByOpenTime[dt]
        openPositionsArr?.forEach((p: Trade) => {
            newOpenPositions += 1
            positionExposure += p.openPrice * p.qty
        })


        const closedPositionsArr = groupedByCloseTime[dt]
        closedPositionsArr?.forEach((p: Trade) => {
            positionExposure -= Math.round(p.openPrice * p.qty)
            closedPnL += p.pnl
            closedPositions += 1
        })


        const executedOrdersInfo = getGroupedOrdersInfoForDate(filledOrdersByUpdateDate, dt)
        const canceledOrdersInfo = getGroupedOrdersInfoForDate(canceledOrdersByUpdateDate, dt)
        const placedOrdersInfo = getGroupedOrdersInfoForDate(allOrdersByPlaceDate, dt)

        totalOpenPositions += newOpenPositions - closedPositions
        equity += closedPnL
        eodOrdersExposure += placedOrdersInfo.exposure - canceledOrdersInfo.exposure - executedOrdersInfo.exposure
        activeOrders += placedOrdersInfo.count - canceledOrdersInfo.count - executedOrdersInfo.count

        const date = new Date(dt)
        byDate.push({
            date,
            equity,
            totalOpenPositions,
            newOpenPositions,
            closedPositions,
            positionExposure,
            eodOrdersExposure,
            totalExposure: eodOrdersExposure + positionExposure,
            placedOrders: placedOrdersInfo.count,
            activeOrders,
            executedOrders: executedOrdersInfo.count,
            canceledOrders: executedOrdersInfo.count,
            closedPnL,
        })

        curDate.setDate(curDate.getDate() + 1);
    }


    return byDate
};


export interface DateStat {
    date: Date
    equity: number
    totalOpenPositions: number
    newOpenPositions: number
    closedPositions: number
    positionExposure: number
    eodOrdersExposure: number
    totalExposure: number
    placedOrders: number
    activeOrders: number
    executedOrders: number
    canceledOrders: number
    closedPnL: number
}

export interface DiscreteAggregationTuple {
    field: string
    value: any
}

export interface ContAggregationTuple extends DiscreteAggregationTuple{
    func: QuantinizeFunction
}

export interface AggregationConditions {
    discreteTags: Array<DiscreteAggregationTuple>
    continuousTags: Array<ContAggregationTuple>
    discreteMeta: Array<DiscreteAggregationTuple>
    continuousMeta: Array<ContAggregationTuple>
    tradeCond: Array<DiscreteAggregationTuple>
}

export interface AggregationResult extends Stats {
    conditions: AggregationConditions
}





