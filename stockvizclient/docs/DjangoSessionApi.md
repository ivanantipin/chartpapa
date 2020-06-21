# DjangoSessionApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**djangoSessionDelete**](DjangoSessionApi.md#djangoSessionDelete) | **DELETE** /django_session | 
[**djangoSessionGet**](DjangoSessionApi.md#djangoSessionGet) | **GET** /django_session | 
[**djangoSessionPatch**](DjangoSessionApi.md#djangoSessionPatch) | **PATCH** /django_session | 
[**djangoSessionPost**](DjangoSessionApi.md#djangoSessionPost) | **POST** /django_session | 


<a name="djangoSessionDelete"></a>
# **djangoSessionDelete**
> djangoSessionDelete(sessionKey, sessionData, expireDate, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoSessionApi()
val sessionKey : kotlin.String = sessionKey_example // kotlin.String | 
val sessionData : kotlin.String = sessionData_example // kotlin.String | 
val expireDate : kotlin.String = expireDate_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.djangoSessionDelete(sessionKey, sessionData, expireDate, prefer)
} catch (e: ClientException) {
    println("4xx response calling DjangoSessionApi#djangoSessionDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoSessionApi#djangoSessionDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sessionKey** | **kotlin.String**|  | [optional]
 **sessionData** | **kotlin.String**|  | [optional]
 **expireDate** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="djangoSessionGet"></a>
# **djangoSessionGet**
> kotlin.Array&lt;DjangoSession&gt; djangoSessionGet(sessionKey, sessionData, expireDate, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoSessionApi()
val sessionKey : kotlin.String = sessionKey_example // kotlin.String | 
val sessionData : kotlin.String = sessionData_example // kotlin.String | 
val expireDate : kotlin.String = expireDate_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<DjangoSession> = apiInstance.djangoSessionGet(sessionKey, sessionData, expireDate, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DjangoSessionApi#djangoSessionGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoSessionApi#djangoSessionGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sessionKey** | **kotlin.String**|  | [optional]
 **sessionData** | **kotlin.String**|  | [optional]
 **expireDate** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;DjangoSession&gt;**](DjangoSession.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="djangoSessionPatch"></a>
# **djangoSessionPatch**
> djangoSessionPatch(sessionKey, sessionData, expireDate, prefer, djangoSession)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoSessionApi()
val sessionKey : kotlin.String = sessionKey_example // kotlin.String | 
val sessionData : kotlin.String = sessionData_example // kotlin.String | 
val expireDate : kotlin.String = expireDate_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val djangoSession : DjangoSession =  // DjangoSession | django_session
try {
    apiInstance.djangoSessionPatch(sessionKey, sessionData, expireDate, prefer, djangoSession)
} catch (e: ClientException) {
    println("4xx response calling DjangoSessionApi#djangoSessionPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoSessionApi#djangoSessionPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sessionKey** | **kotlin.String**|  | [optional]
 **sessionData** | **kotlin.String**|  | [optional]
 **expireDate** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **djangoSession** | [**DjangoSession**](DjangoSession.md)| django_session | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="djangoSessionPost"></a>
# **djangoSessionPost**
> djangoSessionPost(select, prefer, djangoSession)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoSessionApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val djangoSession : DjangoSession =  // DjangoSession | django_session
try {
    apiInstance.djangoSessionPost(select, prefer, djangoSession)
} catch (e: ClientException) {
    println("4xx response calling DjangoSessionApi#djangoSessionPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoSessionApi#djangoSessionPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **djangoSession** | [**DjangoSession**](DjangoSession.md)| django_session | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

