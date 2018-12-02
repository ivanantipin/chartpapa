import {Configuration, MainControllerApi} from "./api";
import {parseDate} from "./dtutils";
import {InstrId, Label, Ohlc} from "./api/api";
import {CandleStickChartProps, OhlcTr} from "./components/OhlcChart/OhlcChart";


const config : Configuration = {
    basePath : "http://" + location.hostname + ":8080"
}


export function fetchInstruments(): Promise<Array<InstrId>> {
    return new MainControllerApi(config).instrumentsUsingGET()
}

export interface TimePointTr {
    time: Date;
    value: number;
}

export function fetchChart(codes: Array<InstrId>) :Promise<Map<string,Array<TimePointTr>>> {
    console.log('fetching codes', codes)
    return new MainControllerApi(config).getSeriesUsingPOST(codes).then(res=>{
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

export function fetchOhlcChart(code: InstrId, timeframe : string): Promise<CandleStickChartProps> {
    let api = new MainControllerApi(config);
    return Promise.all([api.getOhlcsUsingPOST(code, timeframe), api.getAnnotationsUsingPOST(code, timeframe)]).then(arr => {

        console.log('preohlc',arr[0])

        let ohlcs = arr[0].map((oh: Ohlc): OhlcTr => {
            return {
                date: new Date(oh.dateTimeMs),
                open: oh.open,
                high: oh.high,
                low: oh.low,
                close: oh.close,
            }
        });

        console.log('ohlcs ',ohlcs)

        const lb: Map<number, Label> = new Map<number, Label>();

        console.log('ann', arr[1])

        arr[1].labels.forEach((ll: Label) => {
            lb.set(parseDate(ll.time).getTime(), ll)
        })
        return {
            name: code.code,
            data: ohlcs,
            tsToLabel: lb,
            hlines: arr[1].lines
        }
    })
}


