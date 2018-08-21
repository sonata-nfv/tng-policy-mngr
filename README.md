# 5GTango policy manager

Policies in 5GTANGO are considering deployment and operational aspects of network services over programmable infrastructure. Operational or runtime policies regard the runtime adaptation of network service mechanisms in order to optimally support the overall performance achieved. A Policies Editor and Policies Manager are under development for this purpose supporting runtime policies definition and enforcement. Enforcement is going to be based on suggestion of actions produced by an inference engine taking into account the defined rules and real-time information provided by the set of active monitoring probes.

Every runtime runtime policy is defined upon a specific NS. Based on this, for each NS, a set of rules per policy can be defined. Each rule consists of the expressions part, denoting a set of conditions to be met and the actions part denoting actions upon the fulfillment of the conditions. The conditions may regard resources consumption aspects, software-specific aspects, status of a VNF or a NS, while the set of actions may regard resources allocation/disposal aspects, live migration and mobility aspects, horizontal scaling aspects and network functions management or activation aspects.

A network service may be associated with a set of runtime policies, however only one can be active during its deployment and execution time. A runtime policy can be defined by the software developer of the NS, having detailed knowledge of the business logic of the developed software, or the network operator/service platform manager having knowledge of the available resources and the set of active SLAs. Knowledge of SLAs can lead to description of set of rules guaranteeing that such SLAs can be respected in the maximum possible way.

Policies enforcement is realized through a rule-based management system. Based on the set of activated rules associated with the deployed network services and the real time collection of data, inference is realized leading to policies enforcement. Such enforcement takes place over the deployed network services, while the inference follows a continuous match-resolve-act approach. In the match phase, the set of defined conditions are examined, leading to the set of rules that have to be activated, while in the resolve phase, conflict resolution over the set of rules to be activated is taking place. The act phase concerns the triggering of the suggested actions aiming at the guidance of various orchestration components of the 5GTANGO service platform. For each rule, an inertial period is introduced denoting the time period that the same rule should not be re-triggered.

## Documentation - APIs and swagger support


Following table shows the API endpoints of the Policy Manager.
tng-policy-mngr supports its own swagger endpoint at http://<ip_adress>/swagger-ui.html

| Action | HTTP Method | Endpoint |
| --------------- | ------- | -------------------------------------------- |
| GET a list of all Runtime policies | `GET` |`/api/v1`|
| Create a Runtime Policy | `POST` |`/api/v1`|
| GET a Runtime policy | `GET` |`/api/v1/{policy_uuid}`|
| Update a Runtime Policy | `PUT` |`/api/v1`|
| Delete a Runtime Policy | `DELETE` |`/api/v1/{policy_uuid}`|
| Bind a Runtime Policy to an SLA | `PATCH` |`/api/v1/bind/{policy_uuid}`|
| Define a Runtime Policy as default | `PATCH` |`/api/v1/default/{policy_uuid}`|
| GET a list of all Recommended Actions | `GET` |`/api/v1/actions`|
| Create the Placement Policy | `POST` |`/api/v1/placement`|
| GET the Placement policy | `GET` |`/api/v1/placement`|
| Pings to policy manager | `GET` |`/api/v1/pings`|

## Development

To contribute to the development of this 5GTANGO component, you may use the very same development workflow as for any other 5GTANGO Github project. That is, you have to fork the repository and create pull requests.

### Setup development environment

####  Built With (Dependencies)

* Sonata Service Platform local installation (recommended) or vpn connection to SP environment 
* [Docker >= 1.13](https://www.docker.com/)
* [Docker compose version 3](https://docs.docker.com/compose/)
* [Java version 1.8](https://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html) - The programming language used
* [Maven](https://maven.apache.org/) - Dependency Management
* [Drools version 7.7.0](https://www.drools.org/) - Business Rules Management System (BRMS) solution used so as to enforce policies
* [Spring boot Framework 2.0.3 RELEASE](https://spring.io/projects/spring-boot) - Used application framework

#### Build and run tng-policy-mngr
```bash
docker-compose up --build -d
```

Sample runtime policy descriptors can be found at [policy descriptor examples](https://github.com/sonata-nfv/tng-schema/tree/master/policy-descriptor/examples) based at [policy descriptor schema](https://github.com/sonata-nfv/tng-schema/blob/master/policy-descriptor/policy-schema.yml). Policy rules defined at policy descriptors are translated at drool rules. Following can be seen a drool rule example:

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

### License

This 5GTANGO component is published under Apache 2.0 license. Please see the LICENSE file for more details.

### Lead Developers

The following lead developers are responsible for this repository and have admin rights. They can, for example, merge pull requests.

- Eleni Fotopoulou ([@elfo](https://github.com/efotopoulou))
- Anastasios Zafeiropoulos ([@tzafeir ](https://github.com/azafeiropoulos))

### Feedback-Chanel

* Please use the GitHub issues to report bugs.
