FROM eclipse-temurin:21-jdk-jammy
LABEL "Owner"="rubium.nl"
EXPOSE 49980
COPY target/fairy-tales-0.0.1-SNAPSHOT.jar fairy-tales.jar
ENTRYPOINT ["java","-jar","/fairy-tales.jar"]