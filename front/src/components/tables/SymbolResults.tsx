import React, {useMemo, useState} from 'react';
import 'antd/dist/antd.css';
import {Button, Input, Space, Table} from 'antd';
// @ts-ignore
import Highlighter from 'react-highlight-words';

import {SearchOutlined} from '@ant-design/icons';
import "./style.css"
import {calcStats, groupBy} from "../../services/tradeUtils";
import {Trade} from "../../api/models";
import {CandlestickHigh} from "../candlestick/CandlestickHigh";


const columns = [
    {
        title: 'Symbol',
        dataIndex: 'symbol',
        key: 'symbol',
        width: '5%',
    },
    {
        title: 'Sharpe',
        dataIndex: 'sharpe',
        key: 'sharpe',
        width: '5%',

    },
    {
        title: 'Trades',
        dataIndex: 'cnt',
        key: 'cnt',
        width: '10%',
        sorter: {
            compare: (a: { cnt: number }, b: { cnt: number }) => a.cnt - b.cnt,
            multiple: 1
        },
    },
    {
        title: 'Average Trade',
        dataIndex: 'avgPnl',
        key: 'avgPnl',
        width: '5%',
        sorter: {
            compare: (a: any, b: any) => a.avgPnl - b.avgPnl,
            multiple: 1
        },
    },
    {
        title: 'PnL',
        dataIndex: 'pnl',
        key: 'pnl',
        width: '10%',
        sorter: {
            compare: (a: any, b: any) => a.pnl - b.pnl,
            multiple: 1
        },
    },
    {
        title: 'Sector',
        dataIndex: 'sector',
        key: 'sector',
        width: '20%',

    },
    {
        title: 'Industry',
        dataIndex: 'industry',
        key: 'industry',
        width: '20%',

    },
    {
        title: 'Type',
        dataIndex: 'type',
        key: 'type',
        width: '10%',

    },

];

export const SymbolsTable = (props: { onSymbolSelect: (symbol: string) => void, trades: Array<Trade> }) => {

    const [searchText, setSearchText] = useState('')
    const [searchColumn, setSearchColumn] = useState('')
    const [selectedSymbol, setSelectedSymbol] = useState()

    const data = useMemo(() => {
        let ret = Object.entries(groupBy(props.trades, trd => trd.symbol)).map(entry => {
            let stat = calcStats(entry[1]);
            return {
                symbol: entry[0],
                cnt: entry[1].length,
                pnl: stat.pnl,
                avgPnl: stat.avgPnl
            }
        });
        return ret
    }, [props.trades])

    const sf = (dataIndex: string) => ({
        // @ts-ignore
        filterDropdown: ({setSelectedKeys, selectedKeys, confirm, clearFilters}) => (
            <div style={{padding: 8}}>
                <Input
                    ref={node => {
                        // searchInput = node;
                    }}
                    placeholder={`Search ${dataIndex}`}
                    value={selectedKeys[0]}
                    onChange={e => setSelectedKeys(e.target.value ? [e.target.value] : [])}
                    onPressEnter={() => handleSearch(selectedKeys, confirm, dataIndex)}
                    style={{width: 188, marginBottom: 8, display: 'block'}}
                />
                <Space>
                    <Button
                        type="primary"
                        onClick={() => handleSearch(selectedKeys, confirm, dataIndex)}
                        icon={<SearchOutlined/>}
                        size="small"
                        style={{width: 90}}
                    >
                        Search
                    </Button>
                    <Button onClick={() => handleReset(clearFilters)} size="small" style={{width: 90}}>
                        Reset
                    </Button>
                </Space>
            </div>
        ),
        filterIcon: (filtered: any) => <SearchOutlined style={{color: filtered ? '#1890ff' : undefined}}/>,
        onFilter: (value: any, record: any) =>
            record[dataIndex].toString().toLowerCase().includes(value.toLowerCase()),
        onFilterDropdownVisibleChange: (visible: any) => {
            if (visible) {
                // setTimeout(() => searchInput.select());
            }
        },
        render: (text: any) =>
            searchColumn === dataIndex ? (
                <Highlighter
                    highlightStyle={{backgroundColor: '#ffc069', padding: 0}}
                    searchWords={[searchText]}
                    autoEscape
                    textToHighlight={text.toString()}
                />
            ) : (
                <div>{text}</div>
            ),
    });

    const cols = [...columns]

    cols[0] = {...cols[0], ...sf('symbol')}

    const handleSearch = (selectedKeys: Array<string>, confirm: any, dataIndex: string) => {
        confirm();
        setSearchText(selectedKeys[0])
        setSearchColumn(dataIndex)
    };

    const handleReset = (clearFilters: any) => {
        clearFilters();
        setSearchText('')
    };

    const selectRow = (record: any) => {
        setSelectedSymbol(record.symbol)
        props.onSymbolSelect(record.symbol);
    };

    const setRowClassName = (record: any) => {
        return record.symbol === selectedSymbol ? 'clickRowStyl' : '';
    };

    return <Table
        columns={cols}
        dataSource={data}
        size="small"
        rowClassName={setRowClassName}
        style={{cursor: "pointer"}}
        onRow={(record) => ({
            onClick: () => {
                selectRow(record);
            },
        })}
    />;
}

export const SymbolsAndChart = (props: { trades: Array<Trade> }) => {
    let [symbol, setSymbol] = useState('na');
    return <div>
        <SymbolsTable trades={props.trades} onSymbolSelect={setSymbol}/>
        <CandlestickHigh ticker={symbol} trades={props.trades.filter(it => it.symbol === symbol)}/>
    </div>
}