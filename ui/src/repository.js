// @flow


import {MainControllerApi} from "./api";
import type {MergedSeries} from "./api";

export type InstrId = {
    name: string,
    code: string
}



export function fetchInstruments(): Promise<Array<InstrId>> {
    return MainControllerApi().instrumentsUsingGET().then(instr=>{
        return instr.map(code=>{
                return {name : code,
                code : code
            }
        });
    })
}

export function fetchChart(codes: Array<string>): Promise<MergedSeries> {
    return MainControllerApi().loadUsingGET(codes)
}