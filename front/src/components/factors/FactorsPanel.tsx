import * as React from "react";
import {useState} from "react";
import {defaultFactorConf, FactorConf} from "./FlatFactorsPage";
import {Trade} from "../../api/models";
import {FactorTab} from "./FactorTab";
import {FlatFactorsComp} from "./FlatFactorsComp";

export const FactorsPanel = (props: { trades: Array<Trade> }) => {
    const {trades} = props

    const [factorConf, setFactorConf] = useState<FactorConf>(defaultFactorConf)

    return <>
        <FactorTab onFactorConf={it => {
            setFactorConf(it)
        }
        } factorConf={factorConf}/>
        <FlatFactorsComp trades={trades} conf={factorConf}/>
    </>
};