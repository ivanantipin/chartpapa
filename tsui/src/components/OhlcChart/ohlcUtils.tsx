import {tsvParse} from "d3-dsv";
import {timeParse} from "d3-time-format";

const parseDate = timeParse("%Y-%m-%d");

function parseData() {
    return function(d : any) {
        d.date = parseDate(d.date);
        d.open = +d.open;
        d.high = +d.high;
        d.low = +d.low;
        d.close = +d.close;
        d.volume = +d.volume;

        return d;
    };
}



export function getData() {
    const promiseMSFT = fetch("https://cdn.rawgit.com/rrag/react-stockcharts/master/docs/data/MSFT.tsv")
        .then(response => response.text())
        .then(data => tsvParse(data, parseData()));
    return promiseMSFT;
}
