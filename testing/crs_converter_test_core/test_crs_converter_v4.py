import json
import os
import unittest
import warnings

from testing.crs_converter_test_aws import jwt_client
from testing.crs_converter_test_core.test_crs_converter_v3 import TestRecords
from testing.crs_converter_test_core.utility import TestEnvironment
from testing.crs_converter_test_core.v4.swagger_client import Configuration, ApiClient
from testing.crs_converter_test_core.v3.swagger_client.rest import ApiException

import urllib3

from testing.crs_converter_test_core.v4.swagger_client import Crsconverterapiv4Api

urllib3.disable_warnings()


class TestTrajectoryConverterIntegrationV4(unittest.TestCase):
    """Post deployment tests for trajectory-converter service"""

    @classmethod
    def setUpClass(cls):
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<ssl.SSLSocket.*>")
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<socket.socket.*>")
        urllib3.disable_warnings()

        cls.env = TestEnvironment()
        if not cls.env.is_ok():
            raise Exception('Test environment is not properly set up; BASE_URL, ROOT_URL, MY_TENANT not set.')
        # Configure API key authorization: api_key
        configuration = Configuration()
        # Set the bearer token; use a service account to do this
        bearer = jwt_client.get_id_token()  # always create a proper bearer token, needed for calls to dps-trajectory
        configuration.api_key['Authorization'] = 'Bearer ' + bearer
        configuration.access_token = bearer
        configuration.verify_ssl = False
        if 'localhost' in cls.env.root_url:
            url = 'http://' + cls.env.root_url + cls.env.base_url
        else:
            url = 'https://' + cls.env.root_url + cls.env.base_url
        data_partition_header_name = 'data_partition_id'
        data_partition_header_value = cls.env.data_partition_id
        client = ApiClient(host=url)
        client.set_default_header(header_name=data_partition_header_name, header_value=data_partition_header_value)
        client.user_agent = 'IntegrationTest'
        cls.api_instance = Crsconverterapiv4Api(client)
        cls.test_records = TestRecords()
        cls.test_records.setup()

    @classmethod
    def tearDownClass(cls):
        cls.test_records.teardown()

    def convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_WithSuccess(self):
        request = self.test_conversion_only('v4/data/AzimuthalEquidistantProjectedCRS_GN_WithSuccess.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # convert_trajectory
            api_response = self.api_instance.convert_trajectory(body=request, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertEquals(api_response.scaleConvergenceList[0].scalefactor, 0.999723)
            self.assertEquals(api_response.scaleConvergenceList[0].convergence, -1.47055)
            self.assertEquals(api_response.scaleConvergenceList[1].scalefactor, 0.999699)
            self.assertEquals(api_response.scaleConvergenceList[1].convergence, -1.32361)
        except ApiException as e:
            self.fail(str(e))


class TestRecords(unittest.TestCase):
    """Test the info end-points"""
    DATA_PARTITION_TO_REPLACE = '{{DATA_PARTITION_ID}}'
    DOMAIN_TO_REPLACE = '{{DOMAIN}}'
    TAG_TO_REPLACE = '{{LEGAL_TAG}}'
    TEST_ID_REPLACE = '{{TEST_ID}}'

    def setup(self):
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<ssl.SSLSocket.*>")
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<socket.socket.*>")
        urllib3.disable_warnings()

        self.env = TestEnvironment()
        if not self.env.is_ok():
            raise Exception('Test environment is not properly set up; BASE_URL, ROOT_URL, MY_TENANT not set.')
        # Configure API key authorization: api_key
        configuration = Configuration()
        # Set the bearer token; use a service account to do this
        bearer = jwt_client.get_id_token()  # always create a proper bearer token, needed for calls to dps-trajectory
        configuration.api_key['Authorization'] = 'Bearer ' + bearer
        configuration.access_token = bearer
        configuration.verify_ssl = False
        self.header = {}
        self.client = ApiClient(host=self.env.storage_url)
        self.header['data-partition-id'] = self.env.data_partition_id
        self.header['Content-Type'] = 'application/json'
        self.header['Authorization'] = 'Bearer ' + bearer
        self.client.user_agent = 'IntegrationTest'
        self.recordIDs = []
        self.put_records()


def suite():
    suite = unittest.TestSuite()
    suite.addTest(
        TestTrajectoryConverterIntegrationV4('convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_WithSuccess'))
    return suite;


if __name__ == '__main__':
    runner = unittest.TextTestRunner(failfast=True)
    runner.run(suite())
