# swagger_client.Crsconverterapiv4Api

All URIs are relative to *https://az-osdu1.evd.csp.slb.com/api/crs/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**convert_trajectory**](Crsconverterapiv4Api.md#convert_trajectory) | **POST** /v4/convertTrajectory | 


# **convert_trajectory**
> convert_trajectory(data_partition_id)



### Example 
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# Configure API key authorization: Bearer
swagger_client.configuration.api_key['Authorization'] = 'YOUR_API_KEY'
# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# swagger_client.configuration.api_key_prefix['Authorization'] = 'Bearer'

# create an instance of the API class
api_instance = swagger_client.Crsconverterapiv4Api()
data_partition_id = 'data_partition_id_example' # str | Tenant Id

try: 
    api_instance.convert_trajectory(data_partition_id)
except ApiException as e:
    print("Exception when calling Crsconverterapiv4Api->convert_trajectory: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **data_partition_id** | **str**| Tenant Id | 

### Return type

void (empty response body)

### Authorization

[Bearer](../README.md#Bearer)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

