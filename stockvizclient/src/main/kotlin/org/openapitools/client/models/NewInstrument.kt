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
 * @param symbol 
 * @param exchange 
 * @param metaDiscrete 
 * @param metaContinuous 
 */

data class NewInstrument (
    @JsonProperty("symbol")
    val symbol: kotlin.String,
    @JsonProperty("exchange")
    val exchange: kotlin.String,
    @JsonProperty("meta_discrete")
    val metaDiscrete: kotlin.Any? = null,
    @JsonProperty("meta_continuous")
    val metaContinuous: kotlin.Any? = null
)

