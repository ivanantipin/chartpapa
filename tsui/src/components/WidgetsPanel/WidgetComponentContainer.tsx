import {connect} from "react-redux";

import "react-select/dist/react-select.css";
import "react-virtualized-select/styles.css";

import {WidgetCallbacks, WidgetComponent} from "./WidgetComponent";
//import {fetchChart} from "../../repository";
import {Action, makeAmend} from "../../actions";
import {MainStore} from "../types";
import {fetchChart} from "../../repository";



const dispatchCallbacks = (dispatch: (s: Action) => void): WidgetCallbacks => {
    return {
        onChange: (metaId: string, codes: Array<string>) => {
            fetchChart(codes).then(mapOfChart => {
                dispatch(makeAmend((store: MainStore) => {
                    let widgets = store.widgets.map(wdata=> {
                        if (wdata.id == metaId) {
                            return {...wdata, chartData: mapOfChart, codes: codes}
                        } else {
                            return wdata
                        }
                    });
                    return {...store, widgets : widgets};
                }))
            })
        },
    }
};


export default connect(l => l, dispatchCallbacks)(WidgetComponent)