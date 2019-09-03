[![Build Status](https://jenkins.sonata-nfv.eu/buildStatus/icon?job=tng-api-gtw/master)](https://jenkins.sonata-nfv.eu/job/tng-profiler)

[![Join the chat at https://gitter.im/5gtango/tango-schema](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sonata-nfv/5gtango-sp)

<p align="center"><img src="https://github.com/sonata-nfv/tng-api-gtw/wiki/images/sonata-5gtango-logo-500px.png" /></p>

# 5GTango Policy Manager

Policies in 5GTANGO are considering deployment and operational aspects of network services over programmable infrastructure. Operational or runtime policies regard the runtime adaptation of network service mechanisms in order to optimally support the overall performance achieved.

The Policy Manager is the entity of the service platform responsible for runtime policies enforcement over the deployed network services. Policies enforcement regard mainly elasticity actions (scaling in and out VNFs), events identification and triggering of relevant alerts as well as security actions (e.g. application of a firewall rule). It consists of a set of components supporting the formulation of the policies and their enforcement, including the interfaces for interaction with the 5GTANGO monitoring framework,the message broker for publication and consumption of monitoring data, alerts and suggested actions, the catalogue and repository databases,the gatekeeper and the MANO orchestration mechanisms.

## Installing / Getting started

This component is implemented in [spring boot framework.boot, 2.0.3.RELEASE ](https://spring.io/)

### Installing from code
To have it up and running from code, please do the following:
```
$ git clone https://github.com/sonata-nfv/tng-policy-mngr.git # Clone this repository
$ cd tng-policy-mngr# Go to the newly created folder
$ mvn clean install # Install dependencies
$ java -jar -Dspring.profiles.active=yourProfile target/tng-policy-mngr-1.5.0.jar # use the development profile or create a new one
```
Note: See the [Configuration](https://github.com/sonata-nfv/tng-policy-mngr/#configuration) section below for other environment variables that can be used.

Everything being fine, you'll have a server running on that session, on port 8081. You can access it by using curl, like in:
```
$ curl <host name>:8081/api/vi
```
 
### Installing from the Docker container
In case you prefer a docker based development, you can run the following commands (bash shell):

``` 
  $docker-compose up --build -d
```

With these commands, you:

Run the MongoDB container within the tango network;
Run the RabbitMQ container within the tango network;
Run the tng-policy-mngr container
  
This setup is ideal in case you want to test and develop the tng-policy-mngr apart from the rest of the sonata components.

## Developing

To contribute to the development of this 5GTANGO component, you may use the very same development workflow as for any other 5GTANGO Github project. That is, you have to fork the repository and create pull requests.

###  Built With 

* Sonata Service Platform local installation (recommended) or vpn connection to SP environment 
* [Docker >= 1.13](https://www.docker.com/)
* [Docker compose version 3](https://docs.docker.com/compose/)
* [Java version 1.8](https://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html) - The programming language used
* [Maven](https://maven.apache.org/) - Dependency Management
* [Drools version 7.7.0](https://www.drools.org/) - Business Rules Management System (BRMS) solution used so as to enforce policies
* [Spring boot Framework 2.0.3 RELEASE](https://spring.io/projects/spring-boot) - Used application framework

### Setting up Dev

Developing this micro-service is straightforward with a low amount of necessary steps:
* Update properly the [application.properties file](https://github.com/sonata-nfv/tng-policy-mngr/blob/master/src/main/resources/application-development.properties) 
* Open the project with an Integrated development environment (IDE) that support java (ex.[Netbeans](https://netbeans.org/))

### Submiting changes
Changes to the repository can be requested using [this](https://github.com/sonata-nfv/tng-policy-mngr/issues) repository's issues and [pull requests](https://github.com/sonata-nfv/tng-policy-mngr/pulls) mechanisms.

## Versioning
The most up-to-date version is v4. For the versions available, see the link to tags on this repository.

## Api Reference 

Policy Manager APIs can be found at the  [central API documentation of the SONATA orchestrator](https://sonata-nfv.github.io/tng-doc/?urls.primaryName=5GTANGO%20POLICY%20MANAGER%20REST%20API) and also at the [wiki page](https://github.com/sonata-nfv/tng-policy-mngr/wiki/API-reference) of the tng-policy-mngr component





#### Build and run tng-policy-mngr
```bash
docker-compose up --build -d
```
Sample runtime policy descriptors can be found at [policy descriptor examples](https://github.com/sonata-nfv/tng-schema/tree/master/policy-descriptor/examples) based at [policy descriptor schema](https://github.com/sonata-nfv/tng-schema/blob/master/policy-descriptor/policy-schema.yml).


## Licensing

This 5GTANGO component is published under Apache 2.0 license. Please see the LICENSE file for more details.

#### Lead Developers

The following lead developers are responsible for this repository and have admin rights. They can, for example, merge pull requests.

- Eleni Fotopoulou ([@elfo](https://github.com/efotopoulou))
- Anastasios Zafeiropoulos ([@tzafeir ](https://github.com/azafeiropoulos))

#### Feedback-Chanel
* You may use the mailing list [sonata-dev-list](mailto:sonata-dev@lists.atosresearch.eu)
* You may use the GitHub issues to report bugs
* Gitter room [![Join the chat at https://gitter.im/5gtango/tango-schema](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sonata-nfv/5gtango-sp)
