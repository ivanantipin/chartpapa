import React, {useEffect, useState} from 'react';

import Highcharts from "highcharts/highstock";
import HighchartsReact from "highcharts-react-official";
import {portfoliosApi} from "../../services/api/services";
// @ts-ignore
import {CandlesApi} from "../../api/apis";

require('highcharts/modules/annotations')(Highcharts)

export const DisplayTrade = (props: { tradeId: string }) => {

    const [opts, setOpts] = useState<any>({})

    useEffect(() => {

        portfoliosApi.displayTrade({tradeId: props.tradeId}).then(dt => {

            setOpts(JSON.parse(dt))
        })
    }, [props.tradeId]);


    return (
        <div style={{height : '700px'}}>
        <HighchartsReact
            highcharts={Highcharts}
            constructorType={"stockChart"}
            options={opts}/>
        </div>
    );
}