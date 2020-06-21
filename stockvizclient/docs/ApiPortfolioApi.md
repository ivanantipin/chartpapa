# ApiPortfolioApi

All URIs are relative to *http://0.0.0.0:3001*

Method | HTTP request | Description
------------- | ------------- | -------------
[**apiPortfolioDelete**](ApiPortfolioApi.md#apiPortfolioDelete) | **DELETE** /api_portfolio | 
[**apiPortfolioGet**](ApiPortfolioApi.md#apiPortfolioGet) | **GET** /api_portfolio | 
[**apiPortfolioPatch**](ApiPortfolioApi.md#apiPortfolioPatch) | **PATCH** /api_portfolio | 
[**apiPortfolioPost**](ApiPortfolioApi.md#apiPortfolioPost) | **POST** /api_portfolio | 


<a name="apiPortfolioDelete"></a>
# **apiPortfolioDelete**
> apiPortfolioDelete(name, description, createdDate, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiPortfolioApi()
val name : kotlin.String = name_example // kotlin.String | 
val description : kotlin.String = description_example // kotlin.String | 
val createdDate : kotlin.String = createdDate_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    apiInstance.apiPortfolioDelete(name, description, createdDate, prefer)
} catch (e: ClientException) {
    println("4xx response calling ApiPortfolioApi#apiPortfolioDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiPortfolioApi#apiPortfolioDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **kotlin.String**|  | [optional]
 **description** | **kotlin.String**|  | [optional]
 **createdDate** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="apiPortfolioGet"></a>
# **apiPortfolioGet**
> kotlin.Array&lt;ApiPortfolio&gt; apiPortfolioGet(name, description, createdDate, select, order, range, rangeMinusUnit, offset, limit, prefer)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiPortfolioApi()
val name : kotlin.String = name_example // kotlin.String | 
val description : kotlin.String = description_example // kotlin.String | 
val createdDate : kotlin.String = createdDate_example // kotlin.String | 
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val order : kotlin.String = order_example // kotlin.String | Ordering
val range : kotlin.String = range_example // kotlin.String | Limiting and Pagination
val rangeMinusUnit : kotlin.String = rangeMinusUnit_example // kotlin.String | Limiting and Pagination
val offset : kotlin.String = offset_example // kotlin.String | Limiting and Pagination
val limit : kotlin.String = limit_example // kotlin.String | Limiting and Pagination
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
try {
    val result : kotlin.Array<ApiPortfolio> = apiInstance.apiPortfolioGet(name, description, createdDate, select, order, range, rangeMinusUnit, offset, limit, prefer)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ApiPortfolioApi#apiPortfolioGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiPortfolioApi#apiPortfolioGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **kotlin.String**|  | [optional]
 **description** | **kotlin.String**|  | [optional]
 **createdDate** | **kotlin.String**|  | [optional]
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **order** | **kotlin.String**| Ordering | [optional]
 **range** | **kotlin.String**| Limiting and Pagination | [optional]
 **rangeMinusUnit** | **kotlin.String**| Limiting and Pagination | [optional] [default to &quot;items&quot;]
 **offset** | **kotlin.String**| Limiting and Pagination | [optional]
 **limit** | **kotlin.String**| Limiting and Pagination | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: count=none]

### Return type

[**kotlin.Array&lt;ApiPortfolio&gt;**](ApiPortfolio.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/vnd.pgrst.object+json, text/csv

<a name="apiPortfolioPatch"></a>
# **apiPortfolioPatch**
> apiPortfolioPatch(name, description, createdDate, prefer, apiPortfolio)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiPortfolioApi()
val name : kotlin.String = name_example // kotlin.String | 
val description : kotlin.String = description_example // kotlin.String | 
val createdDate : kotlin.String = createdDate_example // kotlin.String | 
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val apiPortfolio : ApiPortfolio =  // ApiPortfolio | api_portfolio
try {
    apiInstance.apiPortfolioPatch(name, description, createdDate, prefer, apiPortfolio)
} catch (e: ClientException) {
    println("4xx response calling ApiPortfolioApi#apiPortfolioPatch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiPortfolioApi#apiPortfolioPatch")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **kotlin.String**|  | [optional]
 **description** | **kotlin.String**|  | [optional]
 **createdDate** | **kotlin.String**|  | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **apiPortfolio** | [**ApiPortfolio**](ApiPortfolio.md)| api_portfolio | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

<a name="apiPortfolioPost"></a>
# **apiPortfolioPost**
> apiPortfolioPost(select, prefer, apiPortfolio)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = ApiPortfolioApi()
val select : kotlin.String = select_example // kotlin.String | Filtering Columns
val prefer : kotlin.String = prefer_example // kotlin.String | Preference
val apiPortfolio : ApiPortfolio =  // ApiPortfolio | api_portfolio
try {
    apiInstance.apiPortfolioPost(select, prefer, apiPortfolio)
} catch (e: ClientException) {
    println("4xx response calling ApiPortfolioApi#apiPortfolioPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiPortfolioApi#apiPortfolioPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **select** | **kotlin.String**| Filtering Columns | [optional]
 **prefer** | **kotlin.String**| Preference | [optional] [enum: return=representation, return=minimal, return=none]
 **apiPortfolio** | [**ApiPortfolio**](ApiPortfolio.md)| api_portfolio | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/vnd.pgrst.object+json, text/csv
 - **Accept**: Not defined

