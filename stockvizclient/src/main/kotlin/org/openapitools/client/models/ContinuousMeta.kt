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
 * @param name 
 * @param min 
 * @param max 
 */

data class ContinuousMeta (
    @JsonProperty("name")
    val name: kotlin.String,
    @JsonProperty("min")
    val min: java.math.BigDecimal,
    @JsonProperty("max")
    val max: java.math.BigDecimal
)
