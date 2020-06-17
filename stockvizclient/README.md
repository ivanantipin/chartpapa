# org.openapitools.client - Kotlin client library for API

## Requires

* Kotlin 1.3.41
* Gradle 4.9

## Build

First, create the gradle wrapper script:

```
gradle wrapper
```

Then, run:

```
./gradlew check assemble
```

This runs all tests and packages the library.

## Features/Implementation Notes

* Supports JSON inputs/outputs, File inputs, and Form inputs.
* Supports collection formats for query parameters: csv, tsv, ssv, pipes.
* Some Kotlin and Java types are fully qualified to avoid conflicts with types defined in OpenAPI definitions.
* Implementation of ApiClient is intended to reduce method counts, specifically to benefit Android targets.

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *http://localhost:8000/api/v1*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*CandlesApi* | [**candlesRead**](docs/CandlesApi.md#candlesread) | **GET** /candles/{timeframe}/{symbol}/ | 
*InstrumentsApi* | [**instrumentsAddCreate**](docs/InstrumentsApi.md#instrumentsaddcreate) | **POST** /instruments/add | 
*InstrumentsApi* | [**instrumentsList**](docs/InstrumentsApi.md#instrumentslist) | **GET** /instruments/ | 
*PortfoliosApi* | [**portfoliosAddOrdersCreate**](docs/PortfoliosApi.md#portfoliosaddorderscreate) | **POST** /portfolios/{portfolio}/add/orders/ | 
*PortfoliosApi* | [**portfoliosAddTradesCreate**](docs/PortfoliosApi.md#portfoliosaddtradescreate) | **POST** /portfolios/{portfolio}/add/trades/ | 
*PortfoliosApi* | [**portfoliosAvailableInstrumentsMetaList**](docs/PortfoliosApi.md#portfoliosavailableinstrumentsmetalist) | **GET** /portfolios/{portfolio}/available-instruments-meta/ | 
*PortfoliosApi* | [**portfoliosAvailableTagsList**](docs/PortfoliosApi.md#portfoliosavailabletagslist) | **GET** /portfolios/{portfolio}/available-tags/ | 
*PortfoliosApi* | [**portfoliosClearCreate**](docs/PortfoliosApi.md#portfoliosclearcreate) | **POST** /portfolios/{portfolio}/clear/ | 
*PortfoliosApi* | [**portfoliosCreateCreate**](docs/PortfoliosApi.md#portfolioscreatecreate) | **POST** /portfolios/create/ | 
*PortfoliosApi* | [**portfoliosDeleteDelete**](docs/PortfoliosApi.md#portfoliosdeletedelete) | **DELETE** /portfolios/{portfolio}/delete/ | 
*PortfoliosApi* | [**portfoliosList**](docs/PortfoliosApi.md#portfolioslist) | **GET** /portfolios/ | 
*PortfoliosApi* | [**portfoliosOrdersList**](docs/PortfoliosApi.md#portfoliosorderslist) | **GET** /portfolios/{portfolio}/orders/ | 
*PortfoliosApi* | [**portfoliosTradesList**](docs/PortfoliosApi.md#portfoliostradeslist) | **GET** /portfolios/{portfolio}/trades/ | 


<a name="documentation-for-models"></a>
## Documentation for Models

 - [org.openapitools.client.models.Candle](docs/Candle.md)
 - [org.openapitools.client.models.ContinuousMeta](docs/ContinuousMeta.md)
 - [org.openapitools.client.models.DiscreteMeta](docs/DiscreteMeta.md)
 - [org.openapitools.client.models.InlineResponse200](docs/InlineResponse200.md)
 - [org.openapitools.client.models.InlineResponse2001](docs/InlineResponse2001.md)
 - [org.openapitools.client.models.NewInstrument](docs/NewInstrument.md)
 - [org.openapitools.client.models.NewOrder](docs/NewOrder.md)
 - [org.openapitools.client.models.NewTrade](docs/NewTrade.md)
 - [org.openapitools.client.models.Order](docs/Order.md)
 - [org.openapitools.client.models.Portfolio](docs/Portfolio.md)
 - [org.openapitools.client.models.PortfolioInstrumentsMeta](docs/PortfolioInstrumentsMeta.md)
 - [org.openapitools.client.models.Symbol](docs/Symbol.md)
 - [org.openapitools.client.models.TagsMetaSummary](docs/TagsMetaSummary.md)
 - [org.openapitools.client.models.Trade](docs/Trade.md)


<a name="documentation-for-authorization"></a>
## Documentation for Authorization

<a name="Basic"></a>
### Basic

- **Type**: HTTP basic authentication

