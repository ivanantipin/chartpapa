
# AuthUserGroups

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **kotlin.Int** | Note: This is a Primary Key.&lt;pk/&gt; | 
**userId** | **kotlin.Int** | Note: This is a Foreign Key to &#x60;auth_user.id&#x60;.&lt;fk table&#x3D;&#39;auth_user&#39; column&#x3D;&#39;id&#39;/&gt; | 
**groupId** | **kotlin.Int** | Note: This is a Foreign Key to &#x60;auth_group.id&#x60;.&lt;fk table&#x3D;&#39;auth_group&#39; column&#x3D;&#39;id&#39;/&gt; | 



