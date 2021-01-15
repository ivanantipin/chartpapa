import React, {useState} from "react";
import {OrderComp} from "./OrdersTable";
import {TradesTable} from "./TradesTable";
import {Trade} from "../../api/models";

export const TradesAndOrders = (props: { trades: Array<Trade> }) => {
    const {trades} = props
    const [tradeId, setTradeId] = useState('')


    return <>
        <TradesTable trades={trades} onRow={(t) => {
            setTradeId(t.tradeId)
        }}/>
        <OrderComp tradeId={tradeId}/>
    </>
}