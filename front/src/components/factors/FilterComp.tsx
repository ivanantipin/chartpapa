import {useMappedState} from "redux-react-hook";
import {IMainState} from "../../reducers/reducers";
import {default as React, Fragment, useEffect, useState} from "react";
import {portfoliosApi} from "../../services/api/services";
import {Checkbox, Col, Row, Slider} from "antd";
import {ContinuousMeta, DiscreteMeta, TagsMetaSummary, Trade} from "../../api";
import {sortCats} from "./FlatFactorsPage";
import {chunk} from "../../services/tradeUtils";

let _ = require('lodash');

export const FilterComp = (props: { onFilter: (filter: Filter) => void }) => {

    const {portfolioID} = useMappedState((state: IMainState) => {
        return state
    })

    const [metaSummary, setMetaSummary] = useState<TagsMetaSummary>({continuousMetas: [], discreteMetas: []})

    useEffect(() => {
        if (portfolioID) {
            portfoliosApi.portfoliosAvailableTagsList({portfolio: portfolioID!!}).then((pf) => {
                setMetaSummary(pf)
            })
        }
    }, [portfolioID])

    const discr = metaSummary.discreteMetas.map(dm => {
        return <DiscreteSlider key={dm.name} meta={dm} onFilter={_.throttle((mmm: Filter) => {
            props.onFilter(mmm)
        }, 500)}/>
    })

    const cont = metaSummary.continuousMetas.map(dm => {
        return <ContinuosSlider key={dm.name} meta={dm} onFilter={_.throttle((mmm: Filter) => {
            props.onFilter(mmm)
        }, 500)}/>
    })

    return <TabledFilter wrapNo={2} children={[...discr, ...cont]}/>
}


export interface Filter {
    name: string,
    filter: (t: Trade) => boolean
}

const ContinuosSlider = (props: { meta: ContinuousMeta, onFilter: (f: Filter) => void }) => {
    const {meta, onFilter} = props

    const [range, setRange] = useState<[number, number]>([meta.min, meta.max])

    const step = (meta.max - meta.min) / 60

    return (
        <div>
            <h4>{meta.name} {range[0].toFixed(2)} - {range[1].toFixed(2)}</h4>
            <Slider range step={step} min={meta.min} max={meta.max}
                    value={[range[0], range[1]]} onChange={(val: [number,number]) => {

                const rr = val as [number, number]
                setRange(rr)

                onFilter({
                    name: meta.name,
                    filter: (t: Trade) => {
                        let val = t.continuousTags!![meta.name];
                        return rr[0] < val && rr[1] > val
                    }
                })

            }}/>
        </div>
    )
}

const DiscreteSlider = (props: { meta: DiscreteMeta, onFilter: (f: Filter) => void }) => {
    const {meta, onFilter} = props

    const [range, setRange] = useState([0, meta.values.length - 1])

    const [inversed, setInversed] = useState(false)

    let mapped = sortCats(meta.values);

    const mappedIndex: { [key: string]: number } = {}

    const marks: { [key: number]: string } = {}

    for (let idx = 0; idx < mapped.length; idx++){
        let str = mapped[idx];
        marks[idx] = str
        mappedIndex[str] = idx
    }

    return (
        <div>
            <h4>{meta.name} {marks[range[0]]} - {marks[range[1]]}</h4>
            <Checkbox checked={inversed} onChange={e=>setInversed(e.target.checked)}>inversed</Checkbox>
            <Slider marks={marks} step={null} range min={0} max={meta.values.length - 1}
                    value={[range[0], range[1]]} onChange={(val: [number, number]) => {
                const range = val as [number, number]
                setRange(range)
                onFilter({
                    name: meta.name,
                    filter: (t: Trade) => {
                        const idx = mappedIndex[t.discreteTags!![meta.name]]
                        return inversed !== (range[0] <= idx && range[1] >= idx);
                    }
                })

            }
            }/>

        </div>
    )
}


export const TabledFilter = (props: { children: Array<any>, wrapNo: number }) => {
    const {children, wrapNo} = props;

    return (
        <Fragment>
            {
                chunk(children, wrapNo).map((ch: Array<any>, idx: number) => {
                    return (<Row key={`${idx}`} gutter={8}>
                        {
                            ch.map((m,colIdx) => {
                                return (<Col key={ `${colIdx}`}  span={12}>
                                        {m}
                                    </Col>
                                )
                            })
                        }
                    </Row>)
                })
            }
        </Fragment>
    )
}
