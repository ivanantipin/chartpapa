
# DjangoAdminLog

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **kotlin.Int** | Note: This is a Primary Key.&lt;pk/&gt; | 
**actionTime** | **kotlin.String** |  | 
**objectRepr** | **kotlin.String** |  | 
**actionFlag** | **kotlin.Int** |  | 
**changeMessage** | **kotlin.String** |  | 
**userId** | **kotlin.Int** | Note: This is a Foreign Key to &#x60;auth_user.id&#x60;.&lt;fk table&#x3D;&#39;auth_user&#39; column&#x3D;&#39;id&#39;/&gt; | 
**objectId** | **kotlin.String** |  |  [optional]
**contentTypeId** | **kotlin.Int** | Note: This is a Foreign Key to &#x60;django_content_type.id&#x60;.&lt;fk table&#x3D;&#39;django_content_type&#39; column&#x3D;&#39;id&#39;/&gt; |  [optional]



