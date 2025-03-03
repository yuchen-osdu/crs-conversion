import os

BASE_URL = os.getenv("BASE_URL", '/api/crs/converter')
ROOT_URL = os.getenv("VIRTUAL_SERVICE_HOST_NAME")
STORAGE_URL=os.getenv("STORAGE_URL", "NOT_FOUND")
MY_TENANT = os.getenv("MY_TENANT")
MY_REPLACE_DOMAIN = os.getenv("MY_REPLACE_DOMAIN", "NOT_FOUND")
MY_LEGAL_TAG = os.getenv("MY_LEGAL_TAG", "NOT_FOUND")
MY_TEST_ID = os.getenv("MY_TEST_ID", "NOT_FOUND")