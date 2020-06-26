# PortfoliosApi

All URIs are relative to *http://localhost:8000/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**portfoliosAddOrdersCreate**](PortfoliosApi.md#portfoliosAddOrdersCreate) | **POST** /portfolios/{portfolio}/add/orders/ | 
[**portfoliosAddTradesCreate**](PortfoliosApi.md#portfoliosAddTradesCreate) | **POST** /portfolios/{portfolio}/add/trades/ | 
[**portfoliosAvailableInstrumentsMetaList**](PortfoliosApi.md#portfoliosAvailableInstrumentsMetaList) | **GET** /portfolios/{portfolio}/available-instruments-meta/ | 
[**portfoliosAvailableTagsList**](PortfoliosApi.md#portfoliosAvailableTagsList) | **GET** /portfolios/{portfolio}/available-tags/ | 
[**portfoliosClearCreate**](PortfoliosApi.md#portfoliosClearCreate) | **POST** /portfolios/{portfolio}/clear/ | 
[**portfoliosCreateCreate**](PortfoliosApi.md#portfoliosCreateCreate) | **POST** /portfolios/create/ | 
[**portfoliosDeleteDelete**](PortfoliosApi.md#portfoliosDeleteDelete) | **DELETE** /portfolios/{portfolio}/delete/ | 
[**portfoliosList**](PortfoliosApi.md#portfoliosList) | **GET** /portfolios/ | 
[**portfoliosOrdersList**](PortfoliosApi.md#portfoliosOrdersList) | **GET** /portfolios/{portfolio}/orders/ | 
[**portfoliosTradesList**](PortfoliosApi.md#portfoliosTradesList) | **GET** /portfolios/{portfolio}/trades/ | 


<a name="portfoliosAddOrdersCreate"></a>
# **portfoliosAddOrdersCreate**
> AddResponse portfoliosAddOrdersCreate(portfolio, data)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
val data : kotlin.Array<NewOrder> =  // kotlin.Array<NewOrder> | 
try {
    val result : AddResponse = apiInstance.portfoliosAddOrdersCreate(portfolio, data)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosAddOrdersCreate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosAddOrdersCreate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  |
 **data** | [**kotlin.Array&lt;NewOrder&gt;**](NewOrder.md)|  |

### Return type

[**AddResponse**](AddResponse.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="portfoliosAddTradesCreate"></a>
# **portfoliosAddTradesCreate**
> AddResponse portfoliosAddTradesCreate(portfolio, data)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
val data : kotlin.Array<NewTrade> =  // kotlin.Array<NewTrade> | 
try {
    val result : AddResponse = apiInstance.portfoliosAddTradesCreate(portfolio, data)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosAddTradesCreate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosAddTradesCreate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  |
 **data** | [**kotlin.Array&lt;NewTrade&gt;**](NewTrade.md)|  |

### Return type

[**AddResponse**](AddResponse.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="portfoliosAvailableInstrumentsMetaList"></a>
# **portfoliosAvailableInstrumentsMetaList**
> PortfolioInstrumentsMeta portfoliosAvailableInstrumentsMetaList(portfolio)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
try {
    val result : PortfolioInstrumentsMeta = apiInstance.portfoliosAvailableInstrumentsMetaList(portfolio)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosAvailableInstrumentsMetaList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosAvailableInstrumentsMetaList")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  |

### Return type

[**PortfolioInstrumentsMeta**](PortfolioInstrumentsMeta.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="portfoliosAvailableTagsList"></a>
# **portfoliosAvailableTagsList**
> TagsMetaSummary portfoliosAvailableTagsList(portfolio)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
try {
    val result : TagsMetaSummary = apiInstance.portfoliosAvailableTagsList(portfolio)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosAvailableTagsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosAvailableTagsList")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  |

### Return type

[**TagsMetaSummary**](TagsMetaSummary.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="portfoliosClearCreate"></a>
# **portfoliosClearCreate**
> InlineResponse200 portfoliosClearCreate(portfolio)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
try {
    val result : InlineResponse200 = apiInstance.portfoliosClearCreate(portfolio)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosClearCreate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosClearCreate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  |

### Return type

[**InlineResponse200**](InlineResponse200.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="portfoliosCreateCreate"></a>
# **portfoliosCreateCreate**
> Portfolio portfoliosCreateCreate(data)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
val data : Portfolio =  // Portfolio | 
try {
    val result : Portfolio = apiInstance.portfoliosCreateCreate(data)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosCreateCreate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosCreateCreate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **data** | [**Portfolio**](Portfolio.md)|  |

### Return type

[**Portfolio**](Portfolio.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="portfoliosDeleteDelete"></a>
# **portfoliosDeleteDelete**
> portfoliosDeleteDelete(portfolio)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
try {
    apiInstance.portfoliosDeleteDelete(portfolio)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosDeleteDelete")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosDeleteDelete")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  |

### Return type

null (empty response body)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="portfoliosList"></a>
# **portfoliosList**
> kotlin.Array&lt;Portfolio&gt; portfoliosList()



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
try {
    val result : kotlin.Array<Portfolio> = apiInstance.portfoliosList()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosList")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Array&lt;Portfolio&gt;**](Portfolio.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="portfoliosOrdersList"></a>
# **portfoliosOrdersList**
> kotlin.Array&lt;Order&gt; portfoliosOrdersList(portfolio)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
try {
    val result : kotlin.Array<Order> = apiInstance.portfoliosOrdersList(portfolio)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosOrdersList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosOrdersList")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  |

### Return type

[**kotlin.Array&lt;Order&gt;**](Order.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="portfoliosTradesList"></a>
# **portfoliosTradesList**
> kotlin.Array&lt;Trade&gt; portfoliosTradesList(portfolio)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiInstance = PortfoliosApi()
val portfolio : kotlin.String = portfolio_example // kotlin.String | 
try {
    val result : kotlin.Array<Trade> = apiInstance.portfoliosTradesList(portfolio)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PortfoliosApi#portfoliosTradesList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PortfoliosApi#portfoliosTradesList")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **portfolio** | **kotlin.String**|  |

### Return type

[**kotlin.Array&lt;Trade&gt;**](Trade.md)

### Authorization


Configure Basic:
    ApiClient.username = ""
    ApiClient.password = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

