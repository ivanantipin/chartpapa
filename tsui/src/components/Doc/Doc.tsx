import * as React from 'react';
import {Component} from 'react';


import setup from "./setup.png";
import signal13 from "./signal13.png";
import {Col, Row} from "antd";


export default class Doc extends Component<any, any> {

    render() {
        return (
            <div>
                <Row>
                    <Col span={3}>
                        <img src={signal13}/>
                    </Col>
                    <Col>
                        <div>saoeuaoeu</div>
                    </Col>
                </Row>
                <Row>
                    <Col span={3}>
                        < img
                            src={setup}
                        />
                    </Col>
                    <Col>
                        <div>setup </div>
                    </Col>
                </Row>
            </div>

        )
    }
}




