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

#!/usr/bin/env bash

if [[ "$OSTYPE" == "msys" ]]; then
  python -m pip install --upgrade pip
  python -m pip install --user virtualenv
  python -m venv env
  source env/Scripts/activate
  python -m pip install --upgrade pip
  python -m pip install -r requirements.txt

  # Run tests
  echo ***RUNNING CRS Converter API V2 TESTS***
  python run_test_api_v2.py
  V2_TEST_STATUS=$?
  echo ***FINISHED CRS Converter API V2 TESTS***

  # Run tests
  echo ***RUNNING CRS Converter API V3 TESTS***
  python run_test_api_v3.py
  V3_TEST_STATUS=$?
  echo ***FINISHED CRS Converter API V3 TESTS***

  # python -m pip freeze > requirements.txt
  python -m pip uninstall -r requirements.txt -y

else

  # Install venv for python3
  which apt-get && sudo apt-get install -y python3 python3-pip python3-venv || echo "Not Ubuntu, skipping"
  which yum && sudo yum install -y python3 python3-pip python3-venv || echo "Not RHEL, skipping"

  python3 -m venv env
  # sed -i 's/$1/${1:-}/' env/bin/activate # Fix deactivation bug '$1 unbound variable'
  source env/bin/activate
  python3 -m pip install --upgrade pip
  python3 -m pip install -r requirements.txt

  # Run tests
  echo ***RUNNING CRS Converter API V2 TESTS***
  python3 run_test_api_v2.py
  V2_TEST_STATUS=$?
  echo ***FINISHED CRS Converter API V2 TESTS***
  svctoken=$(python3 jwt_client.py)
  echo 'Register Legal tag before Integration Tests ...'
  curl --location --request POST "$LEGAL_URL"'legaltags' \
    --header 'accept: application/json' \
    --header 'authorization: Bearer '"$svctoken" \
    --header 'content-type: application/json' \
    --header 'data-partition-id: osdu' \
    --data '{
          "name": "public-usa-dataset",
          "description": "legal tag for CRS Conversion V3 Integration tests",
          "properties": {
              "countryOfOrigin":["US"],
              "contractId":"A1234",
              "expirationDate":"2099-01-25",
              "dataType":"Public Domain Data", 
              "originator":"MyCompany",
              "securityClassification":"Public",
              "exportClassification":"EAR99",
              "personalData":"No Personal Data"
          }
    }'


  # Uploading records and deleting afterwards
    echo 'Add records before Integration Tests ...'
    curl --location --request PUT "$STORAGE_URL" \
      --header 'accept: application/json' \
      --header 'authorization: Bearer '"$svctoken" \
      --header 'content-type: application/json' \
      --header 'data-partition-id: '"$MY_TENANT" \
      --data-raw '[
          {
            "id": "'"$MY_TENANT"':reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851",
            "kind": "osdu:wks:reference-data--CoordinateReferenceSystem:1.1.0",
            "acl": {
              "owners": [
                "data.default.owners@'"$MY_TENANT"'.'"$MY_REPLACE_DOMAIN"'"
              ],
              "viewers": [
                "data.default.viewers@'"$MY_TENANT"'.'"$MY_REPLACE_DOMAIN"'"
              ]
            },
            "legal": {
              "legaltags": [
                "'"$MY_LEGAL_TAG"'"
              ],
              "otherRelevantDataCountries": [
                "US"
              ]
            },
            "data": {
              "BaseCRS": {
                "AuthorityCode": {
                  "Authority": "EPSG",
                  "Code": 4267
                },
                "BaseCRSID": "'"$MY_TENANT"':reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4267:",
                "Name": "NAD27"
              },
              "Code": "32064079",
              "CodeAsNumber": 32064079,
              "CodeSpace": "OSDU",
              "CoordinateReferenceSystemType": "BoundCRS",
              "CoordinateSystem": {
                "AuthorityCode": {
                  "Authority": "EPSG",
                  "Code": 4497
                },
                "Name": "Cartesian 2D CS. Axes: easting, northing (X,Y). Orientations: east, north. UoM: ftUS.",
                "HorizontalAxisUnitID": "'"$MY_TENANT"':reference-data--UnitOfMeasure:ft%5BUS%5D:"
              },
              "ID": "BoundProjected:EPSG::32064_EPSG::15851",
              "Kind": "BoundProjected",
              "Name": "NAD27 * OGP-Usa Conus / BLM 14N (ftUS) [32064,15851]",
              "PersistableReference": "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"32064079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32064\"},\"name\":\"NAD_1927_BLM_Zone_14N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"NAD_1927_BLM_Zone_14N\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",1640416.666666667],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-99.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Foot_US\\\",0.3048006096012192],AUTHORITY[\\\"EPSG\\\",32064]]\"},\"name\":\"NAD27 * OGP-Usa Conus / BLM 14N (ftUS) [32064,15851]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
              "PreferredUsage": {
                "Extent": {
                  "AuthorityCode": {
                    "Authority": "EPSG",
                    "Code": 3637
                  },
                  "BoundingBoxEastBoundLongitude": -95.87,
                  "BoundingBoxNorthBoundLatitude": 49.01,
                  "BoundingBoxSouthBoundLatitude": 25.83,
                  "BoundingBoxWestBoundLongitude": -102.0,
                  "Description": "United States (USA) - between 102\u00b0W and 96\u00b0W. Iowa; Kansas; Minnesota; Nebraska; North Dakota; Oklahoma; South Dakota; Texas; Gulf of Mexico outer continental shelf (GoM OCS) west of approximately 96\u00b0W - protraction areas Corpus Christi; Port Isabel.",
                  "Name": "USA - 102\u00b0W to 96\u00b0W and GoM OCS"
                },
                "Name": "USA - 102\u00b0W to 96\u00b0W and GoM OCS (from coordinate reference system)",
                "Scope": {
                  "AuthorityCode": {
                    "Authority": "EPSG",
                    "Code": 1249
                  },
                  "Name": "Minerals management (including oil and gas exploration and production)."
                }
              },
              "Projection": {
                "AuthorityCode": {
                  "Authority": "EPSG",
                  "Code": 15914
                },
                "Name": "BLM zone 14N (US survey feet)"
              },
              "RevisionDate": "2023-01-21T13:19:59+00:00",
              "SourceCRS": {
                "AuthorityCode": {
                  "Authority": "EPSG",
                  "Code": 32064
                },
                "Name": "NAD27 / BLM 14N (ftUS)",
                "SourceCRSID": "'"$MY_TENANT"':reference-data--CoordinateReferenceSystem:Projected:EPSG::32064:"
              },
              "Transformation": {
                "AuthorityCode": {
                  "Authority": "EPSG",
                  "Code": 15851
                },
                "Name": "NAD27 to WGS 84 (79)",
                "TransformationID": "'"$MY_TENANT"':reference-data--CoordinateTransformation:EPSG::15851:"
              },
              "Usages": [
                {
                  "Extent": {
                    "AuthorityCode": {
                      "Authority": "EPSG",
                      "Code": 3637
                    },
                    "BoundingBoxEastBoundLongitude": -95.87,
                    "BoundingBoxNorthBoundLatitude": 49.01,
                    "BoundingBoxSouthBoundLatitude": 25.83,
                    "BoundingBoxWestBoundLongitude": -102.0,
                    "Description": "United States (USA) - between 102\u00b0W and 96\u00b0W. Iowa; Kansas; Minnesota; Nebraska; North Dakota; Oklahoma; South Dakota; Texas; Gulf of Mexico outer continental shelf (GoM OCS) west of approximately 96\u00b0W - protraction areas Corpus Christi; Port Isabel.",
                    "Name": "USA - 102\u00b0W to 96\u00b0W and GoM OCS"
                  },
                  "Name": "USA - 102\u00b0W to 96\u00b0W and GoM OCS (from coordinate reference system)",
                  "Scope": {
                    "AuthorityCode": {
                      "Authority": "EPSG",
                      "Code": 1249
                    },
                    "Name": "Minerals management (including oil and gas exploration and production)."
                  }
                }
              ],
              "Wgs84Coordinates": {
                "type": "FeatureCollection",
                "features": [
                  {
                    "type": "Feature",
                    "properties": {},
                    "geometry": {
                      "type": "Polygon",
                      "coordinates": [
                        [
                          [
                            -102.0,
                            25.83
                          ],
                          [
                            -95.87,
                            25.83
                          ],
                          [
                            -95.87,
                            49.01
                          ],
                          [
                            -102.0,
                            49.01
                          ],
                          [
                            -102.0,
                            25.83
                          ]
                        ]
                      ]
                    }
                  }
                ]
              },
              "Source": "Workbook Resources/IOGP/Manifests/reference-data/CoordinateReferenceSystem.1.1.0.json; commit SHA ad1bf0bf.",
              "CommitDate": "2022-12-21T08:41:03+01:00"
            }
          },
          {
            "id": "'"$MY_TENANT"':reference-data--CoordinateReferenceSystem:Projected:EPSG::32615",
            "kind": "osdu:wks:reference-data--CoordinateReferenceSystem:1.1.0",
            "acl": {
              "owners": [
                "data.default.owners@'"$MY_TENANT"'.'"$MY_REPLACE_DOMAIN"'"
              ],
              "viewers": [
                "data.default.viewers@'"$MY_TENANT"'.'"$MY_REPLACE_DOMAIN"'"
              ]
            },
            "legal": {
              "legaltags": [
                "'"$MY_LEGAL_TAG"'"
              ],
              "otherRelevantDataCountries": [
                "US"
              ]
            },
            "data": {
              "BaseCRS": {
                "AuthorityCode": {
                  "Authority": "EPSG",
                  "Code": 4326
                },
                "BaseCRSID": "'"$MY_TENANT"':reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:",
                "Name": "WGS 84"
              },
              "Code": "32615",
              "CodeAsNumber": 32615,
              "CodeSpace": "EPSG",
              "CoordinateReferenceSystemType": "ProjectedCRS",
              "CoordinateSystem": {
                "AuthorityCode": {
                  "Authority": "EPSG",
                  "Code": 4400
                },
                "Name": "Cartesian 2D CS. Axes: easting, northing (E,N). Orientations: east, north. UoM: m.",
                "HorizontalAxisUnitID": "'"$MY_TENANT"':reference-data--UnitOfMeasure:m:"
              },
              "Datum": {},
              "ID": "Projected:EPSG::32615",
              "Kind": "projected",
              "Name": "WGS 84 / UTM zone 15N",
              "PersistableReference": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32615\"},\"name\":\"WGS_1984_UTM_Zone_15N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_15N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-93.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32615]]\"}",
              "PreferredUsage": {
                "AuthorityCode": {
                  "Authority": "EPSG",
                  "Code": 7526
                },
                "Extent": {
                  "AuthorityCode": {
                    "Authority": "EPSG",
                    "Code": 2028
                  },
                  "BoundingBoxEastBoundLongitude": -90.0,
                  "BoundingBoxNorthBoundLatitude": 84.0,
                  "BoundingBoxSouthBoundLatitude": 0.0,
                  "BoundingBoxWestBoundLongitude": -96.0,
                  "Description": "Between 96\u00b0W and 90\u00b0W, northern hemisphere between equator and 84\u00b0N, onshore and offshore. Canada - Manitoba; Nunavut; Ontario. Ecuador -Galapagos. Guatemala. Mexico. United States (USA).",
                  "Name": "World - N hemisphere - 96\u00b0W to 90\u00b0W - by country"
                },
                "Name": "World - N hemisphere - 96\u00b0W to 90\u00b0W - by country",
                "Scope": {
                  "AuthorityCode": {
                    "Authority": "EPSG",
                    "Code": 1279
                  },
                  "Name": "Navigation and medium accuracy spatial referencing."
                }
              },
              "Projection": {
                "AuthorityCode": {
                  "Authority": "EPSG",
                  "Code": 16015
                },
                "Name": "UTM zone 15N"
              },
              "RevisionDate": "2022-12-12T00:00:00+00:00",
              "Usages": [
                {
                  "AuthorityCode": {
                    "Authority": "EPSG",
                    "Code": 7526
                  },
                  "Extent": {
                    "AuthorityCode": {
                      "Authority": "EPSG",
                      "Code": 2028
                    },
                    "BoundingBoxEastBoundLongitude": -90.0,
                    "BoundingBoxNorthBoundLatitude": 84.0,
                    "BoundingBoxSouthBoundLatitude": 0.0,
                    "BoundingBoxWestBoundLongitude": -96.0,
                    "Description": "Between 96\u00b0W and 90\u00b0W, northern hemisphere between equator and 84\u00b0N, onshore and offshore. Canada - Manitoba; Nunavut; Ontario. Ecuador -Galapagos. Guatemala. Mexico. United States (USA).",
                    "Name": "World - N hemisphere - 96\u00b0W to 90\u00b0W - by country"
                  },
                  "Name": "World - N hemisphere - 96\u00b0W to 90\u00b0W - by country",
                  "Scope": {
                    "AuthorityCode": {
                      "Authority": "EPSG",
                      "Code": 1279
                    },
                    "Name": "Navigation and medium accuracy spatial referencing."
                  }
                }
              ],
              "Wgs84Coordinates": {
                "type": "FeatureCollection",
                "features": [
                  {
                    "type": "Feature",
                    "properties": {},
                    "geometry": {
                      "type": "Polygon",
                      "coordinates": [
                        [
                          [
                            -96.0,
                            0.0
                          ],
                          [
                            -90.0,
                            0.0
                          ],
                          [
                            -90.0,
                            84.0
                          ],
                          [
                            -96.0,
                            84.0
                          ],
                          [
                            -96.0,
                            0.0
                          ]
                        ]
                      ]
                    }
                  }
                ]
              },
              "Source": "Workbook Resources/IOGP/Manifests/reference-data/CoordinateReferenceSystem.1.1.0.json; commit SHA ad1bf0bf.",
              "CommitDate": "2022-12-21T08:41:03+01:00"
            }
          }
      ]'

  # Run tests
  echo ***RUNNING CRS Converter API V3 TESTS***
  python3 run_test_api_v3.py
  V3_TEST_STATUS=$?
  echo ***FINISHED CRS Converter API V3 TESTS***

  # Deleting Records
  echo 'Deleting records after Integration Tests ...'
  curl --location --request DELETE "$STORAGE_URL"'/'"$MY_TENANT"':reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851' \
    --header 'authorization: Bearer '"$svctoken" \
    --header 'data-partition-id: '"$MY_TENANT" \

  curl --location --request DELETE "$STORAGE_URL"'/'"$MY_TENANT"':reference-data--CoordinateReferenceSystem:Projected:EPSG::32615' \
    --header 'authorization: Bearer '"$svctoken" \
    --header 'data-partition-id: '"$MY_TENANT" \

  # python3 -m pip freeze > requirements.txt
  python3 -m pip uninstall -r requirements.txt -y

fi

deactivate
rm -rf env/

echo ***TEST RESULTS***
echo $V2_TEST_STATUS
echo $V3_TEST_STATUS

if [ $V2_TEST_STATUS -ne 0 ] || [ $V3_TEST_STATUS -ne 0 ]
then
    exit 1
fi
