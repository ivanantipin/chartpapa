import React from 'react';
import 'antd/dist/antd.css';

import {Empty, Table} from 'antd';
import {DateStat, groupByDate} from "../../services/tradeUtils";
import {ColumnType} from "antd/es/table";
import {Order, Trade} from "../../api/models";
import {getRenderByValue} from "./utils";


export const DateStatTable = (props: { trades: Array<Trade>, orders: Array<Order> }) => {
    const {trades, orders} = props;

    const stat = groupByDate(trades, orders).filter(it => it.newOpenPositions !== 0 || it.closedPositions !== 0)

    if (stat === undefined || stat.length === 0) {
        return <Empty/>
    }
    const columns: ColumnType<DateStat>[] = []
    Object.keys(stat[0]).map(p => {
        columns.push({
            title: p.charAt(0).toUpperCase() + p.slice(1),
            dataIndex: p,
            key: p,
            render: getRenderByValue(p)
        })
    })

    return <Table<DateStat>
        columns={columns} dataSource={stat}
        pagination={{pageSize: 150}} scroll={{y: 440}} size="small"
    />
}


