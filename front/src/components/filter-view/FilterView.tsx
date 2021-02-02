import React from "react"
import {Trade} from "../../api/models";
import {Col, Row, Tabs} from 'antd'
import {EquityPanel} from "../equity/EquityPanel";
import {TradesAndOrders} from "../tables/TradesAndOrders";
import {DateStatTable} from "../tables/ByDateResults";
import {CandlestickHigh} from "../candlestick/CandlestickHigh";
import {StatsPanel} from "../equity/StatsPanel";

const {TabPane} = Tabs


export const FilterView = (props: { trades: Array<Trade>, hasCandlestick?: boolean }) => {
    const {trades, hasCandlestick} = props

    if (trades === undefined || trades.length === 0) {
        return null
    }
    const candlestick = hasCandlestick && (
        <TabPane tab="Candlestick" key="candlestick">
            <CandlestickHigh ticker={trades[0].symbol} trades={trades}/>
        </TabPane>
    )
    return (
        <Tabs defaultActiveKey="equity">
            <TabPane tab="Equity" key="equity">
                <Row>
                    <Col span={22}>
                        <EquityPanel trades={trades}/>
                    </Col>
                </Row>
            </TabPane>
            <TabPane tab="Stats" key="stats">
                <Row>
                    <Col span={24}>
                        <StatsPanel trades={trades}/>
                    </Col>
                </Row>
            </TabPane>
            <TabPane tab="Trades&Orders" key="trades&orders">
                <TradesAndOrders trades={trades}/>
            </TabPane>
            <TabPane tab="By Date" key="byDate">
                <DateStatTable trades={trades} orders={[]}/>
            </TabPane>
            {candlestick}
        </Tabs>
    )
}