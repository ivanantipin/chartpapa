import {useMappedState} from "redux-react-hook";
import {IMainState} from "../../reducers/reducers";
import {default as React, Fragment, useEffect, useState} from "react";
import {portfoliosApi} from "../../services/api/services";
import {Col, Row} from "antd";
import {TagsMetaSummary, Trade} from "../../api";
import {chunk} from "../../services/tradeUtils";
import {ContinuosSlider} from "./ContinuosSlider";
import {DiscreteSlider} from "./DiscreteSlider";

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
