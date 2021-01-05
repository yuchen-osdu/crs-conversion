#!/usr/bin/python
import os  
import sys
print("System Path")
print(sys.path)   
# Path  
path = 'crs-conversion-service/apachesis_setup/SIS_DATA'
     
# Check whether the   
# specified path is   
# an existing file  
isExist = os.path.exists(path)  
print(isExist) 
     
     
# Path  
path = 'apachesis_setup/SIS_DATA'
     
# Check whether the   
# specified path is   
# an existing file  
isExist = os.path.exists(path)  
print(isExist)  

path = 'crs-converter/apachesis_setup/SIS_DATA'
isExist = os.path.exists(path)  
print(isExist) 

path = './apachesis_setup/SIS_DATA'
isExist = os.path.exists(path)  
print(isExist) 

path = '/apachesis_setup/SIS_DATA'
isExist = os.path.exists(path)  
print(isExist) 