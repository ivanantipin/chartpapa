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

import org.openapitools.client.models.ContinuousMeta
import org.openapitools.client.models.DiscreteMeta

import com.fasterxml.jackson.annotation.JsonProperty
/**
 * 
 * @param continuousMetas 
 * @param discreteMetas 
 */

data class TagsMetaSummary (
    @JsonProperty("continuous_metas")
    val continuousMetas: kotlin.Array<ContinuousMeta>,
    @JsonProperty("discrete_metas")
    val discreteMetas: kotlin.Array<DiscreteMeta>
)

