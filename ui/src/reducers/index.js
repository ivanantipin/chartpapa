// @flow


import type {MainStore} from "../types";
import type {Action} from "../actions";

const def : MainStore = {
    instruments : [],
    widgets : []
}

export function mainReducer(state : MainStore = def, action : Action) : MainStore{
    console.log('action received', action)
    if(action.type == 'AMEND'){
        return action.thunk(state)
    }
    return state
}

