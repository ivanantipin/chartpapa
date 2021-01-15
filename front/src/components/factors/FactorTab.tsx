import {calcStats, Stats} from "../../services/tradeUtils";
import {Card, Checkbox, Col, Row} from "antd";
import {CheckboxValueType} from "antd/es/checkbox/Group";
import {default as React} from "react";
import {ChartTypes, FactorConf} from "./FlatFactorsPage";

export const FactorTab = (props: { onFactorConf: (conf: FactorConf) => void, factorConf: FactorConf }) => {

    let sample = calcStats([])

    const {metric, chartType} = props.factorConf

    return (<Row>
        <Col span={18}>
            <Card title="metrics" size={'small'}>
                <Checkbox.Group options={Object.keys(sample)} value={[metric]}
                                onChange={(it: Array<CheckboxValueType>) => {
                                    let find = it.find(v => v !== metric);
                                    if (find) {
                                        props.onFactorConf({metric: find as keyof Stats, chartType})
                                    }
                                }}/>
            </Card>
        </Col>
        <Col span={6}>
            <Card title="chart types" size={'small'}>
                <Checkbox.Group options={ChartTypes} value={[chartType]} onChange={it => {
                    let find = it.find(v => v !== chartType);
                    if (find) {
                        props.onFactorConf({metric, chartType: find as string})
                    }
                }}/>
            </Card>
        </Col>
    </Row>)
}