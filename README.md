# CRS conversion service

This document presents useful information about common development activities for the CRS service.

## This repository contains 
1. The Java implementation of the Converter (aka crs-conversion-serice). 
1. To open the Java project, open ```pom.xml```.
1. The core module is in the crs-converter-core. The Java code is located in its ```src``` sub-folder and tests are located in ```src/test/java/org/opengroup/osdu/crs/...```
1. The provider folder contains different provider implementations.
1. Python integration and health tests in the ```testing``` folder. 
See also the test's [README.md](testing/README.md)

## Prerequisites
1. The project builds with [maven](https://maven.apache.org/). Make sure maven is installed locally.
1. The project requires the [Lombok](https://projectlombok.org/) plug-in installed for your IDE.

## Running CRS Conversion Service locally
#### Build and run CRS Conversion Service locally using bash
- Set the required environments described in [Release/deployment](##Release/deployment) section
- Navigate to the Converter Service's root folder ```crs-conversion-service``` 
- Build core and run unit tests on command line:
    - In order to run unit tests, set the SIS_DATA variable before building
```bash
export SIS_DATA=${SRC_ROOT_DIR}/apachesis_setup/SIS_DATA
mvn clean install 
# To run without tests add -DskipTests=true
```
- Run application with command
```bash
java -jar provider/crs-converter-azure/crs-converter-aks/target/crs-converter-aks-1.0.0.jar
```

### Debug CRS Conversion Service locally
- Perform build as instructed in the Build and run Converter Service locally section
- Start the application:
```sh
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n -jar provider/crs-converter-azure/crs-converter-aks/target/crs-converter-aks-1.0.0.jar
```

- Set up remote debugging configuration in Intellij and start it
- Environment variables: Set the required environments described in [Release/deployment](##Release/deployment) section

- Execute **Debug** for configured Application

In the Postman Settings / General, turn SSL certificate validation off when running locally.
Similarly, when not using Postman but client code, set the configuration  ```verify_ssl``` false (see [instructions](https://github.com/swagger-api/swagger-codegen/issues/7778))

Run application using debug mode and use [Postman](https://www.getpostman.com/)
to send a GET request to obtaining the Swagger API documentation
```
http://localhost:8080/api/crs/converter/v2/api-docs
```
or send a POST request to convert points.
```
http://localhost:8080/api/crs/converter/v2/convert
```
Open the Swagger-UI:
```
http://localhost:8080/api/crs/converter/swagger-ui.html
```

Headers for Postman:

| Key | Value |
|----------|----------|
| Authorization | Bearer `<token>` |
| data-partition-id | $MY_TENANT (see [testing\README.md](testing/README.md)) |

### Build and run the Docker container locally
1. Run the `maven run` command to have the .jar file generated.
1. Have the Azure subscription set up 
1. Open a Powershell
1. Install the Azure CLI locally
1. Authenticate yourself to Azure Container Registry (acr) with the following command:
```az acr login --name delfi```
1. Execute the following command to build the container image:
```docker build -t crs-converter .```
1. Execute the following command to build the container image:
```docker run -t --rm -p 8080:8080 crs-converter```
1. Use Postman or curl to try out the endpoints

## Release/deployment
VSTS release definition is located at provider\crs-converter-azure\crs-converter-aks\devops, which 
requires the following environment variables:

| Variable | Contents | Example |
|----------|----------|----------|
| ENTITLEMENT_URL | Required | |
| SIS_DATA | Required | ${SRC_ROOT_DIR}/apachesis_setup/SIS_DATA |

## GCP

Instructions for running the Google Cloud Platform implementation locally can be found [here](./provider/crs-converter-gke/README.md).

