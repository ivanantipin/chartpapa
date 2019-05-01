import {Configuration, MainControllerApi} from "./api";
import {parseDate} from "./dtutils";
import {InstrId, Label, Ohlc} from "./api/api";
import {CandleStickChartProps, OhlcTr} from "./components/OhlcChart/OhlcChart";


function getUrl() {
    if(location.hostname.indexOf("chartpapa") >= 0){
        return "http://" + location.hostname + ":80"
    }else {
        return "http://" + location.hostname + ":8080"
    }
}

const config : Configuration = {
    basePath : getUrl()
}

export const mainControllerApi = new MainControllerApi(config);


export function fetchInstruments(): Promise<Array<InstrId>> {
    return mainControllerApi.instrumentsUsingGET()
}

export interface TimePointTr {
    time: Date;
    value: number;
}

export function fetchChart(codes: Array<InstrId>) :Promise<Map<string,Array<TimePointTr>>> {
    console.log('fetching codes', codes)
    return mainControllerApi.getSeriesUsingPOST(codes).then(res=>{
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

    return Promise.all([mainControllerApi.getOhlcsUsingPOST(code, timeframe), mainControllerApi.getAnnotationsUsingPOST(code, timeframe)]).then(arr => {

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


