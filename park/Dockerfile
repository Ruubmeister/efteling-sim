FROM maven:3-eclipse-temurin-21-jammy AS build

ARG VERSION
ARG JAR_PATH

VOLUME /tmp
WORKDIR /
ADD . .

RUN mvn clean install  -f /common/pom.xml -Dspotless.apply.skip

RUN mvn clean package -f /park/pom.xml -Dmaven.test.skip -Dspotless.apply.skip

FROM eclipse-temurin:21-jre-jammy
LABEL "Owner"="rubium.nl"
EXPOSE 49983
COPY --from=build /park/target/park.jar /
ENTRYPOINT ["java","-jar","/park.jar"]