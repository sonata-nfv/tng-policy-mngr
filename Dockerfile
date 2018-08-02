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
ENV MONGO_DB son-mongo

#ENV HOST_BROKER int-sp-ath.5gtango.eu
#ENV CATALOGUE int-sp-ath.5gtango.eu:4011
#ENV MONITORING_MANAGER int-sp-ath.5gtango.eu:8000
#ENV REPO qual-sp-bcn.5gtango.eu:4012

ENV HOST_BROKER son-broker
ENV CATALOGUE tng-cat:4011
ENV MONITORING_MANAGER son-monitor-manager:8000
ENV REPO tng-rep:4012


CMD ["java","-jar","tng-policy-mngr-1.5.0.jar"]
