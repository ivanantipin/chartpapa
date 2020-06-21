
# ApiTrade

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**portfolio** | **kotlin.String** |  | 
**tradeId** | **kotlin.String** | Note: This is a Primary Key.&lt;pk/&gt; | 
**side** | **kotlin.String** |  | 
**qty** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  | 
**openTime** | **kotlin.String** |  | 
**closeTime** | **kotlin.String** |  | 
**openPrice** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  | 
**closePrice** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  | 
**pnl** | [**java.math.BigDecimal**](java.math.BigDecimal.md) |  | 
**symbolId** | **kotlin.String** | Note: This is a Foreign Key to &#x60;api_instrument.identifier&#x60;.&lt;fk table&#x3D;&#39;api_instrument&#39; column&#x3D;&#39;identifier&#39;/&gt; | 
**discreteTags** | **kotlin.String** |  |  [optional]
**continuousTags** | **kotlin.String** |  |  [optional]



