# IntradayApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**intradayDelete**](IntradayApi.md#intradayDelete) | **DELETE** /intraday | 
[**intradayGet**](IntradayApi.md#intradayGet) | **GET** /intraday | 
[**intradayPatch**](IntradayApi.md#intradayPatch) | **PATCH** /intraday | 
[**intradayPost**](IntradayApi.md#intradayPost) | **POST** /intraday | 


<a name="intradayDelete"></a>
# **intradayDelete**
> intradayDelete(id, symbol, open, high, low, close, volume, datetime, opendatetime, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = IntradayApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val symbol : kotlin.String = symbol_example // kotlin.String | 
val open : kotlin.String = open_example // kotlin.String | 
val high : kotlin.String = high_example // kotlin.String | 
val low : kotlin.String = low_example // kotlin.String | 
val close : kotlin.String = close_example // kotlin.String | 
val volume : kotlin.String = volume_example // kotlin.String | 
val datetime : kotlin.String = datetime_example // kotlin.String | 
val opendatetime : kotlin.String = opendatetime_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.intradayDelete(id, symbol, open, high, low, close, volume, datetime, opendatetime, prefer)
} catch (e: ClientException) {
    println("4xx response calling IntradayApi#intradayDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling IntradayApi#intradayDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **symbol** | **kotlin.String**|  | [optional]
 **open** | **kotlin.String**|  | [optional]
 **high** | **kotlin.String**|  | [optional]
 **low** | **kotlin.String**|  | [optional]
 **close** | **kotlin.String**|  | [optional]
 **volume** | **kotlin.String**|  | [optional]
 **datetime** | **kotlin.String**|  | [optional]
 **opendatetime** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="intradayGet"></a>
# **intradayGet**
> kotlin.Array&lt;Intraday&gt; intradayGet(id, symbol, open, high, low, close, volume, datetime, opendatetime, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = IntradayApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val symbol : kotlin.String = symbol_example // kotlin.String | 
val open : kotlin.String = open_example // kotlin.String | 
val high : kotlin.String = high_example // kotlin.String | 
val low : kotlin.String = low_example // kotlin.String | 
val close : kotlin.String = close_example // kotlin.String | 
val volume : kotlin.String = volume_example // kotlin.String | 
val datetime : kotlin.String = datetime_example // kotlin.String | 
val opendatetime : kotlin.String = opendatetime_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<Intraday> = apiInstance.intradayGet(id, symbol, open, high, low, close, volume, datetime, opendatetime, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling IntradayApi#intradayGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling IntradayApi#intradayGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **symbol** | **kotlin.String**|  | [optional]
 **open** | **kotlin.String**|  | [optional]
 **high** | **kotlin.String**|  | [optional]
 **low** | **kotlin.String**|  | [optional]
 **close** | **kotlin.String**|  | [optional]
 **volume** | **kotlin.String**|  | [optional]
 **datetime** | **kotlin.String**|  | [optional]
 **opendatetime** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;Intraday&gt;**](Intraday.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="intradayPatch"></a>
# **intradayPatch**
> intradayPatch(id, symbol, open, high, low, close, volume, datetime, opendatetime, prefer, intraday)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = IntradayApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val symbol : kotlin.String = symbol_example // kotlin.String | 
val open : kotlin.String = open_example // kotlin.String | 
val high : kotlin.String = high_example // kotlin.String | 
val low : kotlin.String = low_example // kotlin.String | 
val close : kotlin.String = close_example // kotlin.String | 
val volume : kotlin.String = volume_example // kotlin.String | 
val datetime : kotlin.String = datetime_example // kotlin.String | 
val opendatetime : kotlin.String = opendatetime_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val intraday : Intraday =  // Intraday | intraday
try {
    apiInstance.intradayPatch(id, symbol, open, high, low, close, volume, datetime, opendatetime, prefer, intraday)
} catch (e: ClientException) {
    println("4xx response calling IntradayApi#intradayPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling IntradayApi#intradayPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **symbol** | **kotlin.String**|  | [optional]
 **open** | **kotlin.String**|  | [optional]
 **high** | **kotlin.String**|  | [optional]
 **low** | **kotlin.String**|  | [optional]
 **close** | **kotlin.String**|  | [optional]
 **volume** | **kotlin.String**|  | [optional]
 **datetime** | **kotlin.String**|  | [optional]
 **opendatetime** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **intraday** | [**Intraday**](Intraday.md)| intraday | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="intradayPost"></a>
# **intradayPost**
> intradayPost(select, prefer, intraday)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = IntradayApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val intraday : Intraday =  // Intraday | intraday
try {
    apiInstance.intradayPost(select, prefer, intraday)
} catch (e: ClientException) {
    println("4xx response calling IntradayApi#intradayPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling IntradayApi#intradayPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **intraday** | [**Intraday**](Intraday.md)| intraday | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

