
# Order

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**discreteTags** | **kotlin.collections.Map&lt;kotlin.String, kotlin.String&gt;** |  | 
**continuousTags** | [**kotlin.collections.Map&lt;kotlin.String, java.math.BigDecimal&gt;**](java.math.BigDecimal.md) |  | 
**portfolio** | **kotlin.String** |  | 
**orderId** | **kotlin.String** |  | 
**side** | [**inline**](#SideEnum) |  | 
**orderType** | [**inline**](#OrderTypeEnum) |  | 
**status** | [**inline**](#StatusEnum) |  | 
**qty** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  | 
**placeTime** | [**java.time.OffsetDateTime**](java.time.OffsetDateTime.md) |  | 
**updateTime** | [**java.time.OffsetDateTime**](java.time.OffsetDateTime.md) |  | 
**symbol** | **kotlin.String** |  | 
**id** | **kotlin.Int** |  |  [optional] [readonly]
**tradeId** | **kotlin.String** |  |  [optional]
**price** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  |  [optional]
**executionPrice** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  |  [optional]


<a name="SideEnum"></a>
## Enum: side
Name | Value
---- | -----
side | buy, sell


<a name="OrderTypeEnum"></a>
## Enum: order_type
Name | Value
---- | -----
orderType | limit, market, stop, stop_limit, market_on_close, market_on_open, limit_on_close, limit_on_open


<a name="StatusEnum"></a>
## Enum: status
Name | Value
---- | -----
status | filled, canceled, placed, partial_filled



