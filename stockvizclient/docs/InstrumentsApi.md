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
> InlineResponse200 instrumentsList(page, pageSize)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = InstrumentsApi()
val page : kotlin.Int = 56 // kotlin.Int | A page number within the paginated result set.
val pageSize : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
try {
    val result : InlineResponse200 = apiInstance.instrumentsList(page, pageSize)
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

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **page** | **kotlin.Int**| A page number within the paginated result set. | [optional]
 **pageSize** | **kotlin.Int**| Number of results to return per page. | [optional]

### Return type

[**InlineResponse200**](InlineResponse200.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

