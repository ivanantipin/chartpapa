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
    OrderOrderType,
    OrderOrderTypeFromJSON,
    OrderOrderTypeFromJSONTyped,
    OrderOrderTypeToJSON,
    OrderStatus,
    OrderStatusFromJSON,
    OrderStatusFromJSONTyped,
    OrderStatusToJSON,
    Side,
    SideFromJSON,
    SideFromJSONTyped,
    SideToJSON,
} from './';

/**
 * 
 * @export
 * @interface Order
 */
export interface Order {
    /**
     * 
     * @type {string}
     * @memberof Order
     */
    portfolio: string;
    /**
     * 
     * @type {string}
     * @memberof Order
     */
    orderId: string;
    /**
     * 
     * @type {Side}
     * @memberof Order
     */
    side: Side;
    /**
     * 
     * @type {OrderOrderType}
     * @memberof Order
     */
    orderType: OrderOrderType;
    /**
     * 
     * @type {OrderStatus}
     * @memberof Order
     */
    status: OrderStatus;
    /**
     * 
     * @type {number}
     * @memberof Order
     */
    qty: number;
    /**
     * 
     * @type {number}
     * @memberof Order
     */
    placeTime: number;
    /**
     * 
     * @type {number}
     * @memberof Order
     */
    updateTime: number;
    /**
     * 
     * @type {string}
     * @memberof Order
     */
    symbol: string;
    /**
     * 
     * @type {number}
     * @memberof Order
     */
    id?: number | null;
    /**
     * 
     * @type {{ [key: string]: string; }}
     * @memberof Order
     */
    discreteTags: { [key: string]: string; };
    /**
     * 
     * @type {{ [key: string]: number; }}
     * @memberof Order
     */
    continuousTags: { [key: string]: number; };
    /**
     * 
     * @type {string}
     * @memberof Order
     */
    tradeId: string;
    /**
     * 
     * @type {number}
     * @memberof Order
     */
    price: number;
    /**
     * 
     * @type {number}
     * @memberof Order
     */
    executionPrice: number;
}

export function OrderFromJSON(json: any): Order {
    return OrderFromJSONTyped(json, false);
}

export function OrderFromJSONTyped(json: any, ignoreDiscriminator: boolean): Order {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'portfolio': json['portfolio'],
        'orderId': json['orderId'],
        'side': SideFromJSON(json['side']),
        'orderType': OrderOrderTypeFromJSON(json['orderType']),
        'status': OrderStatusFromJSON(json['status']),
        'qty': json['qty'],
        'placeTime': json['placeTime'],
        'updateTime': json['updateTime'],
        'symbol': json['symbol'],
        'id': !exists(json, 'id') ? undefined : json['id'],
        'discreteTags': json['discreteTags'],
        'continuousTags': json['continuousTags'],
        'tradeId': json['tradeId'],
        'price': json['price'],
        'executionPrice': json['executionPrice'],
    };
}

export function OrderToJSON(value?: Order | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'portfolio': value.portfolio,
        'orderId': value.orderId,
        'side': SideToJSON(value.side),
        'orderType': OrderOrderTypeToJSON(value.orderType),
        'status': OrderStatusToJSON(value.status),
        'qty': value.qty,
        'placeTime': value.placeTime,
        'updateTime': value.updateTime,
        'symbol': value.symbol,
        'id': value.id,
        'discreteTags': value.discreteTags,
        'continuousTags': value.continuousTags,
        'tradeId': value.tradeId,
        'price': value.price,
        'executionPrice': value.executionPrice,
    };
}


