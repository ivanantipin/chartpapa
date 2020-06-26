# InstrumentsApi

All URIs are relative to *http://localhost:8000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**instrumentsAddCreate**](InstrumentsApi.md#instrumentsAddCreate) | **POST** /instruments/add | 
[**instrumentsList**](InstrumentsApi.md#instrumentsList) | **GET** /instruments/ | 


<a name="instrumentsAddCreate"></a>
# **instrumentsAddCreate**
> kotlin.Array&lt;NewInstrument&gt; instrumentsAddCreate(data)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = InstrumentsApi()
val data : kotlin.Array<NewInstrument> =  // kotlin.Array<NewInstrument> | 
try {
    val result : kotlin.Array<NewInstrument> = apiInstance.instrumentsAddCreate(data)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling InstrumentsApi#instrumentsAddCreate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling InstrumentsApi#instrumentsAddCreate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **data** | [**kotlin.Array&lt;NewInstrument&gt;**](NewInstrument.md)|  |

### Return type

[**kotlin.Array&lt;NewInstrument&gt;**](NewInstrument.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="instrumentsList"></a>
# **instrumentsList**
> kotlin.Array&lt;Instrument&gt; instrumentsList()



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = InstrumentsApi()
try {
    val result : kotlin.Array<Instrument> = apiInstance.instrumentsList()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling InstrumentsApi#instrumentsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling InstrumentsApi#instrumentsList")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Array&lt;Instrument&gt;**](Instrument.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

