#!/usr/bin/env bash

# Install venv for python3
which apt-get && sudo apt-get install -y python3 python3-pip python3-venv || echo "Not Ubuntu, skipping"
which yum && sudo yum install -y python3 python3-pip python3-venv || echo "Not RHEL, skipping"

# Setup Python Environment and install requirements
python3 -m venv env
sed -i 's/$1/${1:-}/' env/bin/activate # Fix deactivation bug '$1 unbound variable'
source env/bin/activate
python3 -m pip install --upgrade pip
python3 -m pip install -r requirements.txt

echo ***TESTING***
python3 copy_sis.py
echo ***End TESTING**

# Run tests
echo ***RUNNING CRS Converter API TESTS***
python3 run_test_api.py
TEST_STATUS=$?
echo ***FINISHED CRS Converter API TESTS***
echo ""

	
# Display Results
echo "-------------------------------"
echo "TEST_ERRORS: $TEST_STATUS"
echo "-------------------------------"
# Uninstall Environment if not on ADO Pipelines
if [ -z ${AGENT_POOL+x}  ]; then
  python3 -m pip freeze > requirements.txt
  python3 -m pip uninstall -r requirements.txt -y
  deactivate
  rm -rf env/
fi

if [ $TEST_STATUS -ne 0 ]
then
  exit 1
else
  exit 0
fi
