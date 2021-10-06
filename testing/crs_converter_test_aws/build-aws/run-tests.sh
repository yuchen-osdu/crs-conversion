# Copyright © 2020 Amazon Web Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script executes the test and copies reports to the provided output directory
# To call this script from the service working directory
# ./dist/testing/integration/build-aws/run-tests.sh "./reports/"

SCRIPT_SOURCE_DIR=$(dirname "$0")
echo "Script source location"
echo "$SCRIPT_SOURCE_DIR"
# (cd "$SCRIPT_SOURCE_DIR"/../bin && ./install-deps.sh)

#### ADD REQUIRED ENVIRONMENT VARIABLES HERE ###############################################
# The following variables are automatically populated from the environment during integration testing
# see os-deploy-aws/build-aws/integration-test-env-variables.py for an updated list

# AWS_COGNITO_CLIENT_ID
# ELASTIC_HOST
# ELASTIC_PORT
# FILE_URL
# LEGAL_URL
# SEARCH_URL
# STORAGE_URL


export AWS_COGNITO_AUTH_FLOW=USER_PASSWORD_AUTH
export AWS_COGNITO_AUTH_PARAMS_PASSWORD=ABCDEFGabcdefg123!@#
export AWS_COGNITO_AUTH_PARAMS_USER=admin@testing.com
export AWS_COGNITO_AUTH_PARAMS_USER_NO_ACCESS=noaccess@testing.com
export VIRTUAL_SERVICE_HOST_NAME=a5850ca2aa5704565bb0285bd947bd37-1547327503.us-east-1.elb.amazonaws.com
export MY_TENANT=opendes

#### RUN INTEGRATION TEST #########################################################################

pushd "$SCRIPT_SOURCE_DIR"/../

rm -rf test-reports/
mkdir test-reports
chmod +x run-integration-tests.sh
echo Running CRS Converter Service Integration Tests...
./run-integration-tests.sh
TEST_EXIT_CODE=$?
popd

#### COPY TEST REPORTS #########################################################################

if [ -n "$1" ]
  then
    mkdir -p "$1"
    cp -R "$SCRIPT_SOURCE_DIR"/../test-reports "$1"
fi

exit $TEST_EXIT_CODE