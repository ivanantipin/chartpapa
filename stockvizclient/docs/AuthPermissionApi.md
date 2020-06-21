# AuthPermissionApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authPermissionDelete**](AuthPermissionApi.md#authPermissionDelete) | **DELETE** /auth_permission | 
[**authPermissionGet**](AuthPermissionApi.md#authPermissionGet) | **GET** /auth_permission | 
[**authPermissionPatch**](AuthPermissionApi.md#authPermissionPatch) | **PATCH** /auth_permission | 
[**authPermissionPost**](AuthPermissionApi.md#authPermissionPost) | **POST** /auth_permission | 


<a name="authPermissionDelete"></a>
# **authPermissionDelete**
> authPermissionDelete(id, name, contentTypeId, codename, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthPermissionApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val name : kotlin.String = name_example // kotlin.String | 
val contentTypeId : kotlin.Int = contentTypeId_example // kotlin.Int | 
val codename : kotlin.String = codename_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.authPermissionDelete(id, name, contentTypeId, codename, prefer)
} catch (e: ClientException) {
    println("4xx response calling AuthPermissionApi#authPermissionDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthPermissionApi#authPermissionDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **name** | **kotlin.String**|  | [optional]
 **contentTypeId** | **kotlin.Int**|  | [optional]
 **codename** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="authPermissionGet"></a>
# **authPermissionGet**
> kotlin.Array&lt;AuthPermission&gt; authPermissionGet(id, name, contentTypeId, codename, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthPermissionApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val name : kotlin.String = name_example // kotlin.String | 
val contentTypeId : kotlin.Int = contentTypeId_example // kotlin.Int | 
val codename : kotlin.String = codename_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<AuthPermission> = apiInstance.authPermissionGet(id, name, contentTypeId, codename, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AuthPermissionApi#authPermissionGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthPermissionApi#authPermissionGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **name** | **kotlin.String**|  | [optional]
 **contentTypeId** | **kotlin.Int**|  | [optional]
 **codename** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;AuthPermission&gt;**](AuthPermission.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="authPermissionPatch"></a>
# **authPermissionPatch**
> authPermissionPatch(id, name, contentTypeId, codename, prefer, authPermission)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthPermissionApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val name : kotlin.String = name_example // kotlin.String | 
val contentTypeId : kotlin.Int = contentTypeId_example // kotlin.Int | 
val codename : kotlin.String = codename_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authPermission : AuthPermission =  // AuthPermission | auth_permission
try {
    apiInstance.authPermissionPatch(id, name, contentTypeId, codename, prefer, authPermission)
} catch (e: ClientException) {
    println("4xx response calling AuthPermissionApi#authPermissionPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthPermissionApi#authPermissionPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **name** | **kotlin.String**|  | [optional]
 **contentTypeId** | **kotlin.Int**|  | [optional]
 **codename** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authPermission** | [**AuthPermission**](AuthPermission.md)| auth_permission | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="authPermissionPost"></a>
# **authPermissionPost**
> authPermissionPost(select, prefer, authPermission)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthPermissionApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authPermission : AuthPermission =  // AuthPermission | auth_permission
try {
    apiInstance.authPermissionPost(select, prefer, authPermission)
} catch (e: ClientException) {
    println("4xx response calling AuthPermissionApi#authPermissionPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthPermissionApi#authPermissionPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authPermission** | [**AuthPermission**](AuthPermission.md)| auth_permission | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

