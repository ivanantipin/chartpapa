# AuthGroupApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authGroupDelete**](AuthGroupApi.md#authGroupDelete) | **DELETE** /auth_group | 
[**authGroupGet**](AuthGroupApi.md#authGroupGet) | **GET** /auth_group | 
[**authGroupPatch**](AuthGroupApi.md#authGroupPatch) | **PATCH** /auth_group | 
[**authGroupPost**](AuthGroupApi.md#authGroupPost) | **POST** /auth_group | 


<a name="authGroupDelete"></a>
# **authGroupDelete**
> authGroupDelete(id, name, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthGroupApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val name : kotlin.String = name_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.authGroupDelete(id, name, prefer)
} catch (e: ClientException) {
    println("4xx response calling AuthGroupApi#authGroupDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthGroupApi#authGroupDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **name** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="authGroupGet"></a>
# **authGroupGet**
> kotlin.Array&lt;AuthGroup&gt; authGroupGet(id, name, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthGroupApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val name : kotlin.String = name_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<AuthGroup> = apiInstance.authGroupGet(id, name, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AuthGroupApi#authGroupGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthGroupApi#authGroupGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **name** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;AuthGroup&gt;**](AuthGroup.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="authGroupPatch"></a>
# **authGroupPatch**
> authGroupPatch(id, name, prefer, authGroup)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthGroupApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val name : kotlin.String = name_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authGroup : AuthGroup =  // AuthGroup | auth_group
try {
    apiInstance.authGroupPatch(id, name, prefer, authGroup)
} catch (e: ClientException) {
    println("4xx response calling AuthGroupApi#authGroupPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthGroupApi#authGroupPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **name** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authGroup** | [**AuthGroup**](AuthGroup.md)| auth_group | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="authGroupPost"></a>
# **authGroupPost**
> authGroupPost(select, prefer, authGroup)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthGroupApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authGroup : AuthGroup =  // AuthGroup | auth_group
try {
    apiInstance.authGroupPost(select, prefer, authGroup)
} catch (e: ClientException) {
    println("4xx response calling AuthGroupApi#authGroupPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthGroupApi#authGroupPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authGroup** | [**AuthGroup**](AuthGroup.md)| auth_group | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

