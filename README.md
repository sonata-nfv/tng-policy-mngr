[![Build Status](https://jenkins.sonata-nfv.eu/buildStatus/icon?job=tng-api-gtw/master)](https://jenkins.sonata-nfv.eu/job/tng-profiler)

[![Join the chat at https://gitter.im/5gtango/tango-schema](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sonata-nfv/5gtango-sp)

<p align="center"><img src="https://github.com/sonata-nfv/tng-api-gtw/wiki/images/sonata-5gtango-logo-500px.png" /></p>

# 5GTango Policy Manager

Policies in 5GTANGO are considering deployment and operational aspects of network services over programmable infrastructure. Operational or runtime policies regard the runtime adaptation of network service mechanisms in order to optimally support the overall performance achieved.

The Policy Manager is the entity of the service platform responsible for runtime policies enforcement over the deployed network services. Policies enforcement regard mainly elasticity actions (scaling in and out VNFs), events identification and triggering of relevant alerts as well as security actions (e.g. application of a firewall rule). It consists of a set of components supporting the formulation of the policies and their enforcement, including the interfaces for interaction with the 5GTANGO monitoring framework,the message broker for publication and consumption of monitoring data, alerts and suggested actions, the catalogue and repository databases,the gatekeeper and the MANO orchestration mechanisms.

## Documentation - APIs 

Policy Manager APIs can be found here  [here](https://sonata-nfv.github.io/tng-doc/?urls.primaryName=5GTANGO%20POLICY%20MANAGER%20REST%20API)
Following table shows the API endpoints of the Policy Manager.
tng-policy-mngr supports its own swagger endpoint at http://<ip_adress>/swagger-ui.html

| Action | HTTP Method | Endpoint |
| --------------- | ------- | -------------------------------------------- |
| GET a list of all Runtime policies | `GET` |`/api/v1`|
| GET a number of all Runtime policies | `GET` |`/api/v1/counter`|
| GET a list of all Runtime policies matching a specific network service | `GET` |`/api/v1?ns_uuid={value}`|
| Create a Runtime Policy | `POST` |`/api/v1`|
| GET a Runtime policy | `GET` |`/api/v1/{policy_uuid}`|
| Update a Runtime Policy | `PUT` |`/api/v1`|
| Delete a Runtime Policy | `DELETE` |`/api/v1/{policy_uuid}`|
| Bind a Runtime Policy to an SLA | `PATCH` |`/api/v1/bind/{policy_uuid}`|
| Define a Runtime Policy as default | `PATCH` |`/api/v1/default/{policy_uuid}`|
| GET a list of all Recommended Actions | `GET` |`/api/v1/actions`|
| GET a number of all Recommended Actions | `GET` |`/api/v1/actions/counter`|
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
rule "ElasticityRuleScaleOut"
when 
	$m1 := LogMetric( vnf_name== "haproxy-vnf" && value== "mon_rule_vnf_haproxy-vnf_vdu_vdu01_haproxy_backend_sespsrv" ) from entry-point "MonitoringStream"  
then
	insertLogical( new ElasticityAction($m1.getNsrid(),"squid-vnf","eu.5gtango","0.1",ScalingType.addvnf,"1","null",Status.not_send)); 

end

rule "ElasticityRuleScaleIn"
when
	(
	$m1 := LogMetric( vnf_name== "haproxy-vnf" && value== "mon_rule1_vnf_haproxy-vnf_vdu_vdu01_haproxy_backend_sespsrv" ) from entry-point "MonitoringStream" and
	$m1 := LogMetric( vnf_name== "haproxy-vnf" && value== "mon_rule_vnf_haproxy-vnf_vdu_vdu01_haproxy_backend_actsrvs" ) from entry-point "MonitoringStream" ) 
then
	insertLogical( new ElasticityAction($m1.getNsrid(),"squid-vnf","eu.5gtango","0.1",ScalingType.removevnf,"1","random",Status.not_send)); 
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
