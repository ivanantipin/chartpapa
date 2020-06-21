# DjangoAdminLogApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**djangoAdminLogDelete**](DjangoAdminLogApi.md#djangoAdminLogDelete) | **DELETE** /django_admin_log | 
[**djangoAdminLogGet**](DjangoAdminLogApi.md#djangoAdminLogGet) | **GET** /django_admin_log | 
[**djangoAdminLogPatch**](DjangoAdminLogApi.md#djangoAdminLogPatch) | **PATCH** /django_admin_log | 
[**djangoAdminLogPost**](DjangoAdminLogApi.md#djangoAdminLogPost) | **POST** /django_admin_log | 


<a name="djangoAdminLogDelete"></a>
# **djangoAdminLogDelete**
> djangoAdminLogDelete(id, actionTime, objectId, objectRepr, actionFlag, changeMessage, contentTypeId, userId, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoAdminLogApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val actionTime : kotlin.String = actionTime_example // kotlin.String | 
val objectId : kotlin.String = objectId_example // kotlin.String | 
val objectRepr : kotlin.String = objectRepr_example // kotlin.String | 
val actionFlag : kotlin.String = actionFlag_example // kotlin.String | 
val changeMessage : kotlin.String = changeMessage_example // kotlin.String | 
val contentTypeId : kotlin.Int = contentTypeId_example // kotlin.Int | 
val userId : kotlin.Int = userId_example // kotlin.Int | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.djangoAdminLogDelete(id, actionTime, objectId, objectRepr, actionFlag, changeMessage, contentTypeId, userId, prefer)
} catch (e: ClientException) {
    println("4xx response calling DjangoAdminLogApi#djangoAdminLogDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoAdminLogApi#djangoAdminLogDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **actionTime** | **kotlin.String**|  | [optional]
 **objectId** | **kotlin.String**|  | [optional]
 **objectRepr** | **kotlin.String**|  | [optional]
 **actionFlag** | **kotlin.String**|  | [optional]
 **changeMessage** | **kotlin.String**|  | [optional]
 **contentTypeId** | **kotlin.Int**|  | [optional]
 **userId** | **kotlin.Int**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="djangoAdminLogGet"></a>
# **djangoAdminLogGet**
> kotlin.Array&lt;DjangoAdminLog&gt; djangoAdminLogGet(id, actionTime, objectId, objectRepr, actionFlag, changeMessage, contentTypeId, userId, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoAdminLogApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val actionTime : kotlin.String = actionTime_example // kotlin.String | 
val objectId : kotlin.String = objectId_example // kotlin.String | 
val objectRepr : kotlin.String = objectRepr_example // kotlin.String | 
val actionFlag : kotlin.String = actionFlag_example // kotlin.String | 
val changeMessage : kotlin.String = changeMessage_example // kotlin.String | 
val contentTypeId : kotlin.Int = contentTypeId_example // kotlin.Int | 
val userId : kotlin.Int = userId_example // kotlin.Int | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<DjangoAdminLog> = apiInstance.djangoAdminLogGet(id, actionTime, objectId, objectRepr, actionFlag, changeMessage, contentTypeId, userId, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DjangoAdminLogApi#djangoAdminLogGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoAdminLogApi#djangoAdminLogGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **actionTime** | **kotlin.String**|  | [optional]
 **objectId** | **kotlin.String**|  | [optional]
 **objectRepr** | **kotlin.String**|  | [optional]
 **actionFlag** | **kotlin.String**|  | [optional]
 **changeMessage** | **kotlin.String**|  | [optional]
 **contentTypeId** | **kotlin.Int**|  | [optional]
 **userId** | **kotlin.Int**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;DjangoAdminLog&gt;**](DjangoAdminLog.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="djangoAdminLogPatch"></a>
# **djangoAdminLogPatch**
> djangoAdminLogPatch(id, actionTime, objectId, objectRepr, actionFlag, changeMessage, contentTypeId, userId, prefer, djangoAdminLog)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoAdminLogApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val actionTime : kotlin.String = actionTime_example // kotlin.String | 
val objectId : kotlin.String = objectId_example // kotlin.String | 
val objectRepr : kotlin.String = objectRepr_example // kotlin.String | 
val actionFlag : kotlin.String = actionFlag_example // kotlin.String | 
val changeMessage : kotlin.String = changeMessage_example // kotlin.String | 
val contentTypeId : kotlin.Int = contentTypeId_example // kotlin.Int | 
val userId : kotlin.Int = userId_example // kotlin.Int | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val djangoAdminLog : DjangoAdminLog =  // DjangoAdminLog | django_admin_log
try {
    apiInstance.djangoAdminLogPatch(id, actionTime, objectId, objectRepr, actionFlag, changeMessage, contentTypeId, userId, prefer, djangoAdminLog)
} catch (e: ClientException) {
    println("4xx response calling DjangoAdminLogApi#djangoAdminLogPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoAdminLogApi#djangoAdminLogPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **actionTime** | **kotlin.String**|  | [optional]
 **objectId** | **kotlin.String**|  | [optional]
 **objectRepr** | **kotlin.String**|  | [optional]
 **actionFlag** | **kotlin.String**|  | [optional]
 **changeMessage** | **kotlin.String**|  | [optional]
 **contentTypeId** | **kotlin.Int**|  | [optional]
 **userId** | **kotlin.Int**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **djangoAdminLog** | [**DjangoAdminLog**](DjangoAdminLog.md)| django_admin_log | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="djangoAdminLogPost"></a>
# **djangoAdminLogPost**
> djangoAdminLogPost(select, prefer, djangoAdminLog)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoAdminLogApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val djangoAdminLog : DjangoAdminLog =  // DjangoAdminLog | django_admin_log
try {
    apiInstance.djangoAdminLogPost(select, prefer, djangoAdminLog)
} catch (e: ClientException) {
    println("4xx response calling DjangoAdminLogApi#djangoAdminLogPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoAdminLogApi#djangoAdminLogPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **djangoAdminLog** | [**DjangoAdminLog**](DjangoAdminLog.md)| django_admin_log | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

