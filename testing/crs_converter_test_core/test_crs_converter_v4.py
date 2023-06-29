import json
import os
import unittest
import warnings

from testing.crs_converter_test_aws import jwt_client
from testing.crs_converter_test_core.test_crs_converter_v3 import TestRecords
from testing.crs_converter_test_core.utility import TestEnvironment
from testing.crs_converter_test_core.v3.swagger_client import Configuration, ApiClient, Point, TrajectoryStationOut, \
    TrajectoryStationIn, ConvertTrajectoryRequest, ConvertTrajectoryResponse, TrajectoryComputationAndConversionApi
from testing.crs_converter_test_core.v3.swagger_client.rest import ApiException

import urllib3
urllib3.disable_warnings()

def convertString(str):
    DATA_PARTITION_TO_REPLACE = '{{DATA_PARTITION_ID}}'
    DOMAIN_TO_REPLACE = '{{DOMAIN}}'
    TAG_TO_REPLACE = '{{LEGAL_TAG}}'
    TEST_ID_REPLACE = '{{TEST_ID}}'
    env = TestEnvironment()
    body_str = str
    body_str = body_str.replace(DATA_PARTITION_TO_REPLACE, env.data_partition_id)
    body_str = body_str.replace(DOMAIN_TO_REPLACE, env.my_replace_domain)
    body_str = body_str.replace(TAG_TO_REPLACE, env.my_legal_tag)
    body_str = body_str.replace(TEST_ID_REPLACE, env.my_test_id)
    return body_str

class TestV4TrajectoryConverterIntegration(unittest.TestCase):
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
        cls.api_instance = TrajectoryComputationAndConversionApi(client)
        cls.test_records = TestRecords()
        cls.test_records.setup()

    @staticmethod
    def __read_request(file_name):
        dir_path = os.path.dirname(__file__)
        dir_path = os.path.join(dir_path, file_name)
        with open(dir_path) as json_file:
            request_dict = json.loads(convertString(json_file.read()))
        stations = list()
        for station in request_dict['inputStations']:
            stations.append(TrajectoryStationIn(station['md'], station['inclination'], station['azimuth']))
        value = request_dict['referencePoint']
        reference_point = Point(value['x'], value['y'], value['z'])
        return ConvertTrajectoryRequest(trajectory_crs=request_dict['trajectoryCRS'],
                                        azimuth_reference=request_dict['azimuthReference'],
                                        unit_xy=request_dict['unitXY'],
                                        unit_z=request_dict['unitZ'],
                                        reference_point=reference_point,
                                        input_stations=stations, method=request_dict['method'],
                                        interpolate=request_dict['interpolate'])

    @staticmethod
    def __read_response(file_name):
        dir_path = os.path.dirname(__file__)
        dir_path = os.path.join(dir_path, file_name)
        with open(dir_path) as json_file:
            r_dict = json.loads(json_file.read())
        stations = list()
        for station in r_dict['stations']:
            p_d = station['point']
            p = Point(p_d['x'], p_d['y'], p_d['z'])
            stations.append(TrajectoryStationOut(md=station['md'],
                                                 inclination=station['inclination'],
                                                 azimuth_tn=station['azimuthTN'],
                                                 azimuth_gn=station['azimuthGN'],
                                                 dx_tn=station['dxTN'],
                                                 dy_tn=station['dyTN'],
                                                 point=p,
                                                 wgs84_latitude=station['wgs84Latitude'],
                                                 wgs84_longitude=station['wgs84Longitude'],
                                                 dls=station['dls'],
                                                 original=station['original'],
                                                 dz=station['dz']))
        return ConvertTrajectoryResponse(trajectory_crs=r_dict['trajectoryCRS'],
                                         unit_xy=r_dict['unitXY'],
                                         unit_z=r_dict['unitZ'],
                                         unit_dls=r_dict['unitDls'],
                                         stations=stations,
                                         method=r_dict['method'],
                                         local_crs=r_dict['localCRS'],
                                         operations_applied=r_dict['operationsApplied'])

    def test_bin_grid_without_TOCRS(self):
        request = self.__read_binGrid_request('v3/data/Convert_BinGrid_WithoutToCRS.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # Convert a BinGrid
            api_response = self.api_instance.convert_bin_grid(body=request, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertEquals(api_response.max_mis_location.d_i, 0.0)
            self.assertEquals(api_response.max_mis_location.d_j, 0.0)
        except ApiException as e:
            self.fail(str(e))

def suite():
       suite = unittest.TestSuite()
       suite.addTest(TestV4TrajectoryConverterIntegration('test_conversion_only'))
       return suite;

if __name__ == '__main__':
      runner = unittest.TextTestRunner(failfast=True)
      runner.run(suite())