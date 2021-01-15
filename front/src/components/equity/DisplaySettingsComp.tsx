import {Checkbox, Row} from "antd";
import * as React from "react";

export interface DisplaySettings {
    splitBySide: boolean,
    xAsTradeNo: boolean
}

export const DisplaySettingsComp = (props: { displaySettings: DisplaySettings, onChange: (displaySettings: DisplaySettings) => void }) => {
    const {displaySettings} = props
    return (
        <Row style={{padding: '5px'}}>
            <Checkbox checked={displaySettings.splitBySide} onChange={e => {
                props.onChange({...displaySettings, splitBySide: e.target.checked})
            }}>SplitBySide</Checkbox>
            <Checkbox checked={displaySettings.xAsTradeNo} onChange={e => {
                props.onChange({...displaySettings, xAsTradeNo: e.target.checked})
            }}>SetDrawByTrade</Checkbox>
        </Row>
    )
}