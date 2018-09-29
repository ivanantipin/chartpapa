import {MainStore} from "../components/types";


export interface AmendAction {
    type : "AMEND",
    thunk : (s : MainStore)=>MainStore,
    debunk? : any

}

export function makeAmend(thunk : (s : MainStore)=>MainStore) : AmendAction {
    return {
        type : 'AMEND',
        thunk : thunk
    }
}

export type Action = AmendAction