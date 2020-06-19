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
 * @param tradeId 
 * @param discreteTags 
 * @param continuousTags 
 * @param portfolio 
 * @param side 
 * @param qty 
 * @param openTime 
 * @param closeTime 
 * @param openPrice 
 * @param closePrice 
 * @param pnl 
 * @param symbol 
 */

data class Trade (
    @JsonProperty("trade_id")
    val tradeId: kotlin.String,
    @JsonProperty("discrete_tags")
    val discreteTags: kotlin.collections.Map<kotlin.String, kotlin.String>,
    @JsonProperty("continuous_tags")
    val continuousTags: kotlin.collections.Map<kotlin.String, java.math.BigDecimal>,
    @JsonProperty("portfolio")
    val portfolio: kotlin.String,
    @JsonProperty("side")
    val side: kotlin.String,
    @JsonProperty("qty")
    val qty: java.math.BigDecimal,
    @JsonProperty("open_time")
    val openTime: java.time.OffsetDateTime,
    @JsonProperty("close_time")
    val closeTime: java.time.OffsetDateTime,
    @JsonProperty("open_price")
    val openPrice: java.math.BigDecimal,
    @JsonProperty("close_price")
    val closePrice: java.math.BigDecimal,
    @JsonProperty("pnl")
    val pnl: java.math.BigDecimal,
    @JsonProperty("symbol")
    val symbol: kotlin.String
)
