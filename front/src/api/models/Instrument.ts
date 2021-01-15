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
/**
 * 
 * @export
 * @interface Instrument
 */
export interface Instrument {
    /**
     * 
     * @type {string}
     * @memberof Instrument
     */
    symbolAndExchange: string;
    /**
     * 
     * @type {string}
     * @memberof Instrument
     */
    symbol: string;
    /**
     * 
     * @type {string}
     * @memberof Instrument
     */
    exchange: string;
    /**
     * 
     * @type {{ [key: string]: string; }}
     * @memberof Instrument
     */
    metaDiscrete: { [key: string]: string; };
    /**
     * 
     * @type {{ [key: string]: number; }}
     * @memberof Instrument
     */
    metaContinuous: { [key: string]: number; };
}

export function InstrumentFromJSON(json: any): Instrument {
    return InstrumentFromJSONTyped(json, false);
}

export function InstrumentFromJSONTyped(json: any, ignoreDiscriminator: boolean): Instrument {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'symbolAndExchange': json['symbolAndExchange'],
        'symbol': json['symbol'],
        'exchange': json['exchange'],
        'metaDiscrete': json['metaDiscrete'],
        'metaContinuous': json['metaContinuous'],
    };
}

export function InstrumentToJSON(value?: Instrument | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'symbolAndExchange': value.symbolAndExchange,
        'symbol': value.symbol,
        'exchange': value.exchange,
        'metaDiscrete': value.metaDiscrete,
        'metaContinuous': value.metaContinuous,
    };
}


