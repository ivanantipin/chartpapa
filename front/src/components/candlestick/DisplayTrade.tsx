import React, {useEffect, useState} from 'react';

import Highcharts from "highcharts/highstock";
import HighchartsReact from "highcharts-react-official";
import {portfoliosApi} from "../../services/api/services";
// @ts-ignore
import {CandlesApi} from "../../api/apis";

require('highcharts/modules/annotations')(Highcharts)


export const fetchTicker = (tradeId: string): Promise<string> => {
    return portfoliosApi.displayTrade({tradeId})
}


export const DisplayTrade = (props: { tradeId: string }) => {

    const [opts, setOpts] = useState<any>({})

    useEffect(() => {

        fetchTicker(props.tradeId).then(dt => {

            setOpts(JSON.parse(dt))
        })
    }, [props.tradeId]);


    return (
        <HighchartsReact
            highcharts={Highcharts}
            constructorType={"stockChart"}
            options={opts}/>
    );
}