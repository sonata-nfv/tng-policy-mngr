FROM maven

WORKDIR /app

ADD . /app

RUN cd /app



#EXPOSE 8161
#EXPOSE 16161

#RUN curl -O http://mirror.downloadvn.com/apache/activemq/5.15.3/apache-activemq-5.15.3-bin.tar.gz
#RUN tar -xf apache-activemq-5.15.3-bin.tar.gz
#RUN ./apache-activemq-5.15.3/bin/activemq start

EXPOSE 8081

RUN mvn clean install
COPY target/tng-policy-mngr-1.5.0.jar /app/tng-policy-mngr-1.5.0.jar
CMD ["java","-jar","tng-policy-mngr-1.5.0.jar"]
