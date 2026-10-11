FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B clean verify

FROM tomcat:10.1-jdk17-temurin
COPY --from=build /build/target/vnpay-cart.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 10000
CMD ["sh", "-c", "PORT=${PORT:-10000}; sed -i \"s/port=\\\"8080\\\"/port=\\\"$PORT\\\"/g\" /usr/local/tomcat/conf/server.xml; exec catalina.sh run"]
