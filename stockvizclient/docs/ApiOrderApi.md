# ApiOrderApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**apiOrderDelete**](ApiOrderApi.md#apiOrderDelete) | **DELETE** /api_order | 
[**apiOrderGet**](ApiOrderApi.md#apiOrderGet) | **GET** /api_order | 
[**apiOrderPatch**](ApiOrderApi.md#apiOrderPatch) | **PATCH** /api_order | 
[**apiOrderPost**](ApiOrderApi.md#apiOrderPost) | **POST** /api_order | 


<a name="apiOrderDelete"></a>
# **apiOrderDelete**
> apiOrderDelete(id, orderId, tradeId, side, orderType, status, qty, placeTime, updateTime, price, executionPrice, discreteTags, continuousTags, symbolId, portfolio, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiOrderApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val orderId : kotlin.String = orderId_example // kotlin.String | 
val tradeId : kotlin.String = tradeId_example // kotlin.String | 
val side : kotlin.String = side_example // kotlin.String | 
val orderType : kotlin.String = orderType_example // kotlin.String | 
val status : kotlin.String = status_example // kotlin.String | 
val qty : kotlin.String = qty_example // kotlin.String | 
val placeTime : kotlin.String = placeTime_example // kotlin.String | 
val updateTime : kotlin.String = updateTime_example // kotlin.String | 
val price : kotlin.String = price_example // kotlin.String | 
val executionPrice : kotlin.String = executionPrice_example // kotlin.String | 
val discreteTags : kotlin.String = discreteTags_example // kotlin.String | 
val continuousTags : kotlin.String = continuousTags_example // kotlin.String | 
val symbolId : kotlin.String = symbolId_example // kotlin.String | 
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.apiOrderDelete(id, orderId, tradeId, side, orderType, status, qty, placeTime, updateTime, price, executionPrice, discreteTags, continuousTags, symbolId, portfolio, prefer)
} catch (e: ClientException) {
    println("4xx response calling ApiOrderApi#apiOrderDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiOrderApi#apiOrderDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **orderId** | **kotlin.String**|  | [optional]
 **tradeId** | **kotlin.String**|  | [optional]
 **side** | **kotlin.String**|  | [optional]
 **orderType** | **kotlin.String**|  | [optional]
 **status** | **kotlin.String**|  | [optional]
 **qty** | **kotlin.String**|  | [optional]
 **placeTime** | **kotlin.String**|  | [optional]
 **updateTime** | **kotlin.String**|  | [optional]
 **price** | **kotlin.String**|  | [optional]
 **executionPrice** | **kotlin.String**|  | [optional]
 **discreteTags** | **kotlin.String**|  | [optional]
 **continuousTags** | **kotlin.String**|  | [optional]
 **symbolId** | **kotlin.String**|  | [optional]
 **portfolio** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="apiOrderGet"></a>
# **apiOrderGet**
> kotlin.Array&lt;ApiOrder&gt; apiOrderGet(id, orderId, tradeId, side, orderType, status, qty, placeTime, updateTime, price, executionPrice, discreteTags, continuousTags, symbolId, portfolio, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiOrderApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val orderId : kotlin.String = orderId_example // kotlin.String | 
val tradeId : kotlin.String = tradeId_example // kotlin.String | 
val side : kotlin.String = side_example // kotlin.String | 
val orderType : kotlin.String = orderType_example // kotlin.String | 
val status : kotlin.String = status_example // kotlin.String | 
val qty : kotlin.String = qty_example // kotlin.String | 
val placeTime : kotlin.String = placeTime_example // kotlin.String | 
val updateTime : kotlin.String = updateTime_example // kotlin.String | 
val price : kotlin.String = price_example // kotlin.String | 
val executionPrice : kotlin.String = executionPrice_example // kotlin.String | 
val discreteTags : kotlin.String = discreteTags_example // kotlin.String | 
val continuousTags : kotlin.String = continuousTags_example // kotlin.String | 
val symbolId : kotlin.String = symbolId_example // kotlin.String | 
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<ApiOrder> = apiInstance.apiOrderGet(id, orderId, tradeId, side, orderType, status, qty, placeTime, updateTime, price, executionPrice, discreteTags, continuousTags, symbolId, portfolio, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ApiOrderApi#apiOrderGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiOrderApi#apiOrderGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **orderId** | **kotlin.String**|  | [optional]
 **tradeId** | **kotlin.String**|  | [optional]
 **side** | **kotlin.String**|  | [optional]
 **orderType** | **kotlin.String**|  | [optional]
 **status** | **kotlin.String**|  | [optional]
 **qty** | **kotlin.String**|  | [optional]
 **placeTime** | **kotlin.String**|  | [optional]
 **updateTime** | **kotlin.String**|  | [optional]
 **price** | **kotlin.String**|  | [optional]
 **executionPrice** | **kotlin.String**|  | [optional]
 **discreteTags** | **kotlin.String**|  | [optional]
 **continuousTags** | **kotlin.String**|  | [optional]
 **symbolId** | **kotlin.String**|  | [optional]
 **portfolio** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;ApiOrder&gt;**](ApiOrder.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="apiOrderPatch"></a>
# **apiOrderPatch**
> apiOrderPatch(id, orderId, tradeId, side, orderType, status, qty, placeTime, updateTime, price, executionPrice, discreteTags, continuousTags, symbolId, portfolio, prefer, apiOrder)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiOrderApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val orderId : kotlin.String = orderId_example // kotlin.String | 
val tradeId : kotlin.String = tradeId_example // kotlin.String | 
val side : kotlin.String = side_example // kotlin.String | 
val orderType : kotlin.String = orderType_example // kotlin.String | 
val status : kotlin.String = status_example // kotlin.String | 
val qty : kotlin.String = qty_example // kotlin.String | 
val placeTime : kotlin.String = placeTime_example // kotlin.String | 
val updateTime : kotlin.String = updateTime_example // kotlin.String | 
val price : kotlin.String = price_example // kotlin.String | 
val executionPrice : kotlin.String = executionPrice_example // kotlin.String | 
val discreteTags : kotlin.String = discreteTags_example // kotlin.String | 
val continuousTags : kotlin.String = continuousTags_example // kotlin.String | 
val symbolId : kotlin.String = symbolId_example // kotlin.String | 
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val apiOrder : ApiOrder =  // ApiOrder | api_order
try {
    apiInstance.apiOrderPatch(id, orderId, tradeId, side, orderType, status, qty, placeTime, updateTime, price, executionPrice, discreteTags, continuousTags, symbolId, portfolio, prefer, apiOrder)
} catch (e: ClientException) {
    println("4xx response calling ApiOrderApi#apiOrderPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiOrderApi#apiOrderPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **orderId** | **kotlin.String**|  | [optional]
 **tradeId** | **kotlin.String**|  | [optional]
 **side** | **kotlin.String**|  | [optional]
 **orderType** | **kotlin.String**|  | [optional]
 **status** | **kotlin.String**|  | [optional]
 **qty** | **kotlin.String**|  | [optional]
 **placeTime** | **kotlin.String**|  | [optional]
 **updateTime** | **kotlin.String**|  | [optional]
 **price** | **kotlin.String**|  | [optional]
 **executionPrice** | **kotlin.String**|  | [optional]
 **discreteTags** | **kotlin.String**|  | [optional]
 **continuousTags** | **kotlin.String**|  | [optional]
 **symbolId** | **kotlin.String**|  | [optional]
 **portfolio** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **apiOrder** | [**ApiOrder**](ApiOrder.md)| api_order | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="apiOrderPost"></a>
# **apiOrderPost**
> apiOrderPost(select, prefer, apiOrder)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiOrderApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val apiOrder : ApiOrder =  // ApiOrder | api_order
try {
    apiInstance.apiOrderPost(select, prefer, apiOrder)
} catch (e: ClientException) {
    println("4xx response calling ApiOrderApi#apiOrderPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiOrderApi#apiOrderPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **apiOrder** | [**ApiOrder**](ApiOrder.md)| api_order | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

