# tng-policy-mngr

Policies in 5GTANGO are considering deployment and operational aspects of network services over programmable infrastructure and mainly over the created network slices. Operational or runtime policies regard the runtime adaptation of network service mechanisms in order to optimally support the overall performance achieved. A Policies Editor and Policies Manager is going to be developed for this purpose supporting runtime policies definition and enforcement. Enforcement is going to be based on suggestion of actions produced by an inference engine taking into account the defined rules and real-time information provided by the set of active monitoring probes.

## Documentation - APIS


Following table shows the API endpoints of the Policy Manager.

| Action | HTTP Method | Endpoint |
| --------------- | ------- | -------------------------------------------- |
| GET a list of all runtime policies | `GET` |`/api/v1`|
| Create a Policy | `POST` |`/api/v1`|
| GET a policy | `GET` |`/api/v1/{policy_uuid}`|
| Update a Policy | `PUT` |`/api/v1`|
| Delete a Policy | `DELETE` |`/api/v1/{policy_uuid}`|
| Bind a Policy to an SLA | `PATCH` |`/api/v1/{policy_uuid}`|
| Define a Policy as default | `PATCH` |`/api/v1/{policy_uuid}`|

## Development

To contribute to the development of this 5GTANGO component, you may use the very same development workflow as for any other 5GTANGO Github project. That is, you have to fork the repository and create pull requests.

### Setup development environment
##### Prerequisites

* Sonata Service Platform local installation (recommended) or vpn connection to SP environment* 
* Docker >= 1.13
* Docker compose version 3

##### Build and run tng-policy-mngr
```bash
docker-compose up --build -d
```

Sample runtime policy descriptors can be found at [[policy descriptor examples|https://github.com/sonata-nfv/tng-schema/tree/master/policy-descriptor/examples]] based at [[policy descriptor schema|https://github.com/sonata-nfv/tng-schema/blob/master/policy-descriptor/policy-schema.yml]]. Policy rules defined at policy descriptors are translated at drool rules. Following can be seen a drool rule example:
```
rule "highTranscodingRateRule"
when
    (
    $tot0 := java.lang.Double( $tot0 >=150 ) from accumulate(     
    $m0 := MonitoredComponent( name== "transcodingNode" && metric== "transcondingRate" ) over window:time(1m)from entry-point "MonitoringStream" ,
        average( $m0.getValue() )  ) and
    $tot1 := java.lang.Double( $tot1 >=3 ) from accumulate(     
    $m1 := MonitoredComponent( name== "transcodingNode" && metric== "queueSize" ) over window:time(1m)from entry-point "MonitoringStream" ,
        average( $m1.getValue() )  ) ) 
then
    insertLogical( new Action("pilotTranscodingService","transcodingNode",RuleActionType.COMPONENT_LIFECYCLE_MANAGEMENT,"2","infrastracture-start")); 

end
```

### CI Integration

All pull requests are automatically tested by Jenkins and will only be accepted if no test is broken.

## License

This 5GTANGO component is published under Apache 2.0 license. Please see the LICENSE file for more details.

---
#### Lead Developers

The following lead developers are responsible for this repository and have admin rights. They can, for example, merge pull requests.

- Eleni Fotopoulou ([@elfo](https://github.com/efotopoulou))
- Anastasios Zafeiropoulos ([@tzafeir ](https://github.com/azafeiropoulos))


#### Feedback-Chanel

* Please use the GitHub issues to report bugs.

### Prerequisites to run locally (obsolete)


Before moving on, make sure you have also installed Apache activemq the latest.

Install Apache activemq (5.15.0)
```shell
    wget http://mirror.downloadvn.com/apache/activemq/5.15.0/apache-activemq-5.15.0-bin.tar.gz
    sudo tar -xf apache-activemq-5.15.0-bin.tar.gz  -C /opt/
    sudo /opt/apache-activemq-5.15.0/bin/activemq start
```

1. Build the project
```shell
cd tng-policy-mngr
mvn clean install
```

2. Run the executable jar
```shell
java -jar target/tng-policy-mngr-1.5.0.jar

or

nohup java -jar target/tng-policy-mngr-1.5.0.jar > tng-policy-mngr.log 2>&1 &

or

nohup java -jar -Djava.security.egd=file:/dev/./urandom tng-policy-mngr/target/tng-policy-mngr-1.5.0.jar > /home/ubuntu/tng-policy-mngr.log 2>&1 &
```






