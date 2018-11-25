import {Configuration, MainControllerApi, Symbol} from "./api";
import {parseDate} from "./dtutils";
import {Label, Ohlc} from "./api/api";
import {CandleStickChartProps, OhlcTr} from "./components/OhlcChart/OhlcChart";


const basePath = "http://localhost" + ":8080"

const config : Configuration = {
    basePath : basePath
}


export function fetchInstruments(): Promise<Array<Symbol>> {
    return new MainControllerApi(config).instrumentsUsingGET().then(instr=>{
        console.log('receidev instr',instr)
        return instr.map(symbol=>{
                return {
                    name : symbol.name,
                    code : symbol.code
            }
        });
    })
}

export interface TimePointTr {
    time: Date;
    value: number;
}

export function fetchChart(codes: Array<string>) :Promise<Map<string,Array<TimePointTr>>> {
    console.log('fetching codes', codes)
    return new MainControllerApi(config).getSeriesUsingGET(codes).then(res=>{
        console.log('receidev chart',res)
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

export function fetchOhlcChart(code: string): Promise<CandleStickChartProps> {
    let api = new MainControllerApi(config);
    return Promise.all([api.getOhlcsUsingGET(code), api.getAnnotationsUsingGET(code)]).then(arr => {
        let ohlcs = arr[0].map((oh: Ohlc): OhlcTr => {
            return {
                date: parseDate(oh.dateTime),
                open: oh.open,
                high: oh.high,
                low: oh.low,
                close: oh.close,
            }
        });

        const lb: Map<number, Label> = new Map<number, Label>();

        console.log('ann', arr[1])

        arr[1].labels.forEach((ll: Label) => {
            lb.set(parseDate(ll.time).getTime(), ll)
        })
        return {
            name: code,
            data: ohlcs,
            tsToLabel: lb,
            hlines: arr[1].lines
        }
    })
}


