# DayApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**dayDelete**](DayApi.md#dayDelete) | **DELETE** /day | 
[**dayGet**](DayApi.md#dayGet) | **GET** /day | 
[**dayPatch**](DayApi.md#dayPatch) | **PATCH** /day | 
[**dayPost**](DayApi.md#dayPost) | **POST** /day | 


<a name="dayDelete"></a>
# **dayDelete**
> dayDelete(id, symbol, open, high, low, close, volume, datetime, opendatetime, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DayApi()
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
    apiInstance.dayDelete(id, symbol, open, high, low, close, volume, datetime, opendatetime, prefer)
} catch (e: ClientException) {
    println("4xx response calling DayApi#dayDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DayApi#dayDelete")
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

<a name="dayGet"></a>
# **dayGet**
> kotlin.Array&lt;Day&gt; dayGet(id, symbol, open, high, low, close, volume, datetime, opendatetime, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DayApi()
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
    val result : kotlin.Array<Day> = apiInstance.dayGet(id, symbol, open, high, low, close, volume, datetime, opendatetime, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DayApi#dayGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DayApi#dayGet")
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

[**kotlin.Array&lt;Day&gt;**](Day.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="dayPatch"></a>
# **dayPatch**
> dayPatch(id, symbol, open, high, low, close, volume, datetime, opendatetime, prefer, day)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DayApi()
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
val day : Day =  // Day | day
try {
    apiInstance.dayPatch(id, symbol, open, high, low, close, volume, datetime, opendatetime, prefer, day)
} catch (e: ClientException) {
    println("4xx response calling DayApi#dayPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DayApi#dayPatch")
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
 **day** | [**Day**](Day.md)| day | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="dayPost"></a>
# **dayPost**
> dayPost(select, prefer, day)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DayApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val day : Day =  // Day | day
try {
    apiInstance.dayPost(select, prefer, day)
} catch (e: ClientException) {
    println("4xx response calling DayApi#dayPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DayApi#dayPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **day** | [**Day**](Day.md)| day | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

