import {Trade} from "../../api/models";
import {default as React, useMemo} from "react";
import {calcStats} from "../../services/tradeUtils";
import {StatWidget} from "../perfromance/StatWidget";

export const StatsPanel = (props: { trades: Array<Trade> }) => {
    const {trades} = props

    let stats = useMemo(() => {
        return calcStats(trades)
    }, [trades])

    return <StatWidget metrics={stats} wrapNo={4}/>
};