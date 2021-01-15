/* tslint:disable */
/* eslint-disable */
/**
 * demo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { exists, mapValues } from '../runtime';
import {
    Side,
    SideFromJSON,
    SideFromJSONTyped,
    SideToJSON,
} from './';

/**
 * 
 * @export
 * @interface Trade
 */
export interface Trade {
    /**
     * 
     * @type {string}
     * @memberof Trade
     */
    tradeId: string;
    /**
     * 
     * @type {string}
     * @memberof Trade
     */
    portfolio: string;
    /**
     * 
     * @type {Side}
     * @memberof Trade
     */
    side: Side;
    /**
     * 
     * @type {number}
     * @memberof Trade
     */
    qty: number;
    /**
     * 
     * @type {number}
     * @memberof Trade
     */
    openTime: number;
    /**
     * 
     * @type {number}
     * @memberof Trade
     */
    closeTime: number;
    /**
     * 
     * @type {number}
     * @memberof Trade
     */
    openPrice: number;
    /**
     * 
     * @type {number}
     * @memberof Trade
     */
    closePrice: number;
    /**
     * 
     * @type {number}
     * @memberof Trade
     */
    pnl: number;
    /**
     * 
     * @type {string}
     * @memberof Trade
     */
    symbol: string;
    /**
     * 
     * @type {{ [key: string]: string; }}
     * @memberof Trade
     */
    discreteTags: { [key: string]: string; };
    /**
     * 
     * @type {{ [key: string]: number; }}
     * @memberof Trade
     */
    continuousTags: { [key: string]: number; };
}

export function TradeFromJSON(json: any): Trade {
    return TradeFromJSONTyped(json, false);
}

export function TradeFromJSONTyped(json: any, ignoreDiscriminator: boolean): Trade {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'tradeId': json['tradeId'],
        'portfolio': json['portfolio'],
        'side': SideFromJSON(json['side']),
        'qty': json['qty'],
        'openTime': json['openTime'],
        'closeTime': json['closeTime'],
        'openPrice': json['openPrice'],
        'closePrice': json['closePrice'],
        'pnl': json['pnl'],
        'symbol': json['symbol'],
        'discreteTags': json['discreteTags'],
        'continuousTags': json['continuousTags'],
    };
}

export function TradeToJSON(value?: Trade | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'tradeId': value.tradeId,
        'portfolio': value.portfolio,
        'side': SideToJSON(value.side),
        'qty': value.qty,
        'openTime': value.openTime,
        'closeTime': value.closeTime,
        'openPrice': value.openPrice,
        'closePrice': value.closePrice,
        'pnl': value.pnl,
        'symbol': value.symbol,
        'discreteTags': value.discreteTags,
        'continuousTags': value.continuousTags,
    };
}


