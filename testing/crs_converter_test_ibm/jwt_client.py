# Licensed Materials - Property of IBM
# (c) Copyright IBM Corp. 2020. All Rights Reserved.
import os
from keycloak import KeycloakOpenID

def get_id_token():

    keycloak_host = os.getenv('KEYCLOAK_URL')
    keycloak_realm = os.getenv('KEYCLOAK_REALM', "OSDU")
    client_id = os.getenv('KEYCLOAK_CLIENT_ID')
    client_secret = os.getenv('KEYCLOAK_CLIENT_SECRET')

    user = os.getenv('AUTH_USER_ACCESS')
    password = os.getenv('AUTH_USER_ACCESS_PASSWORD')

    try:
        # Configure client
        keycloak_openid = KeycloakOpenID(server_url="https://"+keycloak_host+"/auth/",
                            client_id=client_id,
                            realm_name=keycloak_realm,
                            client_secret_key=client_secret)

        token = keycloak_openid.token(user, password)
        return token['access_token']
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

    return "fake.token"