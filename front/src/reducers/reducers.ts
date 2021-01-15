import {Instrument, Order, Portfolio, PortfolioInstrumentsMeta, TagsMetaSummary, Trade} from "../api/models";

let _ = require('lodash');

export interface InstrumentMap {
    [key: string]: Instrument
}

export interface IMainState {
    portfolioID?: string,
    dailyQuotes: Array<any>,
    intradayQuotes: Array<any>,
    availablePortfolios: Array<Portfolio>,
    trades: Array<Trade>,
    orders: Array<Order>,
    availableTags?: TagsMetaSummary,
    availableInstrumentsMeta?: PortfolioInstrumentsMeta,
    loadStatus: LoadStatus
    instrumentsMap?: InstrumentMap
}

export enum LoadStatusEnum {
    Loading, Loaded, Error
}

export interface LoadStatus {
    [key: string]: LoadStatusEnum
}

export const loadingLoadingState = {
    instruments: LoadStatusEnum.Loading,
    orders: LoadStatusEnum.Loading,
    tags: LoadStatusEnum.Loading,
    trades: LoadStatusEnum.Loading
};


const initialState: IMainState = {
    portfolioID: undefined,
    dailyQuotes: [],
    intradayQuotes: [],
    availablePortfolios: [],
    trades: [],
    orders: [],
    loadStatus: loadingLoadingState
};


export const SET_PORTFOLIO_ID = 'SET_PORTFOLIO_ID';
export const SET_AVAILABLE_PORTFOLIOS = 'SET_AVAILABLE_PORTFOLIOS';
export const TRADES_LOADED = 'LOAD_TRADES';
export const SET_LOAD_STATUS = 'SET_LOAD_STATUS';
export const ORDERS_LOADED = 'ORDERS_LOADED';

export const DAILY_QUOTES_LOADED = 'DAILY_QUOTES_LOADED';
export const INTRADAY_QUOTES_LOADED = 'INTRADAY_QUOTES_LOADED';
export const SET_TAGS_AVAILABLE = 'SET_TAGS_AVAILABLE'
export const SET_AVAILABLE_INSTRUMENTS_META = 'SET_AVAILABLE_INSTRUMENTS_META'
export const INSTRUMENTS_MAP_LOADED = 'INSTRUMENTS_MAP_LOADED'


export const reducer = (state = initialState, action: any) => {
    switch (action.type) {
        case TRADES_LOADED:
            const trd: Array<Trade> = action.payload
            return {
                ...state,
                trades: trd.sort((t0, t1) => t0.openTime - t1.openTime),
            };
        case ORDERS_LOADED:
            const ord: Array<Order> = action.payload
            return {
                ...state,
                orders: ord.sort((t0, t1) => t0.placeTime - t1.placeTime),
            };
        case SET_AVAILABLE_PORTFOLIOS:
            return {
                ...state,
                availablePortfolios: action.payload
            };
        case SET_LOAD_STATUS:
            return {
                ...state,
                loadStatus: {...state.loadStatus, ...action.payload}
            };

        case SET_PORTFOLIO_ID:
            return {
                ...state,
                portfolioID: action.payload
            };
        case DAILY_QUOTES_LOADED:
            return {
                ...state,
                dailyQuotes: action.payload
            };

        case INTRADAY_QUOTES_LOADED:
            return {
                ...state,
                intradayQuotes: action.payload
            };
        case SET_TAGS_AVAILABLE:
            return {
                ...state,
                availableTags: action.payload
            }
        case SET_AVAILABLE_INSTRUMENTS_META:
            return {
                ...state,
                availableInstrumentsMeta: action.payload
            }
        case INSTRUMENTS_MAP_LOADED:
            return {
                ...state,
                instrumentsMap: action.payload
            }
        default:
            return state
    }
};
