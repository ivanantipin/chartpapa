/**
* API
* UI API
*
* The version of the OpenAPI document: v1
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package org.openapitools.client.models


import com.fasterxml.jackson.annotation.JsonProperty
/**
 * 
 * @param portfolio 
 * @param orderId 
 * @param side 
 * @param orderType 
 * @param status 
 * @param qty 
 * @param placeTime 
 * @param updateTime 
 * @param symbol 
 * @param id 
 * @param discreteTags 
 * @param continuousTags 
 * @param tradeId 
 * @param price 
 * @param executionPrice 
 */

data class Order (
    @JsonProperty("portfolio")
    val portfolio: kotlin.String,
    @JsonProperty("order_id")
    val orderId: kotlin.String,
    @JsonProperty("side")
    val side: Order.Side,
    @JsonProperty("order_type")
    val orderType: Order.OrderType,
    @JsonProperty("status")
    val status: Order.Status,
    @JsonProperty("qty")
    val qty: java.math.BigDecimal,
    @JsonProperty("place_time")
    val placeTime: kotlin.Int,
    @JsonProperty("update_time")
    val updateTime: kotlin.Int,
    @JsonProperty("symbol")
    val symbol: kotlin.String,
    @JsonProperty("id")
    val id: kotlin.Int? = null,
    @JsonProperty("discrete_tags")
    val discreteTags: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,
    @JsonProperty("continuous_tags")
    val continuousTags: kotlin.collections.Map<kotlin.String, java.math.BigDecimal>? = null,
    @JsonProperty("trade_id")
    val tradeId: kotlin.String? = null,
    @JsonProperty("price")
    val price: java.math.BigDecimal? = null,
    @JsonProperty("execution_price")
    val executionPrice: java.math.BigDecimal? = null
) {

    /**
    * 
    * Values: buy,sell
    */
    
    enum class Side(val value: kotlin.String){
        @JsonProperty(value="buy") buy("buy"),
        @JsonProperty(value="sell") sell("sell");
    }
    /**
    * 
    * Values: limit,market,stop,stopLimit,marketOnClose,marketOnOpen,limitOnClose,limitOnOpen
    */
    
    enum class OrderType(val value: kotlin.String){
        @JsonProperty(value="limit") limit("limit"),
        @JsonProperty(value="market") market("market"),
        @JsonProperty(value="stop") stop("stop"),
        @JsonProperty(value="stop_limit") stopLimit("stop_limit"),
        @JsonProperty(value="market_on_close") marketOnClose("market_on_close"),
        @JsonProperty(value="market_on_open") marketOnOpen("market_on_open"),
        @JsonProperty(value="limit_on_close") limitOnClose("limit_on_close"),
        @JsonProperty(value="limit_on_open") limitOnOpen("limit_on_open");
    }
    /**
    * 
    * Values: filled,canceled,placed,partialFilled
    */
    
    enum class Status(val value: kotlin.String){
        @JsonProperty(value="filled") filled("filled"),
        @JsonProperty(value="canceled") canceled("canceled"),
        @JsonProperty(value="placed") placed("placed"),
        @JsonProperty(value="partial_filled") partialFilled("partial_filled");
    }
}

