

export type AmendAction = {
    type : "AMEND",
    thunk : (MainStore)=>MainStore,
    debunk : ?any

}

export function makeAmend(thunk : (MainStore)=>MainStore) : AmendAction {
    return {
        type : 'AMEND',
        thunk : thunk

    }
}

export type Action = AmendAction