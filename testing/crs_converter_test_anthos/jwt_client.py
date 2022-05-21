#!/usr/bin/python
#
#  Copyright 2020-2022 Google LLC
#  Copyright 2020-2022 EPAM Systems, Inc
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

import sys


def main(argv):
    pass


if __name__ == '__main__':
    main(sys.argv)
import http.client
import time
import google.auth.crypt
import google.auth.jwt
import urllib
import json
import base64
import os

def generate_jwt():
    INTEGRATION_TESTER = str(os.getenv('INTEGRATION_TESTER'))
    decoded_user_key = base64.b64decode(INTEGRATION_TESTER).decode("utf-8")
    signer = google.auth.crypt.RSASigner.from_string(json.loads(decoded_user_key)['private_key'])
    integration_test_service_account = json.loads(decoded_user_key)['client_email']
    now = int(time.time())

    payload = {
        'iat': now,
        "exp": now + 3600,
        'iss': integration_test_service_account,
        "target_audience": str(os.getenv('GOOGLE_AUDIENCES')),
        "aud": "https://www.googleapis.com/oauth2/v4/token"
    }

    jwt = google.auth.jwt.encode(signer, payload)

    return jwt


def get_id_token():
    try:
        
        params = urllib.parse.urlencode({
            'grant_type': 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion': generate_jwt()
        })
        headers = {"Content-Type": "application/x-www-form-urlencoded"}
        conn = http.client.HTTPSConnection("www.googleapis.com")
        conn.request("POST", "/oauth2/v4/token", params, headers)
        res = json.loads(conn.getresponse().read().decode('utf-8'))
        conn.close()
        return res['id_token']
    except (IOError, KeyError, ValueError) as e:
        raise ValueError('Bearer token could not be obtained - missing service account file? ' + repr(e) + ' ' + str(e))

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

    return "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkdW1teUBkdW1teS5jb20iLCJpc3MiOiJkdW1teUBkdW1teS5jb20iLCJhdWQiOiJkdW1teS5kdW1teS5jb20iLCJpYXQiOjE1NTYxMzcyNzMsImV4cCI6MTU1NjIzMDk3OSwicHJvdmlkZXIiOiJkdW1teS5jb20iLCJjbGllbnQiOiJkdW1teS5jb20iLCJ1c2VyaWQiOiJkdW1teXRlc3Rlci5jb20iLCJlbWFpbCI6ImR1bW15dGVzdGVyLmNvbSIsImF1dGh6IjoiIiwibGFzdG5hbWUiOiJkdW1teSIsImZpcnN0bmFtZSI6ImR1bW15IiwiY291bnRyeSI6IiIsImNvbXBhbnkiOiIiLCJqb2J0aXRsZSI6IiIsInN1YmlkIjoiZHVtbXlpZCIsImlkcCI6ImR1bW15IiwiaGQiOiJkdW1teS5jb20iLCJkZXNpZCI6ImR1bW15aWQiLCJjb250YWN0X2VtYWlsIjoiZHVtbXlAZHVtbXkuY29tIiwianRpIjoiNGEyMWYyYzItZjU5Yy00NWZhLTk0MTAtNDNkNDdhMTg4ODgwIn0.nkiyKtfXXxAlC60iDjXuB2EAGDfZiVglP-CyU1T4etc"
