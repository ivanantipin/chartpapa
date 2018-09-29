import {WidgetData} from "../WidgetsPanel/WidgetComponent";
import {InstrId} from "../../repository";

export interface MainStore {
    widgets : Array<WidgetData>,
    instruments : Array<InstrId>
}

