FROM maven:3-eclipse-temurin-21-jammy AS build

ARG VERSION
ARG JAR_PATH

VOLUME /tmp
WORKDIR /
ADD . .

RUN mvn clean install  -f /common/pom.xml -Dspotless.apply.skip

RUN mvn clean package -f /visitors/pom.xml -Dmaven.test.skip -Dspotless.apply.skip

FROM eclipse-temurin:21-jre-jammy
LABEL "Owner"="rubium.nl"
EXPOSE 49984
COPY --from=build /visitors/target/visitors.jar /
ENTRYPOINT ["java","-jar","/visitors.jar"]