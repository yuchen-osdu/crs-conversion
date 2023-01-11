#!/usr/bin/env bash

# set -e

python3 -m pip install virtualenv && virtualenv venv
python3 -m pip install -r requirements.txt
python3 run_test.py
