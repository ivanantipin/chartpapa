# DjangoMigrationsApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**djangoMigrationsDelete**](DjangoMigrationsApi.md#djangoMigrationsDelete) | **DELETE** /django_migrations | 
[**djangoMigrationsGet**](DjangoMigrationsApi.md#djangoMigrationsGet) | **GET** /django_migrations | 
[**djangoMigrationsPatch**](DjangoMigrationsApi.md#djangoMigrationsPatch) | **PATCH** /django_migrations | 
[**djangoMigrationsPost**](DjangoMigrationsApi.md#djangoMigrationsPost) | **POST** /django_migrations | 


<a name="djangoMigrationsDelete"></a>
# **djangoMigrationsDelete**
> djangoMigrationsDelete(id, app, name, applied, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoMigrationsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val app : kotlin.String = app_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val applied : kotlin.String = applied_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.djangoMigrationsDelete(id, app, name, applied, prefer)
} catch (e: ClientException) {
    println("4xx response calling DjangoMigrationsApi#djangoMigrationsDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoMigrationsApi#djangoMigrationsDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **app** | **kotlin.String**|  | [optional]
 **name** | **kotlin.String**|  | [optional]
 **applied** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="djangoMigrationsGet"></a>
# **djangoMigrationsGet**
> kotlin.Array&lt;DjangoMigrations&gt; djangoMigrationsGet(id, app, name, applied, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoMigrationsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val app : kotlin.String = app_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val applied : kotlin.String = applied_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<DjangoMigrations> = apiInstance.djangoMigrationsGet(id, app, name, applied, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DjangoMigrationsApi#djangoMigrationsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoMigrationsApi#djangoMigrationsGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **app** | **kotlin.String**|  | [optional]
 **name** | **kotlin.String**|  | [optional]
 **applied** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;DjangoMigrations&gt;**](DjangoMigrations.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="djangoMigrationsPatch"></a>
# **djangoMigrationsPatch**
> djangoMigrationsPatch(id, app, name, applied, prefer, djangoMigrations)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoMigrationsApi()
val id : kotlin.Int = id_example // kotlin.Int | 
val app : kotlin.String = app_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val applied : kotlin.String = applied_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val djangoMigrations : DjangoMigrations =  // DjangoMigrations | django_migrations
try {
    apiInstance.djangoMigrationsPatch(id, app, name, applied, prefer, djangoMigrations)
} catch (e: ClientException) {
    println("4xx response calling DjangoMigrationsApi#djangoMigrationsPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoMigrationsApi#djangoMigrationsPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.Int**|  | [optional]
 **app** | **kotlin.String**|  | [optional]
 **name** | **kotlin.String**|  | [optional]
 **applied** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **djangoMigrations** | [**DjangoMigrations**](DjangoMigrations.md)| django_migrations | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="djangoMigrationsPost"></a>
# **djangoMigrationsPost**
> djangoMigrationsPost(select, prefer, djangoMigrations)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = DjangoMigrationsApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val djangoMigrations : DjangoMigrations =  // DjangoMigrations | django_migrations
try {
    apiInstance.djangoMigrationsPost(select, prefer, djangoMigrations)
} catch (e: ClientException) {
    println("4xx response calling DjangoMigrationsApi#djangoMigrationsPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DjangoMigrationsApi#djangoMigrationsPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **djangoMigrations** | [**DjangoMigrations**](DjangoMigrations.md)| django_migrations | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

