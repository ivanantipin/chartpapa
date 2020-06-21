
# AuthGroupPermissions

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **kotlin.Int** | Note: This is a Primary Key.&lt;pk/&gt; | 
**groupId** | **kotlin.Int** | Note: This is a Foreign Key to &#x60;auth_group.id&#x60;.&lt;fk table&#x3D;&#39;auth_group&#39; column&#x3D;&#39;id&#39;/&gt; | 
**permissionId** | **kotlin.Int** | Note: This is a Foreign Key to &#x60;auth_permission.id&#x60;.&lt;fk table&#x3D;&#39;auth_permission&#39; column&#x3D;&#39;id&#39;/&gt; | 



