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

/**
 * 
 * @export
 * @enum {string}
 */
export enum OrderOrderType {
    Limit = 'limit',
    Market = 'market',
    Stop = 'stop',
    StopLimit = 'stopLimit',
    MarketOnClose = 'marketOnClose',
    MarketOnOpen = 'marketOnOpen',
    LimitOnClose = 'limitOnClose',
    LimitOnOpen = 'limitOnOpen'
}

export function OrderOrderTypeFromJSON(json: any): OrderOrderType {
    return OrderOrderTypeFromJSONTyped(json, false);
}

export function OrderOrderTypeFromJSONTyped(json: any, ignoreDiscriminator: boolean): OrderOrderType {
    return json as OrderOrderType;
}

export function OrderOrderTypeToJSON(value?: OrderOrderType | null): any {
    return value as any;
}

