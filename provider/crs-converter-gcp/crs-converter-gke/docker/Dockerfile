FROM openjdk:8-jdk-alpine

ARG ENTITLEMENT_URL
ARG SIS_DATA

ENV ENTITLEMENT_URL=${ENTITLEMENT_URL}
ENV SIS_DATA=${SIS_DATA}

EXPOSE 8080

COPY provider/crs-converter-gcp/crs-converter-gke/target/crs-converter-gke-1.0.0.jar /crs-converter/root.jar
COPY apachesis_setup /crs-converter/apachesis_setup

WORKDIR /crs-converter

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar root.jar