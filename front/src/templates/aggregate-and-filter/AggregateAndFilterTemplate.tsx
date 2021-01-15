import React, {Fragment, useEffect, useState} from "react";
import {Col, Row} from "antd"
import {AggregationTable} from "../../components/tables/AggregationTable";
import {FilterView} from "../../components/filter-view/FilterView";
import {AggregationForm} from "../../components/forms/AggregationForm";
import {AggregationResult,} from "../../services/tradeUtils";
import {useMappedState} from "redux-react-hook";
import {IMainState} from "../../reducers/reducers";
import {Trade} from "../../api/models";
import {filterTradesByAggregationResult} from "../../services/aggregationUtils";

export const AggregateAndFilterTemplate = (props: {
    withForm: boolean,
    aggFunction?: (trades: Array<Trade>) => Array<AggregationResult>, hasCandlestick?: boolean
}) => {

    const {withForm, aggFunction, hasCandlestick} = props

    const {trades, instrumentsMap} = useMappedState((state: IMainState) => {
        return state
    })

    const [filteredTrades, setFilteredTrades] = useState<Array<Trade>>([])
    const [aggData, setAggData] = useState<Array<AggregationResult>>([])


    const applyFilter = (aggregation: AggregationResult) => {
        setFilteredTrades(filterTradesByAggregationResult(trades, aggregation, instrumentsMap))
    }

    const setAggDataFromForm = (results: Array<AggregationResult>) => {
        setAggData(results)
    }

    const aggForm = withForm ? (
        <Row><Col span={24}> <AggregationForm applyResults={setAggDataFromForm}/> </Col></Row>) : null
    let initError = false;

    useEffect(() => {
        if (withForm) {
            return
        }
        if (aggFunction === undefined) {
            initError = true
            return
        }
        const results = aggFunction(trades)
        setAggDataFromForm(results)

    }, [withForm, aggFunction, trades])

    if (initError) {
        return <h3>No agg function provided</h3>
    }

    return (
        <Fragment>
            {aggForm}
            <Row>
                <Col span={24}>
                    <AggregationTable onClick={applyFilter} data={aggData}/>
                </Col>
            </Row>
            <Row>
                <Col span={24}>
                    <FilterView trades={filteredTrades} hasCandlestick={hasCandlestick}/>
                </Col>
            </Row>
        </Fragment>
    )
}