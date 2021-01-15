import React, {useEffect, useState} from "react"
import {AggregationResult, Stats} from "../../services/tradeUtils";
import {Collapse, Empty, Table} from "antd";

import {ColumnsType} from "antd/es/table";
import {getRenderByValue} from "./utils";
import 'antd/dist/antd.css';
import './style.css'
import {CompareFn} from "antd/es/table/interface";

const {Panel} = Collapse;


const getSorter = (fld: keyof Stats): { compare: CompareFn<Stats>, multiple: number } => {
    return {
        compare: (a: Stats, b: Stats) => a[fld] - b[fld],
        multiple: 1
    }
};


function constructTitle(selectedResult: any) {
    let title = selectedResult?.conditions ? "Results for conditions: " : ""

    Object.keys(selectedResult?.conditions || {}).map(e => {
        for (const cond of selectedResult.conditions[e]) {
            title += `${cond.field}=${cond.value} `
        }
    })
    return title;
}

export const AggregationTable = (props: { data: Array<AggregationResult>, onClick: (aggregation: AggregationResult) => void }) => {
    const {data, onClick} = props
    const [selectedResult, setSelectedResult] = useState<any>(undefined)
    const [columns, setColumns] = useState<ColumnsType<AggregationResult>>()
    const [formattedData, setFormattedData] = useState<Array<any>>([])
    const [selectedIndex, setSelectedIndex] = useState<number>()

    const selectRow = (record: AggregationResult, index: any) => {
        setSelectedIndex(index)
        onClick(record)
        setSelectedResult(record)
    };

    const setRowClassName = (record: AggregationResult, index: number) => {
        return index === selectedIndex ? 'clickRowStyl' : '';
    };

    useEffect(() => {
        if (data === undefined || data.length === 0) {
            setColumns([])
            setFormattedData([])
            return
        }
        const newCols: ColumnsType<AggregationResult> = []
        const sample = data[0]

        Object.keys(sample).map(s => {
            if (s !== 'conditions') {
                newCols.push({
                    title: s,
                    dataIndex: s,
                    render: getRenderByValue(s),
                    sorter: getSorter(s as keyof Stats)

                });
                return
            }
            // conditions field
            Object.keys(sample[s]).map(k => {
                // @ts-ignore
                for (const c of sample[s][k]) {
                    newCols.push({
                        title: c.field,
                        dataIndex: c.field,
                        render: getRenderByValue(s)
                    })
                }
            })
        })
        setColumns(newCols)

        // Flat results
        const newFormattedData: Array<AggregationResult> = []
        for (const entry of data) {
            const formattedEntry = {...entry}
            Object.keys(entry.conditions).map(k => {
                // @ts-ignore
                for (const cond of entry.conditions[k]) {
                    // @ts-ignore
                    formattedEntry[cond.field] = cond.value
                }
            })
            newFormattedData.push(formattedEntry)
        }
        setFormattedData(newFormattedData)
    }, [data])

    if (data === undefined || data.length === 0) {
        return <Empty/>
    }
    let title = constructTitle(selectedResult);

    return <Collapse defaultActiveKey={['1']}>
        <Panel header={title} key="1">
            <Table
                columns={columns}
                dataSource={formattedData}
                size="small"
                style={{cursor: "pointer"}}
                onRow={(record, index) => ({
                    onClick: () => {
                        selectRow(record, index);
                    },
                })}
                rowClassName={setRowClassName}
            />
        </Panel>
    </Collapse>
}