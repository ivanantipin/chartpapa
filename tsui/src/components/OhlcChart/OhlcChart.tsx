import {HLine, Label} from "../../api";

export interface OhlcTr{
    date : Date,
    open : number,
    high : number,
    low : number,
    close : number
}

export interface CandleStickChartProps{
    name : string,
    data: Array<OhlcTr>,
    tsToLabel: Map<number,Label>,
    hlines : Array<HLine>
}



