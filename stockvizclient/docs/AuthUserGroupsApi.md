# AuthUserGroupsApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authUserGroupsDelete**](AuthUserGroupsApi.md#authUserGroupsDelete) | **DELETE** /auth_user_groups | 
[**authUserGroupsGet**](AuthUserGroupsApi.md#authUserGroupsGet) | **GET** /auth_user_groups | 
[**authUserGroupsPatch**](AuthUserGroupsApi.md#authUserGroupsPatch) | **PATCH** /auth_user_groups | 
[**authUserGroupsPost**](AuthUserGroupsApi.md#authUserGroupsPost) | **POST** /auth_user_groups | 


<a name="authUserGroupsDelete"></a>
# **authUserGroupsDelete**
> authUserGroupsDelete(id, userId, groupId, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserGroupsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val userId : kotlin.Int = userId_example // kotlin.Int | 
val groupId : kotlin.Int = groupId_example // kotlin.Int | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.authUserGroupsDelete(id, userId, groupId, prefer)
} catch (e: ClientException) {
    println("4xx response calling AuthUserGroupsApi#authUserGroupsDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserGroupsApi#authUserGroupsDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **userId** | **kotlin.Int**|  | [optional]
 **groupId** | **kotlin.Int**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="authUserGroupsGet"></a>
# **authUserGroupsGet**
> kotlin.Array&lt;AuthUserGroups&gt; authUserGroupsGet(id, userId, groupId, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserGroupsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val userId : kotlin.Int = userId_example // kotlin.Int | 
val groupId : kotlin.Int = groupId_example // kotlin.Int | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<AuthUserGroups> = apiInstance.authUserGroupsGet(id, userId, groupId, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AuthUserGroupsApi#authUserGroupsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserGroupsApi#authUserGroupsGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **userId** | **kotlin.Int**|  | [optional]
 **groupId** | **kotlin.Int**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;AuthUserGroups&gt;**](AuthUserGroups.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="authUserGroupsPatch"></a>
# **authUserGroupsPatch**
> authUserGroupsPatch(id, userId, groupId, prefer, authUserGroups)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserGroupsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val userId : kotlin.Int = userId_example // kotlin.Int | 
val groupId : kotlin.Int = groupId_example // kotlin.Int | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authUserGroups : AuthUserGroups =  // AuthUserGroups | auth_user_groups
try {
    apiInstance.authUserGroupsPatch(id, userId, groupId, prefer, authUserGroups)
} catch (e: ClientException) {
    println("4xx response calling AuthUserGroupsApi#authUserGroupsPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserGroupsApi#authUserGroupsPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **userId** | **kotlin.Int**|  | [optional]
 **groupId** | **kotlin.Int**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authUserGroups** | [**AuthUserGroups**](AuthUserGroups.md)| auth_user_groups | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="authUserGroupsPost"></a>
# **authUserGroupsPost**
> authUserGroupsPost(select, prefer, authUserGroups)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserGroupsApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authUserGroups : AuthUserGroups =  // AuthUserGroups | auth_user_groups
try {
    apiInstance.authUserGroupsPost(select, prefer, authUserGroups)
} catch (e: ClientException) {
    println("4xx response calling AuthUserGroupsApi#authUserGroupsPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserGroupsApi#authUserGroupsPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authUserGroups** | [**AuthUserGroups**](AuthUserGroups.md)| auth_user_groups | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

