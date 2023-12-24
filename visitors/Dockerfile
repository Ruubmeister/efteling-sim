FROM eclipse-temurin:21-jdk-jammy
LABEL "Owner"="rubium.nl"
EXPOSE 49984
COPY target/visitors-0.0.1-SNAPSHOT.jar visitors.jar
ENTRYPOINT ["java","-jar","/visitors.jar"]