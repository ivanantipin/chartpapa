import {timeParse, timeFormat} from "d3-time-format";

let specifier = "%Y-%m-%dT%H:%M:%S";
let parser = timeParse(specifier);

export const parseDate=(str : string)=>{
    return parser(str) || new Date()
}

export const formatDate=(dt : Date)=>{
    return timeFormat(specifier)(dt)
}

