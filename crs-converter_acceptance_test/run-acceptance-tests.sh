#!/usr/bin/env bash
python3 -m pip install --upgrade pip

python3 -m venv env
source env/bin/activate

python3 -m pip install -r requirements.txt

echo ""
export API_VER="v2"
echo ***RUNNING CATALOG API $API_VER SCHEMA TESTS***
pytest -v run_test_v2.py
TEST_STATUS_V2=$?
echo ***FINISHED CATALOG API $API_VER SCHEMA TESTS***

echo "TEST STATUS: $TEST_STATUS_V2"

echo ""
export API_VER="v3"
echo ***RUNNING CATALOG API $API_VER SCHEMA TESTS***
pytest -v run_test_v3.py
TEST_STATUS_V3=$?
echo ***FINISHED CATALOG API $API_VER SCHEMA TESTS***

echo "TEST STATUS: $TEST_STATUS_V3"

echo ""
export API_VER="v4"
echo ***RUNNING CATALOG API $API_VER SCHEMA TESTS***
pytest -v run_test_v4.py
TEST_STATUS_V4=$?
echo ***FINISHED CATALOG API $API_VER SCHEMA TESTS***

echo "TEST STATUS: $TEST_STATUS_V4"

deactivate
rm -rf env/

if [ $TEST_STATUS_V2 -ne 0 ] || [ $TEST_STATUS_V3 -ne 0 ] || [ $TEST_STATUS_V4 -ne 0 ]
then
  exit 1
else
  exit 0
fi