import {MainControllerApi} from "./api";
import {parseDate} from "./dtutils";

export type InstrId = {
    name: string,
    code: string
}

export function fetchInstruments(): Promise<Array<InstrId>> {
    return new MainControllerApi().instrumentsUsingGET().then(instr=>{
        return instr.map(code=>{
                return {
                    name : code,
                    code : code
            }
        });
    })
}

export interface TimePointTr {
    time: Date;
    value: number;
}

export function fetchChart(codes: Array<string>) :Promise<Map<string,Array<TimePointTr>>> {
    return new MainControllerApi().loadUsingGET(codes).then(res=>{
        const ret = new Map<string,Array<TimePointTr>>()
        Object.keys(res).forEach(key=>{
            let nar = res[key].map(tp=>{
                return {
                    time : parseDate(tp.time),
                    value : tp.value
                }
            });
            ret.set(key, nar)
        })
        return ret;
    });
}