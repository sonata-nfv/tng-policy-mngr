FROM maven

WORKDIR /app
ADD . /app
RUN cd /app

RUN mvn clean install

FROM openjdk
EXPOSE 8081
COPY --from=0  /app/target/tng-policy-mngr-1.5.0.jar /app/tng-policy-mngr-1.5.0.jar
COPY --from=0  /app/descriptors /app/descriptors
COPY --from=0  /app/rules /app/rules
WORKDIR /app
CMD ["java","-jar","tng-policy-mngr-1.5.0.jar"]
