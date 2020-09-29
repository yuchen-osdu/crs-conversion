FROM openjdk:8-jdk-alpine

COPY EsriCatalog /EsriCatalog
COPY provider/crs-converter-azure/crs-converter-aks/target/crs-converter-aks-1.0.0.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

