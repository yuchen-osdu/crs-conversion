# CRS conversion service

This service provides spatial reference conversions for coordinates.
Coordinates are represented by an array of 3D points.

```
  "points": [
    {
      "x": -61.04340628871454,
      "y": 10.673103179456877,
      "z": 0
    },
    {
      "x": -62.28871454043406,
      "y": 11.794568776731031,
      "z": 0
    }
  ]
```

The context, i.e. the measurement and unit associated with the axes,
is given by the CRS definitions. In most of the cases, the CRS definition
is 2D. In both the geographic and projected CRS types, the Z-axis is passed through unchanged, and its unit is
only known to the client.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Requirements

* Java 8
* [Maven 3.6.0+](https://maven.apache.org/download.cgi)
* GCloud command line tool
* GCloud access to opendes project

### General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler

* [direnv](https://direnv.net/) - for a shell/terminal environment
* [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.

* [Intellij configuration](https://projectlombok.org/setup/intellij)
* [VSCode configuration](https://projectlombok.org/setup/vscode)

### Installation

In order to run the service locally or remotely, you will need to have the following environment variables defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
 | `LOG_PREFIX` | `service` | Logging prefix | no | - |
 | `SERVER_SERVLET_CONTEXPATH` | `/api/crs/converter/v2` | CRS conversion service context path | no | - |
 | `ENTITLEMENTS_API` | ex `https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
 | `SIS_DATA` | ex `E:\crs-converter\apachesis_setup\` | Apache SIS setup | no | [apachesis](../../../apachesis_setup/README.md) |
 | `STORAGE_API` | ex `https://storage.com//api/storage/v2` | Storage service API endpoint | no | output of infrastructure deployment |

### Run Locally

Check that maven is installed:

```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You will need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.m2/settings.xml`:

```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>os-core</id>
            <username>slb-des-ext-collaboration</username>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <password>${VSTS_FEED_TOKEN}</password>
        </server>
    </servers>
</settings>
```

* Update the Google cloud SDK to the latest version:

```bash
gcloud components update
```

* Set Google Project Id:

```bash
gcloud config set project <YOUR-PROJECT-ID>
```

* Perform a basic authentication in the selected project:

```bash
gcloud auth application-default login
```

* Navigate to CRS conversion service root folder and run:

```bash
mvn jetty:run
## Testing
* Navigate to CRS conversion service root folder and run:
 
```bash
mvn clean install   
```

* If you wish to see the coverage report then go to testing/target/site/jacoco-aggregate and open index.html

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
cd provider/crs-converte-gc/crs-converter-gke/ && mvn spring-boot:run
```

## Testing

Navigate to CRS conversion service root folder and run all the tests:

```bash
# build + test + install core service code
$ (cd crs-converter-core/ && mvn clean install)
```

## Test the application

After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/api/crs/converter/swagger-ui.html](http://localhost:8080/api/crs/converter/swagger-ui.html). If the request does not fail, you can then run the integration tests.

### Running E2E Tests

This section describes how to run cloud OSDU E2E tests (testing/crs_converter_test_gc).

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `INTEGRATION_TESTER` | `********` | A base64 encoded google service account json credentials authorization for OSDU services | yes | output of infrastructure deployment |

This section describes how to run cloud OSDU E2E tests (testing/crs_converter_test_anthos).

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `INTEGRATION_TESTER` | `********` | A base64 encoded google service account json credentials authorization for OSDU services | yes | output of infrastructure deployment |

## Tests core (crs_converter_test_core/constants.py)

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `BASE_URL` | ex `/api/crs/converter/v2` | CRS conversion service context path | yes | output of infrastructure deployment |
| `VIRTUAL_SERVICE_HOST_NAME` | ex `open.opendes.cloud.slb-ds.com` | - | false | output of infrastructure deployment |
| `MY_TENANT` | ex `opendes` | Current Tenant | false | output of infrastructure deployment |
| `DATA_DIR` | ex `E:\tmp\CloudTestData` | Test Data dir | false | - |
| `DATA_PATTERN` | ex `Test*.0*.json` | Test Data patter for file | false | - |
| `REPORT_PATH` | ex `E:\tmp\CloudTestData\Report\SuiteReport.txt` | Report path | false | - |

| INTEGRATION_TESTER |
| ---  |
| users<br/>service.entitlements.user<br/>data.test1<br/>data.integration.test<br/>users@{tenant1}@{domain}.com |

Execute following command to build code and run all the integration tests:

## Building/running ```test_crs_converter_v2.py```

Go to the provider folder:

```bash
cd crs_converter_test_$PROVIDER_NAME/
```

To set up a virtual environment:

```bash
virtualenv venv
python3 -m pip install -r requirements.txt
```

To activate on Windows:

```bash
venv\Scripts\activate
```

To run:

```bash
python3 run_test.py
```

**Note:** To simulate a runtime exactly as that of the vsts build agent, you can simply exec into the docker image we use for the build agent, and run the tests from inside it. To know how to do this, please follow [this](https://slb-swt.visualstudio.com/data-at-rest/_git/dps-vsts-build-agent?path=%2FREADME.md&version=GBmaster) documentation.

## Suite Test: ```test_suite.py```

This test requires pre-computed test data with expected values. This test - depending on how many data are sent through - will take a significant amount of time.

### Example format for the test data

```
{
  "CreateDate": "2017-08-07T15:02:51.8424946+00:00",
  "TestData": [
    {
      "FromCrsPersistableReference": "%7B%22WKT%22%3A%22PROJCS%5B%5C%22DGN_1995_UTM_Zone_48N%5C%22%2CGEOGCS%5B%5C%22GCS_DGN_1995%5C%22%2CDATUM%5B%5C%22D_Datum_Geodesi_Nasional_1995%5C%22%2CSPHEROID%5B%5C%22WGS_1984%5C%22%2C6378137.0%2C298.257223563%5D%5D%2CPRIMEM%5B%5C%22Greenwich%5C%22%2C0.0%5D%2CUNIT%5B%5C%22Degree%5C%22%2C0.0174532925199433%5D%5D%2CPROJECTION%5B%5C%22Transverse_Mercator%5C%22%5D%2CPARAMETER%5B%5C%22False_Easting%5C%22%2C500000.0%5D%2CPARAMETER%5B%5C%22False_Northing%5C%22%2C0.0%5D%2CPARAMETER%5B%5C%22Central_Meridian%5C%22%2C105.0%5D%2CPARAMETER%5B%5C%22Scale_Factor%5C%22%2C0.9996%5D%2CPARAMETER%5B%5C%22Latitude_Of_Origin%5C%22%2C0.0%5D%2CUNIT%5B%5C%22Meter%5C%22%2C1.0%5D%2CAUTHORITY%5B%5C%22EPSG%5C%22%2C23868%5D%5D%22%2C%22Type%22%3A%22LBCRS%22%2C%22EngineVersion%22%3A%22PE_10_3_1%22%2C%22Name%22%3A%22DGN_1995_UTM_Zone_48N%22%2C%22AuthorityCode%22%3A%7B%22Authority%22%3A%22EPSG%22%2C%22Code%22%3A%2223868%22%7D%7D",
      "ToCrsPersistableReference": "%7B%22WKT%22%3A%22GEOGCS%5B%5C%22GCS_DGN_1995%5C%22%2CDATUM%5B%5C%22D_Datum_Geodesi_Nasional_1995%5C%22%2CSPHEROID%5B%5C%22WGS_1984%5C%22%2C6378137.0%2C298.257223563%5D%5D%2CPRIMEM%5B%5C%22Greenwich%5C%22%2C0.0%5D%2CUNIT%5B%5C%22Degree%5C%22%2C0.0174532925199433%5D%2CAUTHORITY%5B%5C%22EPSG%5C%22%2C4755%5D%5D%22%2C%22Type%22%3A%22LBCRS%22%2C%22EngineVersion%22%3A%22PE_10_3_1%22%2C%22Name%22%3A%22GCS_DGN_1995%22%2C%22AuthorityCode%22%3A%7B%22Authority%22%3A%22EPSG%22%2C%22Code%22%3A%224755%22%7D%7D",
      "FromInfo": "DGN_1995_UTM_Zone_48N [m]",
      "ToInfo": "GCS_DGN_1995 [dega]",
      "Info": "Late-bound CRS Conversions",
      "FromToPoints": [
        {
          "FromPoint": {
            "X": 166021.4430837048,
            "Y": 0.0,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 102.0,
            "Y": 0.0,
            "Z": 0.0
          }
        },
        {
          "FromPoint": {
            "X": 833978.5569162938,
            "Y": 0.0,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 108.0,
            "Y": 0.0,
            "Z": 0.0
          }
        },
        {
          "FromPoint": {
            "X": 168456.68464518717,
            "Y": 768165.58302924014,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 102.0,
            "Y": 6.94,
            "Z": 0.0
          }
        },
        {
          "FromPoint": {
            "X": 831543.31535481149,
            "Y": 768165.58302924014,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 108.0,
            "Y": 6.94,
            "Z": 0.0
          }
        },
        {
          "FromPoint": {
            "X": 500000.0,
            "Y": 383543.96537060448,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 105.0,
            "Y": 3.47,
            "Z": 0.0
          }
        }
      ]
    }
  ]
}
```

## Building/running ```test_suite.py```

To set up a virtual environment:

```bash
virtualenv venv
python3 -m pip install -r requirements.txt
```

To activate on Windows:

```bash
venv\Scripts\activate
```

To run:

```bash
python test_suite.py
```

## Deployment

See Google Documentation: <https://cloud.google.com/cloud-build/docs/deploying-builds/deploy-gke>

## Licence

Copyright © Google LLC
Copyright © EPAM Systems

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
