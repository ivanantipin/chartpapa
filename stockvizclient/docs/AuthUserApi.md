# AuthUserApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**authUserDelete**](AuthUserApi.md#authUserDelete) | **DELETE** /auth_user | 
[**authUserGet**](AuthUserApi.md#authUserGet) | **GET** /auth_user | 
[**authUserPatch**](AuthUserApi.md#authUserPatch) | **PATCH** /auth_user | 
[**authUserPost**](AuthUserApi.md#authUserPost) | **POST** /auth_user | 


<a name="authUserDelete"></a>
# **authUserDelete**
> authUserDelete(id, password, lastLogin, isSuperuser, username, firstName, lastName, email, isStaff, isActive, dateJoined, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val password : kotlin.String = password_example // kotlin.String | 
val lastLogin : kotlin.String = lastLogin_example // kotlin.String | 
val isSuperuser : kotlin.Boolean = isSuperuser_example // kotlin.Boolean | 
val username : kotlin.String = username_example // kotlin.String | 
val firstName : kotlin.String = firstName_example // kotlin.String | 
val lastName : kotlin.String = lastName_example // kotlin.String | 
val email : kotlin.String = email_example // kotlin.String | 
val isStaff : kotlin.Boolean = isStaff_example // kotlin.Boolean | 
val isActive : kotlin.Boolean = isActive_example // kotlin.Boolean | 
val dateJoined : kotlin.String = dateJoined_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.authUserDelete(id, password, lastLogin, isSuperuser, username, firstName, lastName, email, isStaff, isActive, dateJoined, prefer)
} catch (e: ClientException) {
    println("4xx response calling AuthUserApi#authUserDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserApi#authUserDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **password** | **kotlin.String**|  | [optional]
 **lastLogin** | **kotlin.String**|  | [optional]
 **isSuperuser** | **kotlin.Boolean**|  | [optional]
 **username** | **kotlin.String**|  | [optional]
 **firstName** | **kotlin.String**|  | [optional]
 **lastName** | **kotlin.String**|  | [optional]
 **email** | **kotlin.String**|  | [optional]
 **isStaff** | **kotlin.Boolean**|  | [optional]
 **isActive** | **kotlin.Boolean**|  | [optional]
 **dateJoined** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="authUserGet"></a>
# **authUserGet**
> kotlin.Array&lt;AuthUser&gt; authUserGet(id, password, lastLogin, isSuperuser, username, firstName, lastName, email, isStaff, isActive, dateJoined, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val password : kotlin.String = password_example // kotlin.String | 
val lastLogin : kotlin.String = lastLogin_example // kotlin.String | 
val isSuperuser : kotlin.Boolean = isSuperuser_example // kotlin.Boolean | 
val username : kotlin.String = username_example // kotlin.String | 
val firstName : kotlin.String = firstName_example // kotlin.String | 
val lastName : kotlin.String = lastName_example // kotlin.String | 
val email : kotlin.String = email_example // kotlin.String | 
val isStaff : kotlin.Boolean = isStaff_example // kotlin.Boolean | 
val isActive : kotlin.Boolean = isActive_example // kotlin.Boolean | 
val dateJoined : kotlin.String = dateJoined_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<AuthUser> = apiInstance.authUserGet(id, password, lastLogin, isSuperuser, username, firstName, lastName, email, isStaff, isActive, dateJoined, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AuthUserApi#authUserGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserApi#authUserGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **password** | **kotlin.String**|  | [optional]
 **lastLogin** | **kotlin.String**|  | [optional]
 **isSuperuser** | **kotlin.Boolean**|  | [optional]
 **username** | **kotlin.String**|  | [optional]
 **firstName** | **kotlin.String**|  | [optional]
 **lastName** | **kotlin.String**|  | [optional]
 **email** | **kotlin.String**|  | [optional]
 **isStaff** | **kotlin.Boolean**|  | [optional]
 **isActive** | **kotlin.Boolean**|  | [optional]
 **dateJoined** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;AuthUser&gt;**](AuthUser.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="authUserPatch"></a>
# **authUserPatch**
> authUserPatch(id, password, lastLogin, isSuperuser, username, firstName, lastName, email, isStaff, isActive, dateJoined, prefer, authUser)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val password : kotlin.String = password_example // kotlin.String | 
val lastLogin : kotlin.String = lastLogin_example // kotlin.String | 
val isSuperuser : kotlin.Boolean = isSuperuser_example // kotlin.Boolean | 
val username : kotlin.String = username_example // kotlin.String | 
val firstName : kotlin.String = firstName_example // kotlin.String | 
val lastName : kotlin.String = lastName_example // kotlin.String | 
val email : kotlin.String = email_example // kotlin.String | 
val isStaff : kotlin.Boolean = isStaff_example // kotlin.Boolean | 
val isActive : kotlin.Boolean = isActive_example // kotlin.Boolean | 
val dateJoined : kotlin.String = dateJoined_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authUser : AuthUser =  // AuthUser | auth_user
try {
    apiInstance.authUserPatch(id, password, lastLogin, isSuperuser, username, firstName, lastName, email, isStaff, isActive, dateJoined, prefer, authUser)
} catch (e: ClientException) {
    println("4xx response calling AuthUserApi#authUserPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserApi#authUserPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **password** | **kotlin.String**|  | [optional]
 **lastLogin** | **kotlin.String**|  | [optional]
 **isSuperuser** | **kotlin.Boolean**|  | [optional]
 **username** | **kotlin.String**|  | [optional]
 **firstName** | **kotlin.String**|  | [optional]
 **lastName** | **kotlin.String**|  | [optional]
 **email** | **kotlin.String**|  | [optional]
 **isStaff** | **kotlin.Boolean**|  | [optional]
 **isActive** | **kotlin.Boolean**|  | [optional]
 **dateJoined** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authUser** | [**AuthUser**](AuthUser.md)| auth_user | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="authUserPost"></a>
# **authUserPost**
> authUserPost(select, prefer, authUser)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = AuthUserApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val authUser : AuthUser =  // AuthUser | auth_user
try {
    apiInstance.authUserPost(select, prefer, authUser)
} catch (e: ClientException) {
    println("4xx response calling AuthUserApi#authUserPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AuthUserApi#authUserPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **authUser** | [**AuthUser**](AuthUser.md)| auth_user | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

