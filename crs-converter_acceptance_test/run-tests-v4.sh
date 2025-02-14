#!/usr/bin/env bash

python3 -m pip install --upgrade pip

python3 -m venv env
source env/bin/activate

python3 -m pip install -r v4/requirements.txt

echo ""
export API_VER="v4"
echo ***RUNNING CATALOG API $API_VER TESTS***
python3 run_test_v4.py
TEST_STATUS=$?
echo ***FINISHED CATALOG API $API_VER TESTS***

echo "TEST STATUS: $TEST_STATUS"

deactivate
rm -rf env/


if [ $TEST_STATUS -ne 0 ]
then
    exit 1
fi
