#!/usr/bin/bash
SHARE_NAME="crs-conversion"
search_dir="apachesis_setup/SIS_DATA"
search_dir="apachesis_setup/SIS_DATA"
      if [ -d "$search_dir" ]; then
        echo "Starting to upload files for CRS Conversion Service"
        accountKey=$(kubectl get secret airflow -n osdu -o jsonpath='{.data.azurestorageaccountkey}' | base64 -d)
        accountName=$(kubectl get secret airflow -n osdu -o jsonpath='{.data.azurestorageaccountname}' | base64 -d)
		az storage directory create --name "apachesis_setup" --account-name $accountName --account-key $accountKey --share-name $SHARE_NAME 
		az storage directory create --name "apachesis_setup\SIS_DATA" --account-name $accountName --account-key $accountKey --share-name $SHARE_NAME 
			az storage directory create --name "apachesis_setup\SIS_DATA\Databases" --account-name $accountName --account-key $accountKey --share-name $SHARE_NAME 
			
		az storage directory create --name "apachesis_setup\SIS_DATA\Databases\ExternalSources" --account-name $accountName --account-key $accountKey --share-name $SHARE_NAME 
        az storage directory create --name "apachesis_setup\SIS_DATA\Databases\SpatialMetadata" --account-name $accountName --account-key $accountKey --share-name $SHARE_NAME 
		az storage directory create --name "apachesis_setup\SIS_DATA\Databases\SpatialMetadata\log" --account-name $accountName --account-key $accountKey --share-name $SHARE_NAME 
                            
		az storage directory create --name "apachesis_setup\SIS_DATA\Databases\SpatialMetadata\seg0" --account-name $accountName --account-key $accountKey --share-name $SHARE_NAME 
                            
		az storage directory create --name "apachesis_setup\SIS_DATA\DatumChanges" --account-name $accountName --account-key $accountKey --share-name $SHARE_NAME 
                            
		
        find "$search_dir/" -type f -print0 | while read -d $'\0' file; do
			echo "File: $file"
            az storage file upload --account-name $accountName --account-key $accountKey --share-name $SHARE_NAME --source "$file" --path "$file"
        done
        echo "File upload successfully completed for CRS Conversion Service"
      fi