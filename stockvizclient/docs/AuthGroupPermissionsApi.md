# AuthGroupPermissionsApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authGroupPermissionsDelete**](AuthGroupPermissionsApi.md#authGroupPermissionsDelete) | **DELETE** /auth_group_permissions | 
[**authGroupPermissionsGet**](AuthGroupPermissionsApi.md#authGroupPermissionsGet) | **GET** /auth_group_permissions | 
[**authGroupPermissionsPatch**](AuthGroupPermissionsApi.md#authGroupPermissionsPatch) | **PATCH** /auth_group_permissions | 
[**authGroupPermissionsPost**](AuthGroupPermissionsApi.md#authGroupPermissionsPost) | **POST** /auth_group_permissions | 


<a name="authGroupPermissionsDelete"></a>
# **authGroupPermissionsDelete**
> authGroupPermissionsDelete(id, groupId, permissionId, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthGroupPermissionsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val groupId : kotlin.Int = groupId_example // kotlin.Int | 
val permissionId : kotlin.Int = permissionId_example // kotlin.Int | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.authGroupPermissionsDelete(id, groupId, permissionId, prefer)
} catch (e: ClientException) {
    println("4xx response calling AuthGroupPermissionsApi#authGroupPermissionsDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthGroupPermissionsApi#authGroupPermissionsDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **groupId** | **kotlin.Int**|  | [optional]
 **permissionId** | **kotlin.Int**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="authGroupPermissionsGet"></a>
# **authGroupPermissionsGet**
> kotlin.Array&lt;AuthGroupPermissions&gt; authGroupPermissionsGet(id, groupId, permissionId, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthGroupPermissionsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val groupId : kotlin.Int = groupId_example // kotlin.Int | 
val permissionId : kotlin.Int = permissionId_example // kotlin.Int | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<AuthGroupPermissions> = apiInstance.authGroupPermissionsGet(id, groupId, permissionId, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AuthGroupPermissionsApi#authGroupPermissionsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthGroupPermissionsApi#authGroupPermissionsGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **groupId** | **kotlin.Int**|  | [optional]
 **permissionId** | **kotlin.Int**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;AuthGroupPermissions&gt;**](AuthGroupPermissions.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="authGroupPermissionsPatch"></a>
# **authGroupPermissionsPatch**
> authGroupPermissionsPatch(id, groupId, permissionId, prefer, authGroupPermissions)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthGroupPermissionsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val groupId : kotlin.Int = groupId_example // kotlin.Int | 
val permissionId : kotlin.Int = permissionId_example // kotlin.Int | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authGroupPermissions : AuthGroupPermissions =  // AuthGroupPermissions | auth_group_permissions
try {
    apiInstance.authGroupPermissionsPatch(id, groupId, permissionId, prefer, authGroupPermissions)
} catch (e: ClientException) {
    println("4xx response calling AuthGroupPermissionsApi#authGroupPermissionsPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthGroupPermissionsApi#authGroupPermissionsPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **groupId** | **kotlin.Int**|  | [optional]
 **permissionId** | **kotlin.Int**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authGroupPermissions** | [**AuthGroupPermissions**](AuthGroupPermissions.md)| auth_group_permissions | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="authGroupPermissionsPost"></a>
# **authGroupPermissionsPost**
> authGroupPermissionsPost(select, prefer, authGroupPermissions)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthGroupPermissionsApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authGroupPermissions : AuthGroupPermissions =  // AuthGroupPermissions | auth_group_permissions
try {
    apiInstance.authGroupPermissionsPost(select, prefer, authGroupPermissions)
} catch (e: ClientException) {
    println("4xx response calling AuthGroupPermissionsApi#authGroupPermissionsPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthGroupPermissionsApi#authGroupPermissionsPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authGroupPermissions** | [**AuthGroupPermissions**](AuthGroupPermissions.md)| auth_group_permissions | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

