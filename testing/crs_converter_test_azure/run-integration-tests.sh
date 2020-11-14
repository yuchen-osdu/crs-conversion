#!/usr/bin/bash

# Install venv for python3
which apt-get && sudo apt-get install -y python3 python3-pip python3-venv || echo "Not Ubuntu, skipping"
which yum && sudo yum install -y python3 python3-pip python3-venv || echo "Not RHEL, skipping"
# if [[ "$OSTYPE" =~ ^msys ]]; then #windows
	# if ! [ -x "$(command -v virtualenv)" ]; then
		# python -m pip install virtualenv
	# fi
  
	# python -m venv env
	# source env/Scripts/activate
	
	# python -m pip install --upgrade pip
	# python -m pip install -r requirements.txt
# else
python3 -m venv env
#sed -i 's/$1/${1:-}/' env/bin/activate # Fix deactivation bug '$1 unbound variable'
source env/bin/activate
python3 -m pip install --upgrade pip
python3 -m pip install -r requirements.txt

# Run tests
echo ***RUNNING CRS Converter API TESTS***
python3 run_test_api.py
TEST_STATUS=$?
echo ***FINISHED CRS Converter API V2 TESTS***

	
# python3 -m pip freeze > requirements.txt
python3 -m pip uninstall -r requirements.txt -y
deactivate
rm -rf env/

if [ $TEST_STATUS -ne 0 ]
then
    exit 1
fi
