# ApiTradeApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**apiTradeDelete**](ApiTradeApi.md#apiTradeDelete) | **DELETE** /api_trade | 
[**apiTradeGet**](ApiTradeApi.md#apiTradeGet) | **GET** /api_trade | 
[**apiTradePatch**](ApiTradeApi.md#apiTradePatch) | **PATCH** /api_trade | 
[**apiTradePost**](ApiTradeApi.md#apiTradePost) | **POST** /api_trade | 


<a name="apiTradeDelete"></a>
# **apiTradeDelete**
> apiTradeDelete(portfolio, tradeId, side, qty, openTime, closeTime, openPrice, closePrice, pnl, discreteTags, continuousTags, symbolId, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiTradeApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
val tradeId : kotlin.String = tradeId_example // kotlin.String | 
val side : kotlin.String = side_example // kotlin.String | 
val qty : kotlin.String = qty_example // kotlin.String | 
val openTime : kotlin.String = openTime_example // kotlin.String | 
val closeTime : kotlin.String = closeTime_example // kotlin.String | 
val openPrice : kotlin.String = openPrice_example // kotlin.String | 
val closePrice : kotlin.String = closePrice_example // kotlin.String | 
val pnl : kotlin.String = pnl_example // kotlin.String | 
val discreteTags : kotlin.String = discreteTags_example // kotlin.String | 
val continuousTags : kotlin.String = continuousTags_example // kotlin.String | 
val symbolId : kotlin.String = symbolId_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.apiTradeDelete(portfolio, tradeId, side, qty, openTime, closeTime, openPrice, closePrice, pnl, discreteTags, continuousTags, symbolId, prefer)
} catch (e: ClientException) {
    println("4xx response calling ApiTradeApi#apiTradeDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiTradeApi#apiTradeDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  | [optional]
 **tradeId** | **kotlin.String**|  | [optional]
 **side** | **kotlin.String**|  | [optional]
 **qty** | **kotlin.String**|  | [optional]
 **openTime** | **kotlin.String**|  | [optional]
 **closeTime** | **kotlin.String**|  | [optional]
 **openPrice** | **kotlin.String**|  | [optional]
 **closePrice** | **kotlin.String**|  | [optional]
 **pnl** | **kotlin.String**|  | [optional]
 **discreteTags** | **kotlin.String**|  | [optional]
 **continuousTags** | **kotlin.String**|  | [optional]
 **symbolId** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="apiTradeGet"></a>
# **apiTradeGet**
> kotlin.Array&lt;ApiTrade&gt; apiTradeGet(portfolio, tradeId, side, qty, openTime, closeTime, openPrice, closePrice, pnl, discreteTags, continuousTags, symbolId, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiTradeApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
val tradeId : kotlin.String = tradeId_example // kotlin.String | 
val side : kotlin.String = side_example // kotlin.String | 
val qty : kotlin.String = qty_example // kotlin.String | 
val openTime : kotlin.String = openTime_example // kotlin.String | 
val closeTime : kotlin.String = closeTime_example // kotlin.String | 
val openPrice : kotlin.String = openPrice_example // kotlin.String | 
val closePrice : kotlin.String = closePrice_example // kotlin.String | 
val pnl : kotlin.String = pnl_example // kotlin.String | 
val discreteTags : kotlin.String = discreteTags_example // kotlin.String | 
val continuousTags : kotlin.String = continuousTags_example // kotlin.String | 
val symbolId : kotlin.String = symbolId_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<ApiTrade> = apiInstance.apiTradeGet(portfolio, tradeId, side, qty, openTime, closeTime, openPrice, closePrice, pnl, discreteTags, continuousTags, symbolId, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ApiTradeApi#apiTradeGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiTradeApi#apiTradeGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  | [optional]
 **tradeId** | **kotlin.String**|  | [optional]
 **side** | **kotlin.String**|  | [optional]
 **qty** | **kotlin.String**|  | [optional]
 **openTime** | **kotlin.String**|  | [optional]
 **closeTime** | **kotlin.String**|  | [optional]
 **openPrice** | **kotlin.String**|  | [optional]
 **closePrice** | **kotlin.String**|  | [optional]
 **pnl** | **kotlin.String**|  | [optional]
 **discreteTags** | **kotlin.String**|  | [optional]
 **continuousTags** | **kotlin.String**|  | [optional]
 **symbolId** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;ApiTrade&gt;**](ApiTrade.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="apiTradePatch"></a>
# **apiTradePatch**
> apiTradePatch(portfolio, tradeId, side, qty, openTime, closeTime, openPrice, closePrice, pnl, discreteTags, continuousTags, symbolId, prefer, apiTrade)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiTradeApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
val tradeId : kotlin.String = tradeId_example // kotlin.String | 
val side : kotlin.String = side_example // kotlin.String | 
val qty : kotlin.String = qty_example // kotlin.String | 
val openTime : kotlin.String = openTime_example // kotlin.String | 
val closeTime : kotlin.String = closeTime_example // kotlin.String | 
val openPrice : kotlin.String = openPrice_example // kotlin.String | 
val closePrice : kotlin.String = closePrice_example // kotlin.String | 
val pnl : kotlin.String = pnl_example // kotlin.String | 
val discreteTags : kotlin.String = discreteTags_example // kotlin.String | 
val continuousTags : kotlin.String = continuousTags_example // kotlin.String | 
val symbolId : kotlin.String = symbolId_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val apiTrade : ApiTrade =  // ApiTrade | api_trade
try {
    apiInstance.apiTradePatch(portfolio, tradeId, side, qty, openTime, closeTime, openPrice, closePrice, pnl, discreteTags, continuousTags, symbolId, prefer, apiTrade)
} catch (e: ClientException) {
    println("4xx response calling ApiTradeApi#apiTradePatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiTradeApi#apiTradePatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  | [optional]
 **tradeId** | **kotlin.String**|  | [optional]
 **side** | **kotlin.String**|  | [optional]
 **qty** | **kotlin.String**|  | [optional]
 **openTime** | **kotlin.String**|  | [optional]
 **closeTime** | **kotlin.String**|  | [optional]
 **openPrice** | **kotlin.String**|  | [optional]
 **closePrice** | **kotlin.String**|  | [optional]
 **pnl** | **kotlin.String**|  | [optional]
 **discreteTags** | **kotlin.String**|  | [optional]
 **continuousTags** | **kotlin.String**|  | [optional]
 **symbolId** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **apiTrade** | [**ApiTrade**](ApiTrade.md)| api_trade | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="apiTradePost"></a>
# **apiTradePost**
> apiTradePost(select, prefer, apiTrade)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiTradeApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val apiTrade : ApiTrade =  // ApiTrade | api_trade
try {
    apiInstance.apiTradePost(select, prefer, apiTrade)
} catch (e: ClientException) {
    println("4xx response calling ApiTradeApi#apiTradePost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiTradeApi#apiTradePost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **apiTrade** | [**ApiTrade**](ApiTrade.md)| api_trade | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

