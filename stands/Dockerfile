FROM eclipse-temurin:21-jdk-jammy
LABEL "Owner"="rubium.nl"
EXPOSE 49982
COPY target/stands-0.0.1-SNAPSHOT.jar stands.jar
ENTRYPOINT ["java","-jar","/stands.jar"]