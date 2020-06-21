# DjangoContentTypeApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**djangoContentTypeDelete**](DjangoContentTypeApi.md#djangoContentTypeDelete) | **DELETE** /django_content_type | 
[**djangoContentTypeGet**](DjangoContentTypeApi.md#djangoContentTypeGet) | **GET** /django_content_type | 
[**djangoContentTypePatch**](DjangoContentTypeApi.md#djangoContentTypePatch) | **PATCH** /django_content_type | 
[**djangoContentTypePost**](DjangoContentTypeApi.md#djangoContentTypePost) | **POST** /django_content_type | 


<a name="djangoContentTypeDelete"></a>
# **djangoContentTypeDelete**
> djangoContentTypeDelete(id, appLabel, model, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoContentTypeApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val appLabel : kotlin.String = appLabel_example // kotlin.String | 
val model : kotlin.String = model_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.djangoContentTypeDelete(id, appLabel, model, prefer)
} catch (e: ClientException) {
    println("4xx response calling DjangoContentTypeApi#djangoContentTypeDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoContentTypeApi#djangoContentTypeDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **appLabel** | **kotlin.String**|  | [optional]
 **model** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="djangoContentTypeGet"></a>
# **djangoContentTypeGet**
> kotlin.Array&lt;DjangoContentType&gt; djangoContentTypeGet(id, appLabel, model, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoContentTypeApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val appLabel : kotlin.String = appLabel_example // kotlin.String | 
val model : kotlin.String = model_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<DjangoContentType> = apiInstance.djangoContentTypeGet(id, appLabel, model, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DjangoContentTypeApi#djangoContentTypeGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoContentTypeApi#djangoContentTypeGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **appLabel** | **kotlin.String**|  | [optional]
 **model** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;DjangoContentType&gt;**](DjangoContentType.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="djangoContentTypePatch"></a>
# **djangoContentTypePatch**
> djangoContentTypePatch(id, appLabel, model, prefer, djangoContentType)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoContentTypeApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val appLabel : kotlin.String = appLabel_example // kotlin.String | 
val model : kotlin.String = model_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val djangoContentType : DjangoContentType =  // DjangoContentType | django_content_type
try {
    apiInstance.djangoContentTypePatch(id, appLabel, model, prefer, djangoContentType)
} catch (e: ClientException) {
    println("4xx response calling DjangoContentTypeApi#djangoContentTypePatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoContentTypeApi#djangoContentTypePatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **appLabel** | **kotlin.String**|  | [optional]
 **model** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **djangoContentType** | [**DjangoContentType**](DjangoContentType.md)| django_content_type | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="djangoContentTypePost"></a>
# **djangoContentTypePost**
> djangoContentTypePost(select, prefer, djangoContentType)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoContentTypeApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val djangoContentType : DjangoContentType =  // DjangoContentType | django_content_type
try {
    apiInstance.djangoContentTypePost(select, prefer, djangoContentType)
} catch (e: ClientException) {
    println("4xx response calling DjangoContentTypeApi#djangoContentTypePost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoContentTypeApi#djangoContentTypePost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **djangoContentType** | [**DjangoContentType**](DjangoContentType.md)| django_content_type | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

