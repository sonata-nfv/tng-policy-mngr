FROM maven

WORKDIR /app
ADD . /app
RUN cd /app

RUN mvn clean install

FROM openjdk
EXPOSE 8081
COPY target/tng-policy-mngr-1.5.0.jar /app/tng-policy-mngr-1.5.0.jar
CMD ["java","-jar","tng-policy-mngr-1.5.0.jar"]
