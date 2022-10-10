#!/bin/bash

CRS_CONVERSION_URL=http://${RELEASE_NAME}-ibm-crs-conversion-deploy:8080/api/crs/converter/v2/info
echo ${CRS_CONVERSION_URL}
while [[ "$(curl -s -k -L -o /dev/null -w ''%{http_code}'' http://${RELEASE_NAME}-ibm-crs-conversion-deploy:8080/api/crs/converter/v2/info)" != "200" ]]; do sleep 5; done
echo "CRS Conversion is ready"
echo ${RELEASE_NAMESPACE}
echo ${RELEASE_NAME}
crs_conversion_pod_name=$(oc get pods -o=name -n ${RELEASE_NAMESPACE} | grep 'crs-conversion-deploy' | sed "s/^.\{4\}//")
echo $crs_conversion_pod_name
oc rsync /data/crs-conversion-service/apachesis_setup $crs_conversion_pod_name:/home/jboss/data/ -n ${RELEASE_NAMESPACE}

