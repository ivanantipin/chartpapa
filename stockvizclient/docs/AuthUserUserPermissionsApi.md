# AuthUserUserPermissionsApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authUserUserPermissionsDelete**](AuthUserUserPermissionsApi.md#authUserUserPermissionsDelete) | **DELETE** /auth_user_user_permissions | 
[**authUserUserPermissionsGet**](AuthUserUserPermissionsApi.md#authUserUserPermissionsGet) | **GET** /auth_user_user_permissions | 
[**authUserUserPermissionsPatch**](AuthUserUserPermissionsApi.md#authUserUserPermissionsPatch) | **PATCH** /auth_user_user_permissions | 
[**authUserUserPermissionsPost**](AuthUserUserPermissionsApi.md#authUserUserPermissionsPost) | **POST** /auth_user_user_permissions | 


<a name="authUserUserPermissionsDelete"></a>
# **authUserUserPermissionsDelete**
> authUserUserPermissionsDelete(id, userId, permissionId, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserUserPermissionsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val userId : kotlin.Int = userId_example // kotlin.Int | 
val permissionId : kotlin.Int = permissionId_example // kotlin.Int | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.authUserUserPermissionsDelete(id, userId, permissionId, prefer)
} catch (e: ClientException) {
    println("4xx response calling AuthUserUserPermissionsApi#authUserUserPermissionsDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserUserPermissionsApi#authUserUserPermissionsDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **userId** | **kotlin.Int**|  | [optional]
 **permissionId** | **kotlin.Int**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="authUserUserPermissionsGet"></a>
# **authUserUserPermissionsGet**
> kotlin.Array&lt;AuthUserUserPermissions&gt; authUserUserPermissionsGet(id, userId, permissionId, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserUserPermissionsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val userId : kotlin.Int = userId_example // kotlin.Int | 
val permissionId : kotlin.Int = permissionId_example // kotlin.Int | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<AuthUserUserPermissions> = apiInstance.authUserUserPermissionsGet(id, userId, permissionId, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AuthUserUserPermissionsApi#authUserUserPermissionsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserUserPermissionsApi#authUserUserPermissionsGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **userId** | **kotlin.Int**|  | [optional]
 **permissionId** | **kotlin.Int**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;AuthUserUserPermissions&gt;**](AuthUserUserPermissions.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="authUserUserPermissionsPatch"></a>
# **authUserUserPermissionsPatch**
> authUserUserPermissionsPatch(id, userId, permissionId, prefer, authUserUserPermissions)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserUserPermissionsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val userId : kotlin.Int = userId_example // kotlin.Int | 
val permissionId : kotlin.Int = permissionId_example // kotlin.Int | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authUserUserPermissions : AuthUserUserPermissions =  // AuthUserUserPermissions | auth_user_user_permissions
try {
    apiInstance.authUserUserPermissionsPatch(id, userId, permissionId, prefer, authUserUserPermissions)
} catch (e: ClientException) {
    println("4xx response calling AuthUserUserPermissionsApi#authUserUserPermissionsPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserUserPermissionsApi#authUserUserPermissionsPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **userId** | **kotlin.Int**|  | [optional]
 **permissionId** | **kotlin.Int**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authUserUserPermissions** | [**AuthUserUserPermissions**](AuthUserUserPermissions.md)| auth_user_user_permissions | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="authUserUserPermissionsPost"></a>
# **authUserUserPermissionsPost**
> authUserUserPermissionsPost(select, prefer, authUserUserPermissions)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserUserPermissionsApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authUserUserPermissions : AuthUserUserPermissions =  // AuthUserUserPermissions | auth_user_user_permissions
try {
    apiInstance.authUserUserPermissionsPost(select, prefer, authUserUserPermissions)
} catch (e: ClientException) {
    println("4xx response calling AuthUserUserPermissionsApi#authUserUserPermissionsPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserUserPermissionsApi#authUserUserPermissionsPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authUserUserPermissions** | [**AuthUserUserPermissions**](AuthUserUserPermissions.md)| auth_user_user_permissions | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

