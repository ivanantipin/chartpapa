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
package org.openapitools.server.models


/**
 * 
 * @param tradeId 
 * @param side 
 * @param qty 
 * @param openTime 
 * @param closeTime 
 * @param openPrice 
 * @param closePrice 
 * @param pnl 
 * @param symbol 
 * @param discreteTags 
 * @param continuousTags 
 */
data class NewTrade (
    val tradeId: kotlin.String,
    val side: NewTrade.Side,
    val qty: java.math.BigDecimal,
    val openTime: kotlin.Long,
    val closeTime: kotlin.Long,
    val openPrice: java.math.BigDecimal,
    val closePrice: java.math.BigDecimal,
    val pnl: java.math.BigDecimal,
    val symbol: kotlin.String,
    val discreteTags: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,
    val continuousTags: kotlin.collections.Map<kotlin.String, java.math.BigDecimal>? = null
) 
{
    /**
    * 
    * Values: long,short
    */
    enum class Side(val value: kotlin.String){
        long("long"),
        short("short");
    }
}

