// @flow

import type {InstrId} from "../repository";
import type {WidgetData} from "../components/WidgetComponent";


export type MainStore = {
    widgets : Array<WidgetData>,
    instruments : Array<InstrId>
}

