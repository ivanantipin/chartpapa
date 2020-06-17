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
 * @param time 
 * @param symbol 
 * @param open 
 * @param high 
 * @param low 
 * @param close 
 * @param volume 
 * @param datetime 
 * @param opendatetime 
 * @param id 
 */
data class CandlesDaily (
    val time: kotlin.String,
    val symbol: kotlin.String,
    val open: java.math.BigDecimal,
    val high: java.math.BigDecimal,
    val low: java.math.BigDecimal,
    val close: java.math.BigDecimal,
    val volume: kotlin.Int,
    val datetime: java.time.LocalDateTime,
    val opendatetime: java.time.LocalDateTime,
    val id: kotlin.Int? = null
) 

