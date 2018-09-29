import Chart from "../src/components/Chart/Chart";

import * as React from "react";
import {storiesOf} from "@storybook/react";
import {withInfo} from "@storybook/addon-info";
import {TimePointTr} from "../build/lib/src/repository";


const stories = storiesOf("Components", module);

const getChart = (): Map<string,Array<TimePointTr>> => {
    const mp = new Map<string,Array<TimePointTr>>();
    mp.set('aaa', [
        {time : new Date(2015, 5, 1), value : 1},
        {time : new Date(2015, 5, 2), value : 2},
        {time : new Date(2015, 5, 1), value : 1},
        ]
    );
    return mp
};

stories.add(
    "TicTacToeCell",
    withInfo({ inline: true })(() => {
        {
            return <Chart series={getChart()}/>
        }
    })
);