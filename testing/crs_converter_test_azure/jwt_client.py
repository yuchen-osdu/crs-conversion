import os
import msal
import logging
logging.basicConfig(level=os.environ.get("LOG_LEVEL", "INFO"))


def get_id_token():
    # Generate valid Token for given tenant.
    tenant_id = os.getenv('AZURE_TENANT_ID')
    resource_id = os.getenv('AZURE_AD_APP_RESOURCE_ID')
    client_id = os.getenv('INTEGRATION_TESTER')
    client_secret = os.getenv('AZURE_TESTER_SERVICEPRINCIPAL_SECRET')
    authority_host_uri = 'https://login.microsoftonline.com'
    authority_uri = authority_host_uri + '/' + tenant_id
    scopes = [resource_id + '/.default']

    try:
        app = msal.ConfidentialClientApplication(client_id=client_id, authority=authority_uri, client_credential=client_secret)
        result = app.acquire_token_for_client(scopes=scopes)
        return result.get('access_token')
    except Exception as e:
        print(e)

def get_invalid_token():
    '''
    This is dummy jwt
    {
         "sub": "dummy@dummy.com",
         "iss": "dummy@dummy.com",
         "aud": "dummy.dummy.com",
         "iat": 1556137273,
         "exp": 1556223673,
         "provider": "dummy.com",
         "client": "dummy.com",
         "userid": "dummytester.com",
         "email": "dummytester.com",
         "authz": "",
         "lastname": "dummy",
         "firstname": "dummy",
         "country": "",
         "company": "",
         "jobtitle": "",
         "subid": "dummyid",
         "idp": "dummy",
         "hd": "dummy.com",
         "desid": "dummyid",
         "contact_email": "dummy@dummy.com"
    }
    '''
    return "fail.azure"
