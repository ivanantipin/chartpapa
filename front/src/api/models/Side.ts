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
export enum Side {
    Sell = 'Sell',
    Buy = 'Buy',
    None = 'None'
}

export function SideFromJSON(json: any): Side {
    return SideFromJSONTyped(json, false);
}

export function SideFromJSONTyped(json: any, ignoreDiscriminator: boolean): Side {
    return json as Side;
}

export function SideToJSON(value?: Side | null): any {
    return value as any;
}
