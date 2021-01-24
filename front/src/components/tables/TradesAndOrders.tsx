import React, {useState} from "react";
import {OrderComp} from "./OrdersTable";
import {TradesTable} from "./TradesTable";
import {Trade} from "../../api/models";
import {DisplayTrade} from "../candlestick/DisplayTrade";
import {Col, Row} from "antd";

export const TradesAndOrders = (props: { trades: Array<Trade> }) => {
    const {trades} = props
    const [tradeId, setTradeId] = useState('')



    return <div style={{height : '700px'}}>
        <TradesTable trades={trades} onRow={(t) => {
            setTradeId(t.tradeId)
        }}/>
        <DisplayTrade tradeId={tradeId}/>
        </div>

        {/*<OrderComp tradeId={tradeId}/>*/}
}