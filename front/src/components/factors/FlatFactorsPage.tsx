import {useMappedState} from "redux-react-hook";
import {IMainState} from "../../reducers/reducers";
import {default as React, useEffect, useState} from "react";
import {TagsMetaSummary, Trade} from "../../api/models";
import {CategoryFactorWidget} from "./CategoryFactorWidget";
import {portfoliosApi} from "../../services/api/services";
import {Tabs} from "antd";
import {Stats} from "../../services/tradeUtils";
import {PieWidget} from "./PieWidget";
import moment from "moment-timezone";

let _ = require('lodash');


const {TabPane} = Tabs;

const BUCKETS_NUM = 10

const getQuantileFunction = (min: number, max: number, valExtract: (tr: Trade) => number): (tr: Trade) => string => {
    const range = max - min; // to allow to avoid last bucket with single metric
    return (tr: Trade) => {
        const val = valExtract(tr)
        return `${((val - min) / range * BUCKETS_NUM).toFixed(0)}Q`
    }
}

const getCats = (min: number, max: number): Array<CatDescription> => {
    return Array.from(Array(BUCKETS_NUM + 1).keys()).map(it => {
        return {
            key: `${it}Q`,
            description: `${(min + (max - min) * it / BUCKETS_NUM).toFixed(2)} - ${(min + (max - min) * (it + 1) / BUCKETS_NUM).toFixed(2)}`
        }
    })
}


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

export const FlatFactorsComp = (props: { trades: Array<Trade>, conf: FactorConf }) => {

    const {trades, conf} = props

    const metric = conf.metric

    const {portfolioID, availableInstrumentsMeta, instrumentsMap} = useMappedState((state: IMainState) => {
        return state
    })

    const [metas, setMeta] = useState<TagsMetaSummary>()

    useEffect(() => {
        if (!metas) {
            portfoliosApi.portfoliosAvailableTagsList({portfolio: portfolioID!}).then((pf) => {
                setMeta(pf)
            })
        }
    })

    if (!metas) {
        return <div/>
    }


    if (metas.discreteMetas.length === 0 && metas.continuousMetas.length === 0) {
        return <div>No factors</div>
    }

    const defaultKey = metas.discreteMetas.length !== 0 ? metas.discreteMetas[0].name : metas.continuousMetas[0].name;


    const cont: Array<FactorWidgetProps> = metas.continuousMetas.map(meta => {

        return {
            trades: trades,
            title: meta.name,
            metric: metric,
            groupFunc: getQuantileFunction(meta.min, meta.max, it => it.continuousTags![meta.name]),
            categories: getCats(meta.min, meta.max)
        }
    });

    const descr: Array<FactorWidgetProps> = metas.discreteMetas.map(meta => {
        return {
            trades: trades,
            title: meta.name,
            metric: metric,
            groupFunc: (it: Trade) => {
                return it.discreteTags![meta.name]
            },
            categories: sortCats(meta.values).map(it => {
                return {
                    key: it,
                    description: it
                }
            })
        }
    })

    const discrInstr = availableInstrumentsMeta?.discreteMetas.map(meta => {
        return {
            trades: trades,
            title: meta.name,
            metric: metric,
            groupFunc: (it: Trade) => {
                return instrumentsMap!![it.symbol]!!.metaDiscrete!![meta.name]
            },
            categories: meta.values.map(it => {
                return {
                    key: it,
                    description: it
                }
            })
        }
    }) || []

    const contInstr = availableInstrumentsMeta?.continuousMetas.map(meta => {
        return {
            trades: trades,
            title: meta.name,
            metric: metric,
            groupFunc: getQuantileFunction(meta.min, meta.max, tr => {
                try {
                    return instrumentsMap!![tr.symbol]!!.metaContinuous!![meta.name]
                } catch {
                    console.log(meta)
                    return 0
                }

            }),
            categories: getCats(meta.min, meta.max)
        }
    }) || []


    const allmetas = [...cont, ...descr, ...discrInstr, ...contInstr,
        {...hourProp, metric, trades},
        {...weekDayProp, metric, trades},
        {...dateProp, metric, trades}
    ]


    return <Tabs defaultActiveKey={defaultKey} animated={false}>
        {
            allmetas.map(meta => {
                return <TabPane tab={meta.title} key={meta.title}>
                    <UniversalWidget trades={trades}
                                     title={meta.title}
                                     metric={meta.metric}
                                     groupFunc={meta.groupFunc}
                                     categories={meta.categories} chartType={conf.chartType}/>
                </TabPane>
            })
        }
    </Tabs>
};

export interface CatDescription {
    key: string,
    description: string
}

const hourProp = {
    title: 'Hour Stat',
    metric: 'pnl',
    groupFunc: (tr: Trade) => {
        return `${moment(tr.openTime).hour()}`
    },
    categories: Array.from(Array(25).keys()).map(it => {
        return {
            key: '' + it,
            description: '' + it
        }
    })
}

const weekDayProp = {
    title: 'Week Day Stat',
    metric: 'pnl',
    groupFunc: (tr: Trade) => {
        return `${moment(tr.openTime).day()}`
    },
    categories: Array.from(Array(8).keys()).map(it => {
        return {
            key: '' + it,
            description: '' + it
        }
    })
}

const dateProp = {
    title: 'Date Stat',
    metric: 'pnl',
    groupFunc: (tr: Trade) => {
        return `${moment(tr.openTime).date()}`
    },
    categories: Array.from(Array(32).keys()).map(it => {
        return {
            key: '' + it,
            description: '' + it
        }
    })
}


interface FactorWidgetProps {
    trades: Array<Trade>,
    title: string,
    metric: string,
    groupFunc: (tr: Trade) => string,
    categories: Array<CatDescription>
}

const UniversalWidget = (props: (FactorWidgetProps & { chartType: string })) => {
    if (props.chartType === 'Simple') {
        return <CategoryFactorWidget {...props}/>
    }
    return <PieWidget {...props}/>
}
