import WidgetPanel from "./components/WidgetsPanel/WidgetPanel";
import SeqPanel from "./components/SeqPanel/SeqPanel";
import StaticHtml from "./components/StaticHtml/StaticHtml";

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
    },
    {
        name : "FunCharts",
        path : '/funcharts/:id',
        component : StaticHtml,
        shown : true
    }
]