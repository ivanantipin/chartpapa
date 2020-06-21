# ApiInstrumentApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**apiInstrumentDelete**](ApiInstrumentApi.md#apiInstrumentDelete) | **DELETE** /api_instrument | 
[**apiInstrumentGet**](ApiInstrumentApi.md#apiInstrumentGet) | **GET** /api_instrument | 
[**apiInstrumentPatch**](ApiInstrumentApi.md#apiInstrumentPatch) | **PATCH** /api_instrument | 
[**apiInstrumentPost**](ApiInstrumentApi.md#apiInstrumentPost) | **POST** /api_instrument | 


<a name="apiInstrumentDelete"></a>
# **apiInstrumentDelete**
> apiInstrumentDelete(symbol, exchange, identifier, metaContinuous, metaDiscrete, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiInstrumentApi()
val symbol : kotlin.String = symbol_example // kotlin.String | 
val exchange : kotlin.String = exchange_example // kotlin.String | 
val identifier : kotlin.String = identifier_example // kotlin.String | 
val metaContinuous : kotlin.String = metaContinuous_example // kotlin.String | 
val metaDiscrete : kotlin.String = metaDiscrete_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.apiInstrumentDelete(symbol, exchange, identifier, metaContinuous, metaDiscrete, prefer)
} catch (e: ClientException) {
    println("4xx response calling ApiInstrumentApi#apiInstrumentDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiInstrumentApi#apiInstrumentDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **symbol** | **kotlin.String**|  | [optional]
 **exchange** | **kotlin.String**|  | [optional]
 **identifier** | **kotlin.String**|  | [optional]
 **metaContinuous** | **kotlin.String**|  | [optional]
 **metaDiscrete** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="apiInstrumentGet"></a>
# **apiInstrumentGet**
> kotlin.Array&lt;ApiInstrument&gt; apiInstrumentGet(symbol, exchange, identifier, metaContinuous, metaDiscrete, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiInstrumentApi()
val symbol : kotlin.String = symbol_example // kotlin.String | 
val exchange : kotlin.String = exchange_example // kotlin.String | 
val identifier : kotlin.String = identifier_example // kotlin.String | 
val metaContinuous : kotlin.String = metaContinuous_example // kotlin.String | 
val metaDiscrete : kotlin.String = metaDiscrete_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<ApiInstrument> = apiInstance.apiInstrumentGet(symbol, exchange, identifier, metaContinuous, metaDiscrete, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ApiInstrumentApi#apiInstrumentGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiInstrumentApi#apiInstrumentGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **symbol** | **kotlin.String**|  | [optional]
 **exchange** | **kotlin.String**|  | [optional]
 **identifier** | **kotlin.String**|  | [optional]
 **metaContinuous** | **kotlin.String**|  | [optional]
 **metaDiscrete** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;ApiInstrument&gt;**](ApiInstrument.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="apiInstrumentPatch"></a>
# **apiInstrumentPatch**
> apiInstrumentPatch(symbol, exchange, identifier, metaContinuous, metaDiscrete, prefer, apiInstrument)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiInstrumentApi()
val symbol : kotlin.String = symbol_example // kotlin.String | 
val exchange : kotlin.String = exchange_example // kotlin.String | 
val identifier : kotlin.String = identifier_example // kotlin.String | 
val metaContinuous : kotlin.String = metaContinuous_example // kotlin.String | 
val metaDiscrete : kotlin.String = metaDiscrete_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val apiInstrument : ApiInstrument =  // ApiInstrument | api_instrument
try {
    apiInstance.apiInstrumentPatch(symbol, exchange, identifier, metaContinuous, metaDiscrete, prefer, apiInstrument)
} catch (e: ClientException) {
    println("4xx response calling ApiInstrumentApi#apiInstrumentPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiInstrumentApi#apiInstrumentPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **symbol** | **kotlin.String**|  | [optional]
 **exchange** | **kotlin.String**|  | [optional]
 **identifier** | **kotlin.String**|  | [optional]
 **metaContinuous** | **kotlin.String**|  | [optional]
 **metaDiscrete** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **apiInstrument** | [**ApiInstrument**](ApiInstrument.md)| api_instrument | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="apiInstrumentPost"></a>
# **apiInstrumentPost**
> apiInstrumentPost(select, prefer, apiInstrument)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiInstrumentApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val apiInstrument : ApiInstrument =  // ApiInstrument | api_instrument
try {
    apiInstance.apiInstrumentPost(select, prefer, apiInstrument)
} catch (e: ClientException) {
    println("4xx response calling ApiInstrumentApi#apiInstrumentPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiInstrumentApi#apiInstrumentPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **apiInstrument** | [**ApiInstrument**](ApiInstrument.md)| api_instrument | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

