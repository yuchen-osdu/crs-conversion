import os

VENDOR = os.getenv("VENDOR")
BASE_URL = os.getenv("BASE_URL", '/api/crs/converter/v2')
if VENDOR == 'ibm':
    ROOT_URL = os.getenv("IBM_VIRTUAL_HOST_CRS_CONVERSION")
else:
    ROOT_URL = os.getenv("VIRTUAL_SERVICE_HOST_NAME")
DATA_DIR = os.getenv("DATA_DIR")
DATA_PATTERN = os.getenv("DATA_PATTERN")
REPORT_PATH = os.getenv("REPORT_PATH")
MY_TENANT = os.getenv("MY_TENANT")
