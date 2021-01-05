#!/usr/bin/python
import os  
import sys
print("System Path")
print(sys.path[0])   
basic = '/builds/osdu/platform/system/reference/crs-conversion-service'
isExist = os.path.exists(basic)  
print(isExist) 


# Path  
path = basic+'/apachesis_setup/SIS_DATA'
     
# Check whether the   
# specified path is   
# an existing file  
isExist = os.path.exists(path)  
print(isExist) 
     
     
# Path  
path = '~/apachesis_setup/SIS_DATA'
     
# Check whether the   
# specified path is   
# an existing file  
isExist = os.path.exists(path)  
print(isExist)  
