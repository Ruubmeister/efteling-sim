FROM eclipse-temurin:21-jdk-jammy
LABEL "Owner"="rubium.nl"
EXPOSE 49981
COPY target/rides-0.0.1-SNAPSHOT.jar rides.jar
ENTRYPOINT ["java","-jar","/rides.jar"]