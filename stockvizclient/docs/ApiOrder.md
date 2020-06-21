
# ApiOrder

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **kotlin.Int** | Note: This is a Primary Key.&lt;pk/&gt; | 
**orderId** | **kotlin.String** |  | 
**side** | **kotlin.String** |  | 
**orderType** | **kotlin.String** |  | 
**status** | **kotlin.String** |  | 
**qty** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  | 
**placeTime** | **kotlin.String** |  | 
**updateTime** | **kotlin.String** |  | 
**symbolId** | **kotlin.String** | Note: This is a Foreign Key to &#x60;api_instrument.identifier&#x60;.&lt;fk table&#x3D;&#39;api_instrument&#39; column&#x3D;&#39;identifier&#39;/&gt; | 
**portfolio** | **kotlin.String** |  | 
**tradeId** | **kotlin.String** |  |  [optional]
**price** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  |  [optional]
**executionPrice** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  |  [optional]
**discreteTags** | **kotlin.String** |  |  [optional]
**continuousTags** | **kotlin.String** |  |  [optional]



