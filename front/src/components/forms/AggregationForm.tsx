import React, {Fragment, useEffect, useState} from "react";
import {AggregationResult} from "../../services/tradeUtils";
import {useMappedState} from "redux-react-hook";
import {IMainState} from "../../reducers/reducers";
import {Button, Col, Divider, Empty, Form, InputNumber, Radio, Row, Select} from "antd"
import {DownOutlined, UpOutlined} from '@ant-design/icons'
import "./style.css"
import {aggregateTradesByParams} from "../../services/aggregationUtils";

const {Option} = Select;

const formItemLayout = {
    labelCol: {
        span: 6,
    },
    wrapperCol: {
        span: 14,
    },
}


export const AggregationForm = (props: { applyResults: (results: Array<AggregationResult>) => void }) => {
    const {availableTags, trades, availableInstrumentsMeta} = useMappedState((state: IMainState) => {
        return state
    })

    const {instrumentsMap} = useMappedState((state: IMainState) => {
        return state
    })

    const [tradesTagsOptions, setTradesTagsOptions] = useState<Array<any>>([])
    const [tradesTagsOptionsCont, setTradesTagsOptionsCont] = useState<Array<any>>([])
    const [instrumentsMetaOptions, setInstrumentsMetaOptions] = useState<Array<any>>([])
    const [instrumentsMetaOptionsCont, setInstrumentsMetaOptionsCont] = useState<Array<any>>([])
    const [collapse, setCollapse] = useState(false);
    const [form] = Form.useForm();


    useEffect(() => {
        if (availableTags === undefined) {
            setTradesTagsOptions([])
            setTradesTagsOptionsCont([])
            return
        }
        const newOptions = availableTags.discreteMetas.map(m => {
            return (<Option value={m.name}>{m.name}</Option>)
        })
        setTradesTagsOptions(newOptions)
        const newOptionsCont = availableTags.continuousMetas.map(m => {
            return (<Option value={m.name}>{m.name}</Option>)
        })
        setTradesTagsOptionsCont(newOptionsCont)
    }, [availableTags])


    useEffect(() => {
        if (availableInstrumentsMeta === undefined) {
            setInstrumentsMetaOptions([])
            return
        }
        const newOptions = availableInstrumentsMeta.discreteMetas.map(m => {
            return (<Option value={m.name}>{m.name}</Option>)
        })
        setInstrumentsMetaOptions(newOptions)

        const newOptionsCont = availableInstrumentsMeta.continuousMetas.map(m => {
            return (<Option value={m.name}>{m.name}</Option>)
        })
        setInstrumentsMetaOptionsCont(newOptionsCont)
    }, [availableInstrumentsMeta])

    if (availableTags === undefined) {
        return <Empty/>
    }

    const onFinish = (values: any) => {
        setCollapse(true)
        props.applyResults(aggregateTradesByParams(values, trades, instrumentsMap))
    }
    const inputFields = collapse ? null : (
        <Fragment>
            <Row gutter={24}>
                <Col span={12} key='discrete_tags'>
                    <Form.Item
                        name="discrete_tags"
                        label="Tags"
                        rules={[{
                            required: false,
                            message: 'Select portfolio trades tag fields you want to use for aggregation',
                            type: 'array',
                        },]}>
                        <Select mode="multiple" placeholder="Select tags..">
                            {tradesTagsOptions}
                        </Select>
                    </Form.Item>
                </Col>
                <Col span={12} key='continuous_tags'>
                    <Form.Item
                        name="continuous_tags"
                        label="Tags Continuous"
                        rules={[{
                            required: false,
                            message: 'Select portfolio trades tag fields you want to use for aggregation',
                            type: 'array',
                        },]}>
                        <Select mode="multiple" placeholder="Select tags..">
                            {tradesTagsOptionsCont}
                        </Select>
                    </Form.Item>
                </Col>
            </Row>
            <Row gutter={24}>
                <Col span={12} key='discrete_metas'>
                    <Form.Item
                        name="discrete_metas"
                        label="Meta"
                        rules={[{
                            required: false,
                            message: 'Select fields you want to use for aggregation',
                            type: 'array',
                        },]}>
                        <Select mode="multiple" placeholder="Select meta..">
                            {instrumentsMetaOptions}
                        </Select>
                    </Form.Item>
                </Col>
                <Col span={12} key='continuous_metas'>
                    <Form.Item
                        name="continuous_metas"
                        label="Meta Continuous"
                        rules={[{
                            required: false,
                            message: 'Select fields you want to use for aggregation',
                            type: 'array',
                        },]}>
                        <Select mode="multiple" placeholder="Select meta..">
                            {instrumentsMetaOptionsCont}
                        </Select>
                    </Form.Item>
                </Col>
            </Row>
            <Row>
                <Divider/>
                <Col span={12} key="agg_method">
                    <Form.Item name="agg_method" label="Method">
                        <Radio.Group defaultValue="Quantile">
                            <Radio value="Quantile">Quantile</Radio>
                            <Radio value="Range">Range</Radio>
                        </Radio.Group>
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label="Split groups" name='split_groups'>
                        <InputNumber min={5} max={50} step={5}/>
                    </Form.Item>
                </Col>
            </Row>
        </Fragment>
    )
    const buttons = !collapse ? (
        <Row>
            <Col span={24} style={{textAlign: 'right'}} key='agg_submit_group'>
                <Button type="primary" htmlType="submit">
                    Aggregate
                </Button>
                <Button style={{margin: '0 8px'}}
                        onClick={() => {
                            form.resetFields();
                        }}>
                    Clear
                </Button>
                <a style={{fontSize: 14}}
                   onClick={() => {
                       setCollapse(!collapse);
                   }}>{!collapse ? <UpOutlined/> : <DownOutlined/>} Collapse
                </a>
            </Col>
        </Row>) : (
        <Row>
            <Col span={24} style={{textAlign: 'right'}} key='agg_submit_group'>
                <Button type="link" style={{fontSize: 14}}
                   onClick={() => {
                       setCollapse(!collapse);
                   }}>{!collapse ? <UpOutlined/> : <DownOutlined/>} Show form
                </Button>
            </Col>
        </Row>
    )
    return (
        <Form name="validate_other"
              className="ant-advanced-search-form"
              {...formItemLayout}
              form={form}
              initialValues={{
                  ["split_groups"]: 10
              }}
              onFinish={onFinish}>
            {inputFields}
            {buttons}
        </Form>
    )
}


