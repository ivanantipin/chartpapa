import {
    InstrumentMap,
    INSTRUMENTS_MAP_LOADED,
    LoadStatus,
    ORDERS_LOADED,
    SET_AVAILABLE_INSTRUMENTS_META,
    SET_AVAILABLE_PORTFOLIOS,
    SET_LOAD_STATUS,
    SET_PORTFOLIO_ID,
    SET_TAGS_AVAILABLE,
    TRADES_LOADED
} from "../reducers/reducers";
import {Instrument, Order, Portfolio, PortfolioInstrumentsMeta, TagsMetaSummary, Trade} from "../api/models";


export const setPortfolioID = (portfolioID: string) => {
    return {
        type: SET_PORTFOLIO_ID,
        payload: portfolioID
    }
};

export const setPortfolios = (portfolios: Array<Portfolio>) => {
    return {
        type: SET_AVAILABLE_PORTFOLIOS,
        payload: portfolios
    }
};

export const setTrades = (trades: Array<Trade>) => {
    return {
        type: TRADES_LOADED,
        payload: trades
    }
};

export const setLoadStatus = (loadStatus: LoadStatus) => {
    return {
        type: SET_LOAD_STATUS,
        payload: loadStatus
    }
};


export const setOrders = (orders: Array<Order>) => {
    return {
        type: ORDERS_LOADED,
        payload: orders
    }
};

export const setAvailableTags = (tags?: TagsMetaSummary) => {
    return {type: SET_TAGS_AVAILABLE, payload: tags}
}

export const setAvailableInstrumentsMeta = (meta?: PortfolioInstrumentsMeta) => {
    return {type: SET_AVAILABLE_INSTRUMENTS_META, payload: meta}
}

export const setInstruments = (instruments?: Array<Instrument>) => {
    const instrumentMap: InstrumentMap = {}
    instruments?.forEach(i => {
        instrumentMap[i.symbolAndExchange] = i
    })

    return {type: INSTRUMENTS_MAP_LOADED, payload: instrumentMap}
}
