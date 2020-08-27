FROM openjdk:8-jdk-alpine

# Path to the jar file. E.g. provider/crs-converter-azure/crs-converter-aks/target/crs-converter-aks-*.jar
ARG CRS_CONVERTER_JAR_PATH

ARG ENTITLEMENT_URL

ENV ENTITLEMENT_URL=$ENTITLEMENT_URL

EXPOSE 8080

COPY $CRS_CONVERTER_JAR_PATH ./crs-converter/crs-converter.jar

WORKDIR ./crs-converter

ENTRYPOINT ["java", "-jar", "crs-converter.jar"]
