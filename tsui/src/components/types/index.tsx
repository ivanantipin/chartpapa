import {WidgetData} from "../WidgetsPanel/WidgetComponent";
import {InstrId} from "api";


export interface MainStore {
    widgets : Array<WidgetData>,
    instruments : Array<InstrId>
}

