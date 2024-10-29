FROM azul/zulu-openjdk:17-latest
WORKDIR /crs-converter
ARG PORT
ENV PORT $PORT
ENV SIS_DATA="/crs-converter/apachesis_setup/SIS_DATA"
EXPOSE 8080


# Copy the jar to the production image from the builder stage.
COPY crs-converter-core-plus/target/crs-converter-core-plus-*-spring-boot.jar crs-converter-core-plus.jar
COPY apachesis_setup /crs-converter/apachesis_setup

# Add a non-root user
RUN groupadd -g 10001 -r nonroot \
  && useradd -g 10001 -r -u 10001 nonroot
  
# Run as non-root user
USER 10001:10001

# Run the web service on container startup.
CMD java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT} -jar /crs-converter/crs-converter-core-plus.jar
