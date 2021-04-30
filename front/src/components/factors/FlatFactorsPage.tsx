import {default as React} from "react";
import {Trade} from "../../api/models";
import {CategoryFactorWidget} from "./CategoryFactorWidget";
import {Tabs} from "antd";
import {Stats} from "../../services/tradeUtils";
import {PieWidget} from "./PieWidget";

let _ = require('lodash');


export const ChartTypes = ['Pie', 'Simple']

export interface FactorConf {
    metric: keyof Stats,
    chartType: string
}

export const defaultFactorConf: FactorConf = {metric: 'pnl', chartType: 'Simple'}

export const sortCats = (cats: Array<string>): Array<string> => {
    return cats
        .map(it => parseInt(it)).sort((a, b) => a - b).map(it => it.toString())
}

export interface CatDescription {
    key: string,
    description: string
}


export interface FactorWidgetProps {
    trades: Array<Trade>,
    title: string,
    metric: string,
    groupFunc: (tr: Trade) => string,
    categories: Array<CatDescription>
}

export const UniversalWidget = (props: (FactorWidgetProps & { chartType: string })) => {
    if (props.chartType === 'Simple') {
        return <CategoryFactorWidget {...props}/>
    }
    return <PieWidget {...props}/>
}
