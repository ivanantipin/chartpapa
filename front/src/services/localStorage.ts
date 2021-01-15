import {Instrument, PortfolioInstrumentsMeta, TagsMetaSummary} from "../api/models";

const LAST_INSTRUMENTS_UPDATE = 'LAST_INSTRUMENTS_UPDATE'
const STORED_INSTRUMENTS = 'STORED_INSTRUMENTS'
const PORTFOLIO_UPDATE = 'PORTFOLIO_UPDATE'
const PORTFOLIO_AVAILABLE_TAGS = 'PORTFOLIO_AVAILABLE_TAGS'
const PORTFOLIO_INSTRUMENTS_META = 'PORTFOLIO_INSTRUMENTS_META'


export function getLocalStoredInstruments(): Array<Instrument> | undefined {
    const stored = localStorage.getItem(STORED_INSTRUMENTS)
    if (stored !== null && stored !== 'undefined') {
        return JSON.parse(stored)
    }
    return
}

export function setLocalStoredInstruments(map: Array<Instrument> | undefined) {
    localStorage.setItem(STORED_INSTRUMENTS, JSON.stringify(map))
}

export interface PortfolioUpdateInfo {
    orders: number
    trades: number
}

export function getLastStoredPortfolioUpdate(portfolioID: string): PortfolioUpdateInfo | undefined {
    const stored = localStorage.getItem(`${PORTFOLIO_UPDATE}_${portfolioID}`)
    if (stored !== null && stored !== 'undefined') {
        return JSON.parse(stored)
    }
    return
}

export function getStoredPortfolioInstrumentsMeta(portfolioID: string): PortfolioInstrumentsMeta | undefined {
    const stored = localStorage.getItem(`${PORTFOLIO_INSTRUMENTS_META}_${portfolioID}`)
    if (stored !== null && stored !== 'undefined') {
        return JSON.parse(stored)
    }
    return
}

export function setStoredPortfolioInstrumentsMeta(portfolioID: string, meta: PortfolioInstrumentsMeta) {
    localStorage.setItem(`${PORTFOLIO_INSTRUMENTS_META}_${portfolioID}`, JSON.stringify(meta))
}


export function setStoredPortfolioAvailableTags(portfolioID: string, tags: TagsMetaSummary) {
    localStorage.setItem(`${PORTFOLIO_INSTRUMENTS_META}_${portfolioID}`, JSON.stringify(tags))
}