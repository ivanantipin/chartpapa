import React from "react";
import {AggregateAndFilterTemplate} from "../../templates/aggregate-and-filter/AggregateAndFilterTemplate";
import {AggregationConditions, AggregationResult, calcStats, groupBy} from "../../services/tradeUtils";
import {Trade} from "../../api";

export const BySymbolPage = () => {
    const bySymbolAggFunction = (trades: Array<Trade>): Array<AggregationResult> => {
        return Object.entries(groupBy(trades, trd => trd.symbol)).map(entry => {
            let stat = calcStats(entry[1]);
            const conditions: AggregationConditions = {
                discreteTags: [],
                discreteMeta: [],
                continuousMeta: [],
                continuousTags: [],
                tradeCond: []
            }
            conditions.tradeCond.push({field: "symbol", value: entry[0]})
            return {conditions, ...stat}
        });
    }
    return <AggregateAndFilterTemplate withForm={false} aggFunction={bySymbolAggFunction} hasCandlestick={true}/>
}

