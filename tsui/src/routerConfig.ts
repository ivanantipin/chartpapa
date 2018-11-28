import WidgetPanel from "./components/WidgetsPanel/WidgetPanel";
import SeqPanel from "./components/SeqPanel/SeqPanel";

export interface MenuConf {
    name : string,
    path : string,
    shown : boolean,
    component : any
}

export const routerConfig : Array<MenuConf>=[
    {
        name : "Sequenta",
        path : '/indicators',
        component : SeqPanel,
        shown : true
    },
    {
        name : "Widgets",
        path : '/widgets',
        component : WidgetPanel,
        shown : false
    }
]