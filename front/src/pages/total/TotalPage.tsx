import React, {useEffect, useReducer, useState} from "react";
import {Checkbox, Collapse, Tabs} from "antd";
import {useMappedState} from "redux-react-hook";
import {IMainState} from "../../reducers/reducers";
import {EquityPanel} from "../../components/equity/EquityPanel";
import {FactorsPanel} from "../../components/factors/FactorsPanel";
import {SymbolsAndChart} from "../../components/tables/SymbolResults";
import {DateStatTable} from "../../components/tables/ByDateResults";
import {StatsPanel} from "../../components/equity/StatsPanel";
import {Trade} from "../../api/models";
import {Filter, FilterComp} from "../../components/factors/FilterComp";
import {DisplayTrade} from "../../components/candlestick/DisplayTrade";

const {TabPane} = Tabs;

const {Panel} = Collapse;

let _ = require('lodash');


interface FilteredTrades {
    filter: { [key: string]: Filter }
    trades: Array<Trade>
}

export const TotalPage = (props: any) => {

    const origTrades = useMappedState((state: IMainState) => {
        return state.trades
    })

    const [inversed, setInversed] = useState(false)

    const [filter, dispatch] = useReducer((state: FilteredTrades, action: Filter) => {
        const tradesAndFilter = {...state}
        tradesAndFilter.filter[action.name] = action
        let filters = Object.values(tradesAndFilter.filter);
        tradesAndFilter.trades = origTrades.filter(it => {
            return filters.length === 0 || filters.every(f => f.filter(it))
        })
        return tradesAndFilter
    }, {
        filter: {},
        trades: origTrades
    }, undefined)

    useEffect(()=>{
        dispatch({name : "dummy", filter : (t)=>true})
        return
    },[origTrades])

    return (
        <>
            <Collapse defaultActiveKey={['1']}>
                <Panel header={'Filter'} key="1">
                    <Checkbox checked={inversed} onChange={e => {
                        const lastTrade = origTrades[origTrades.length * 0.8 | 0]
                        if(e.target.checked){
                            dispatch({name : "oos", filter : (t)=>t.openTime < lastTrade.openTime})
                        }else {
                            dispatch({name : "oos", filter : (t)=>true})
                        }
                        setInversed(e.target.checked)
                    }}>oos</Checkbox>
                    <FilterComp onFilter={(f)=>{
                        dispatch(f)
                    }}/>
                </Panel>
            </Collapse>
            <Tabs defaultActiveKey="equity" animated={false}>
                <TabPane tab="Trades" key="trades">
                    <SymbolsAndChart trades={filter.trades} key={'trades'}/>
                </TabPane>
                <TabPane tab="Factors" key="factors">
                    <FactorsPanel trades={filter.trades}/>
                </TabPane>
                <TabPane tab="Equity" key="equity">
                    <EquityPanel trades={filter.trades}/>
                </TabPane>
                <TabPane tab="Stats" key="stats">
                    <StatsPanel trades={filter.trades}/>
                </TabPane>
                <TabPane tab="By Date" key="by_date">
                    <DateStatTable trades={filter.trades} orders={[]}/>
                </TabPane>
                <TabPane tab="Dummy" key="dummy">
                    <DisplayTrade tradeId={"some"}/>
                </TabPane>
            </Tabs>
        </>
    )
}