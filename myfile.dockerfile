
FROM openjdk:8-jdk-alpine
ARG JAR_FILE=/provider/crs-converter-azure/crs-converter-aks/target/crs-converter-aks-1.0.0.jar
ARG client=ab58a975-2582-4cbd-a029-34d24e8459ea
COPY ${JAR_FILE} /app.jar
ENTRYPOINT ["java","-Dclient-id=$client","-jar","/app.jar"]