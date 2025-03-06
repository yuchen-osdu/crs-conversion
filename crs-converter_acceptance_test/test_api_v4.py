import pytest
import schemathesis
import jwt_client
import os

from hypothesis import settings
from dotenv import load_dotenv
# loading variables from .env file
load_dotenv()

schema = schemathesis.from_uri(f"https://{os.environ['HOST_URL']}/api/crs/catalog/api-docs/v4/")

@pytest.fixture(scope="session")
def token():
    return jwt_client.get_id_token()

# exclude methods that fail
# TODO: should be fixed on later api revisions
@schema.parametrize()
@settings(max_examples=25)
def test_api(case, token):
    case.headers = {"Authorization": f"Bearer {token}"}
    response = case.call()
    case.validate_response(response)