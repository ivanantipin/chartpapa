import React, {useEffect, useMemo, useState} from 'react';
import 'antd/dist/antd.css';

import {Table} from 'antd';
import {Order} from "../../api/models";
import {ColumnsType} from 'antd/es/table';
import {IMainState} from "../../reducers/reducers";
import {useMappedState} from "redux-react-hook";
import {getRenderByValue} from "./utils";


export const OrderComp = (props: { tradeId: string }) => {


    let allOrders = useMappedState((state: IMainState) => {
        return state.orders
    });

    const orders = useMemo(() => {
        return allOrders.filter(it => it.tradeId === props.tradeId)
    }, [props.tradeId])

    return <OrdersTable orders={orders}/>
}


export const OrdersTable = (props: { orders: Array<Order> }) => {
    const {orders} = props;
    const [columns, setColumns] = useState<ColumnsType<Order>>()
    useEffect(() => {
        if (orders === undefined || orders.length === 0) {
            setColumns([])
            return
        }
        const sample = orders[0]
        const newCols: ColumnsType<Order> = []

        Object.keys(sample).map(s => {
            newCols.push({
                title: s,
                dataIndex: s,
                render: getRenderByValue(s)
            })
        })
        setColumns(newCols)
    }, [orders])

    return <Table<Order> columns={columns}
                         dataSource={orders}
                         pagination={
                             {
                                 pageSize: 50
                             }
                         }
                         scroll={
                             {
                                 y: 240
                             }
                         }
                         size="small"/>
}