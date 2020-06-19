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
 * @param success Request status
 * @param message Results description
 */

data class InlineResponse2001 (
    /* Request status */
    @JsonProperty("success")
    val success: kotlin.Boolean? = null,
    /* Results description */
    @JsonProperty("message")
    val message: kotlin.String? = null
)
