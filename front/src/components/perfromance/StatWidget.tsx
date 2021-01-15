import React, {Fragment} from 'react';
import 'antd/dist/antd.css';
import {Col, Row, Statistic} from 'antd';
import {chunk, Stats} from "../../services/tradeUtils";


export const StatWidget = (props: { metrics: Stats, wrapNo: number }) => {
    const {metrics, wrapNo} = props;

    return (
        <Fragment>
            {

                chunk(Object.entries(metrics), wrapNo).map((ch: Array<[string, number]>, idx: number) => {
                    return (<Row key={idx} gutter={8}>
                        {
                            ch.map(m => {
                                return (<Col key={m[0]} span={6}>
                                        <Statistic title={m[0]} value={m[1]} precision={3}/>
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