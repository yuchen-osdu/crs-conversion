#!/usr/bin/env bash

# Copyright © Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

CUR_DIR=$(pwd)
SCRIPT_SOURCE_DIR=$(dirname "$0")
echo "Script source location"
echo "$SCRIPT_SOURCE_DIR"
cd $SCRIPT_SOURCE_DIR

# Required variables
export VIRTUAL_SERVICE_HOST_NAME=$CRS_CONVERTER_HOST
export MY_TENANT="osdu"
export STORAGE_URL=$STORAGE_URL'records'
export MY_REPLACE_DOMAIN="example.com"
export MY_LEGAL_TAG="osdu-public-usa-dataset"
export MY_TEST_ID="12345678"

export AWS_COGNITO_AUTH_FLOW="USER_PASSWORD_AUTH"
export PRIVILEGED_USER_TOKEN=$(aws cognito-idp initiate-auth --region ${AWS_REGION} --auth-flow ${AWS_COGNITO_AUTH_FLOW} --client-id ${AWS_COGNITO_CLIENT_ID} --auth-parameters "{\"USERNAME\":\"${ADMIN_USER}\",\"PASSWORD\":\"${ADMIN_PASSWORD}\"}" --query AuthenticationResult.AccessToken --output text)

python3 -m pip install --upgrade pip

python3 -m venv env
source env/bin/activate

python3 -m pip install -r v2/requirements.txt
pip install xmlrunner==1.7.7
pip install pytest

export API_VER="v2"
echo ***RUNNING CATALOG API $API_VER SCHEMA TESTS***
python3 run_test_v2.py
TEST_STATUS_V2=$?
echo ***FINISHED CATALOG API $API_VER SCHEMA TESTS***
echo "TEST STATUS: $TEST_STATUS_V2"

python3 -m pip install -r v3/requirements.txt
export API_VER="v3"
echo ***RUNNING CATALOG API $API_VER SCHEMA TESTS***
python3 run_test_v3.py
TEST_STATUS_V3=$?
echo ***FINISHED CATALOG API $API_VER SCHEMA TESTS***

echo "TEST STATUS: $TEST_STATUS_V3"

python3 -m pip install -r v4/requirements.txt
export API_VER="v4"
echo ***RUNNING CATALOG API $API_VER SCHEMA TESTS***
python3 run_test_v4.py
TEST_STATUS_V4=$?
echo ***FINISHED CATALOG API $API_VER SCHEMA TESTS***
echo "TEST STATUS: $TEST_STATUS_V4"

echo "Copying XML files to test-reports folder"
cp *.xml $CUR_DIR/test-reports

deactivate
rm -rf env/

cd $CUR_DIR

if [ $TEST_STATUS_V2 -ne 0 ] || [ $TEST_STATUS_V3 -ne 0 ] || [ $TEST_STATUS_V4 -ne 0 ]
then
  exit 1
else
  exit 0
fi
