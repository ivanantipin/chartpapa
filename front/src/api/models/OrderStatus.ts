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
export enum OrderStatus {
    Filled = 'filled',
    Canceled = 'canceled',
    Placed = 'placed',
    PartialFilled = 'partialFilled',
    Rejected = 'rejected'
}

export function OrderStatusFromJSON(json: any): OrderStatus {
    return OrderStatusFromJSONTyped(json, false);
}

export function OrderStatusFromJSONTyped(json: any, ignoreDiscriminator: boolean): OrderStatus {
    return json as OrderStatus;
}

export function OrderStatusToJSON(value?: OrderStatus | null): any {
    return value as any;
}

