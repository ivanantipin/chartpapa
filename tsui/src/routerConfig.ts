import WidgetPanel from "./components/WidgetsPanel/WidgetPanel";
import SeqPanel from "./components/SeqPanel/SeqPanel";

export interface MenuConf {
    name : string,
    path : string,
    component : any
}

export const routerConfig : Array<MenuConf>=[
    {
        name : "Indicators",
        path : '/indicators',
        component : SeqPanel
    },
    {
        name : "Widgets",
        path : '/widgets',
        component : WidgetPanel
    }
]