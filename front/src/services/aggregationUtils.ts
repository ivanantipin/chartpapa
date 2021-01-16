import {Trade} from "../api/models";
import {InstrumentMap} from "../reducers/reducers";
import {AggregationConditions, AggregationResult, calcStats, groupBy} from "./tradeUtils";
import {GetContQuantizationFunc, QuantinizeAggType, QuantinizeFunction} from "./quantizationUtils";

export interface AggParams {
    discrete_tags?: Array<any>
    continuous_tags?: Array<any>
    discrete_metas?: Array<any>
    continuous_metas?: Array<any>
    agg_method: string
    split_groups: number
}

export const aggregateTradesByParams = (params: AggParams, trades: Array<Trade>, instrumentsMap?: InstrumentMap): Array<AggregationResult> => {
    const aggType:QuantinizeAggType = params.agg_method === 'Quantile'? QuantinizeAggType.Quantile: QuantinizeAggType.Range

    const stringToConditions: { [key: string]: AggregationConditions } = {}

    const continuousMetaQuantinizeFuncs: { [key: string]: QuantinizeFunction } = {}
    const continuousTagsQuantinizeFuncs: { [key: string]: QuantinizeFunction } = {}

    params.continuous_tags?.forEach(m => {
        // @ts-ignore
        const valArr = trades.map(t => t.continuousTags[m])
        continuousTagsQuantinizeFuncs[m] = GetContQuantizationFunc(valArr, params.split_groups, aggType)
    })

    params.continuous_metas?.forEach(m => {
        // @ts-ignore
        const valArr = trades.map(t => {
            // @ts-ignore
            const inst = instrumentsMap[t.symbol]
            // @ts-ignore
            return inst.metaContinuous[m]
        })
        continuousMetaQuantinizeFuncs[m] = GetContQuantizationFunc(valArr, params.split_groups, aggType)
    })

    const aggFunc = (trade: Trade): string => {
        let agg_str = ''
        const sep = "*+*"
        const conditions: AggregationConditions = {
            discreteTags: [],
            discreteMeta: [],
            continuousMeta: [],
            continuousTags: [],
            tradeCond: []
        }
        params.discrete_tags?.forEach((t) => {
            // @ts-ignore
            agg_str += `${sep}${t}-${trade.discreteTags[t]}`
            // @ts-ignore
            conditions.discreteTags.push({field: t, value: trade.discreteTags[t]})
        })

        params.continuous_tags?.forEach(p => {
            const qFunc = continuousTagsQuantinizeFuncs[p]
            // @ts-ignore
            const qVal = qFunc(trade.continuousTags[p])
            agg_str += `${sep}${p}-${qVal}`
            conditions.continuousTags.push({field: p, value: qVal, func: qFunc})
        })

        params.discrete_metas?.forEach(m => {
            // @ts-ignore
            const val = instrumentsMap[trade.symbol].metaDiscrete[m]
            agg_str += `${sep}${m}-${val}`
            conditions.discreteMeta.push({field: m, value: val})
        })

        params.continuous_metas?.forEach(m => {
            const qFunc = continuousMetaQuantinizeFuncs[m]
            // @ts-ignore
            const val = qFunc(instrumentsMap[trade.symbol].metaDiscrete[m])
            agg_str += `${sep}${m}-${val}`
            conditions.continuousMeta.push({field: m, value: val, func: qFunc})
        })

        stringToConditions[agg_str] = conditions
        return agg_str
    }

    console.log(stringToConditions)
    const grouped = groupBy(trades, aggFunc)
    return Object.entries(grouped).map(entry => {
        let stat = calcStats(entry[1]);
        const conditions = stringToConditions[entry[0]]
        return {conditions, ...stat}
    })
}

export const filterTradesByAggregationResult = (trades: Array<Trade>, aggregation: AggregationResult, instrumentsMap?: InstrumentMap): Array<Trade> => {
    const filteredTrades: Array<Trade> = []
    trades.forEach(t => {
        let allOK = true
        aggregation.conditions.tradeCond?.forEach(cond => {
            // @ts-ignore
            if (t[cond.field] !== cond.value) {
                allOK = false
            }
        })
        if (!allOK) {
            return
        }
        aggregation.conditions.discreteTags?.forEach(cond => {
            // @ts-ignore
            if (t.discreteTags[cond.field] !== cond.value) {
                allOK = false
            }
        })

        if (!allOK) {
            return
        }

        aggregation.conditions.continuousTags?.forEach(cond => {
            // @ts-ignore
            const comVal = cond.func(t.continuousTags[cond.field])
            if (comVal !== cond.value) {
                allOK = false
            }
        })

        if (!allOK) {
            return
        }


        aggregation.conditions.discreteMeta?.forEach(cond => {
            if (instrumentsMap === undefined || instrumentsMap === null) {
                return
            }
            // @ts-ignore
            const instVal = instrumentsMap[t.symbol].metaDiscrete[cond.field]
            if (instVal !== cond.value) {
                allOK = false
            }
        })

        if (!allOK) {
            return
        }

        aggregation.conditions.continuousMeta?.forEach(cond => {
            if (instrumentsMap === undefined || instrumentsMap === null) {
                return
            }
            // @ts-ignore
            const instVal = cond.func(instrumentsMap[t.symbol].metaDiscrete[cond.field])
            if (instVal !== cond.value) {
                allOK = false
            }
        })

        if (!allOK) {
            return
        }

        filteredTrades.push(t)
    })
    return filteredTrades
}