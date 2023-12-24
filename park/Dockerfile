FROM eclipse-temurin:21-jdk-jammy
LABEL "Owner"="rubium.nl"
EXPOSE 49983
COPY target/park-0.0.1-SNAPSHOT.jar park.jar
ENTRYPOINT ["java","-jar","/park.jar"]