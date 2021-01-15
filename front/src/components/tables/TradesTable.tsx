import React, {useEffect, useState} from 'react';
import 'antd/dist/antd.css';

import {Table} from 'antd';
import {Trade} from "../../api/models";
import {ColumnsType} from "antd/es/table";
import {getRenderByValue} from "./utils";


export const TradesTable = (props: { trades: Array<Trade>, onRow: (t: Trade) => void }) => {
    const {trades} = props;

    const [tradeId, setTradeId] = useState('')
    const [columns, setColumns] = useState<ColumnsType<Trade>>()

    useEffect(() => {
        if (trades === undefined || trades.length === 0) {
            setColumns([])
            return
        }
        const sample = trades[0]
        const newCols: ColumnsType<Trade> = []

        Object.keys(sample).map(s => {
            newCols.push({
                title: s,
                dataIndex: s,
                render: getRenderByValue(s)
            })
        })
        setColumns(newCols)
    }, [trades])


    const getRowClassName = (record: Trade) => {
        return record.tradeId === tradeId ? 'clickRowStyl' : '';
    };

    return <Table<Trade>
        columns={columns} dataSource={trades}
        pagination={{pageSize: 50}} scroll={{y: 240}} size="small"
        rowClassName={getRowClassName}

        onRow={(record, index) => ({
            onClick: (event) => {
                setTradeId(record.tradeId)
                props.onRow(record)
            }
        })}
    />
}


