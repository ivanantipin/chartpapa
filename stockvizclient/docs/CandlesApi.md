# CandlesApi

All URIs are relative to *http://localhost:8000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**candlesRead**](CandlesApi.md#candlesRead) | **GET** /candles/{timeframe}/{symbol}/ | 


<a name="candlesRead"></a>
# **candlesRead**
> kotlin.Array&lt;Candle&gt; candlesRead(symbol, timeframe, fromDate, toDate)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = CandlesApi()
val symbol : kotlin.String = symbol_example // kotlin.String | 
val timeframe : kotlin.String = timeframe_example // kotlin.String | 
val fromDate : java.time.LocalDate = 2013-10-20 // java.time.LocalDate | filter by date gte
val toDate : kotlin.String = toDate_example // kotlin.String | filter by date lte
try {
    val result : kotlin.Array<Candle> = apiInstance.candlesRead(symbol, timeframe, fromDate, toDate)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CandlesApi#candlesRead")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CandlesApi#candlesRead")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **symbol** | **kotlin.String**|  |
 **timeframe** | **kotlin.String**|  |
 **fromDate** | **java.time.LocalDate**| filter by date gte | [optional]
 **toDate** | **kotlin.String**| filter by date lte | [optional]

### Return type

[**kotlin.Array&lt;Candle&gt;**](Candle.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

