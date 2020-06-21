# IntrospectionApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**rootGet**](IntrospectionApi.md#rootGet) | **GET** / | OpenAPI description (this document)


<a name="rootGet"></a>
# **rootGet**
> rootGet()

OpenAPI description (this document)

### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = IntrospectionApi()
try {
    apiInstance.rootGet()
} catch (e: ClientException) {
    println("4xx response calling IntrospectionApi#rootGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling IntrospectionApi#rootGet")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

