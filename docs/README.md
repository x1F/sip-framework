# System Integration Platform Framework

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=IKOR-GmbH_sip-framework&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=IKOR-GmbH_sip-framework)
[![Impressum](https://img.shields.io/badge/Imprint-ikor.one-blue)](https://ikor.one/impressum/)

The System Integration Platform (SIP) Framework is an IKOR product
developed from 10 project years of experience with standardized integration of core and peripheral systems.

The framework enables building light-weight integration adapters to achieve a technical and non-technical decoupling of
systems, using microservices and is therefore highly scalable.

It builds on [Apache Camel Framework](https://camel.apache.org/manual/) and extends it with a lot of usable features to create a standardized integration
approach for all adapters.

[TOC]

## What is SIP

**S**ystem **I**ntegration **P**latform is a combination of SIP framework, SIP management application and various implementation
guidelines and best practices. Combined they create an advantage when developing microservice integration adapters.
SIP adapters are specially designed as middleware integrators of specific subdomains of an enterprise. SIP project structure
and implementation guidelines provide higher degree of code consistency across different adapter instances, making them
much easier to maintain, manage and monitor. The goal is to have minimal set of restrictions of developer's freedom with
maximum comfort and efficiency while developing.

To start developing with SIP, we should first get familiar with its basic concepts.
Let's take a very simple scenario as an example. Say we have two systems working on the same domain (Partner, Policy, Billing
etc.), but they were never designed to work together, and suddenly there is a need to connect them.

![Image of Unconnected systems image](./img/SIP_readme_systems.svg?raw=true "Unconnected systems")

Both systems expose APIs, which are mutually not compatible, both by data model and/or communication technology
they use. SIP is designed as a standalone middleware app with a sole purpose to resolve exactly this kind of problems in
a flexible and standardized way. Actual integration scenarios may include more than two APIs, or even more than two
systems, but all the principles apply equally to such scenarios.

![Image of SIP connected systems](./img/SIP_readme_adapter.svg?raw=true "SIP connected systems")

The integration logic is divided into different packages: 

**Common**

The **Common** package provides the common data model inside domain for both systems and common util functionalities.

**Models**

**Models** package should contain only simple Java objects representing the respective domain
in which the system connectors of an adapter operate, and should not contain any integration logic.
All connectors should adapt the data models of their systems to or from this common model, 
depending on data flow, due to their incompatibilities. 
The domain can be seen as a kind of contract between the different system connectors,
which ensures that they can communicate with each other. 
It contains common data model which uniforms the data models from all integration sides.

**Connector groups**

Each **Connector group** is designed to contain connectors which communicate with the associated external systems, 
thus all classes found in a connector group should only relate to their integration side. 
To enable this, their local domain objects are aligned with the API of an
external systems they communicate with. In order to send a message from one system 
to another, the local domain objects must be mapped to the shared domain object. Furthermore, this means that a message
from system A is mapped to the shared domain object and then from the shared domain object to the model of system B and
vice versa, due to their bidirectional nature.
This also means that changes on one of adapter's connector does not necessarily require
changes of the other. That's especially important if the affected connector is reused across multiple adapters.
Domain A and domain B packages in each connector are optional,
since integrated systems use the same communication data model sometimes.

Each connector will have the following structure:

- `config` - a place for any configuration classes
- `connectors` - here we should define inbound and outbound connectors
- `transformers` - it should contain classes for adapting the connector model to common domain model.
- `models` - (optional) it may contain the data model of the system.
- `processors` - camel processors
- `validators` - camel validators

## Usage

### Framework components

- **[sip-archetype](./archetype.md)** - Archetype creates a basic SIP Adapter project with a defined structure and necessary dependencies. Project is created by executing single maven command.
- **[sip-core](./core.md)** - Core project for base SIP functionalities.
- **[sip-starter-parent](./starter-parent.md)** - This project takes care of versions for Spring Boot and Camel dependencies.
- **[sip-integration-starter](./integration-starter.md)** - Starter project adding necessary predefined dependencies for integration adapters.
- **[sip-soap-starter](./soap-starter.md)** - Starter dependency for adapter that use SOAP.
- **[sip-security](./security.md)** - Security in SIP framework.
- **[sip-test-kit](./test-kit.md)** - Tool for integration testing.

### Framework features

Framework provides different features some of which are enabled by default. All the features are customizable and can be
overwritten or turned off by configuration. More about how to use them you can find under the corresponding module's
documentation.

- **[Actuator health check and metrics](./core.md#actuator-health-check-and-metrics)** - Out-of-the-box health checks for HTTP(S), JMS and FTP, SFTP and FTPS endpoints.
- **[Working with routes in runtime](./core.md#working-with-routes-in-runtime)** - Dynamical changing routes lifecycle.
- **[Logging Translation](./core.md#logging-translation)** - Translation of logging messages.
- **[Changing log level programmatically](./core.md#changing-log-level-programmatically)** - Dynamical changing of log level.
- **[Exchange tracing](./core.md#exchange-tracing)** - Tracing and storing exchanges on Camel Processor level.
- **[OpenAPI Descriptor](./core.md#openapi-descriptor)** - Built-in OpenAPI.
- **[Declarative Structure in actuator adapter definition endpoint](./core.md#declarative-structure-in-actuator-adapter-definition-endpoint)** - Detailed view of Declarative Structure.
- **[SIP Security](./security.md)** - Includes SSL setup, base and x509 authentication
- **[SIP Test Kit](./test-kit.md)** - Provides ability to run integration tests inside SIP adapters, define mocks for endpoints and generate test reports.
- **[SIP Test Kit Declarative](./test-kit-declarative.md)** - Run integration tests inside SIP adapters in Declarative Structure.

## Getting started

Before development, check the following [Installation guide](installation.md).

Once you have your adapter you can do the following steps:

- Run `mvn clean install`
- Create common Data Models inside models package
- Add necessary dependencies
- Add Connectors inside "connectors" package in connector groups
- Add classes which transform system data models to or from common domain model in "transformers" package in connector groups (if needed)
- Add any configuration classes for a specific system inside "config" package in connector groups
- Add general integration configuration in application.yml found inside resources
- Run SIPApplication found inside base package
- After the application is up and running you can check SIP's management API under [localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Framework version upgrade

If you need to upgrade your adapter to a newer SIP Framework version, please follow this 
[guide](./framework_version_upgrade.md).

### Adding additional Camel starters to the project

The Maven dependency management techniques implemented in the SIP archetype give you the ability to easily add new
camel-starter dependencies to integration adapters.

Should you need additional [Apache Camel Components](https://camel.apache.org/components/latest/) in the project, it is
enough to add their Spring Boot starter dependency, without version number - as it is managed in the parent Maven module
of the SIP integration adapter.

For instance, to add ArangoDb Camel component, it is enough to add the following dependency to the project:

```xml
<dependency>
    <groupId>org.apache.camel.springboot</groupId>
    <artifactId>camel-arangodb-starter</artifactId>
</dependency>
```

The same stands for adding Spring Boot starters - as they are managed in the parent Maven module they too can be added
without explicitly stating version numbers. As a matter of fact, the overall dependency management performed by the
Spring Boot is in place in integration adapters too.

### Adding new Connector Groups

By using the SIP archetype to create a new SIP adapter, by default there are two connector groups, designed to make it
more convenient to integrate systems. In case there are more than two systems, which need to be integrated, you need to add
additional connector groups to the project structure.

The project structure usually looks like this:

```text
fancy-sip-adapter
├───src/main/java/<package-path>
│   ├───common
│   │   ├───config
│   │   └───util
│   ├───connectorgroups
│   │   ├───connectorGroup1
│   │   │   ├───config
│   │   │   ├───transformer
│   │   │   ├───processors
│   │   │   ├───connectors
│   │   │   ├───validators
│   │   │   └───models
│   │   └───connectorGroup2
│   │       ├───config
│   │       ├───transformer
│   │       ├───processors
│   │       ├───connectors
│   │       ├───validators
│   │       └───models
│   ├───scenarios
│   │   ├───models
│   │   └───definitions
│   └───SIPApplication.java
└───pom.xml
```

Easiest way would be just copying an existing connector package which contains the pre-made structure.
This can also be done manually by creating a new package. 
It is important that these packages are contained in connectors package.

### Development Tips

**Endpoint Configuration**

When it comes to working with URIs in routes, it is recommended to use property placeholders, which makes the routes configurable.
Additionally, it would make much sense to follow suggested configuration convention for defining endpoint configuration.

```yaml
endpoint:
  <in/out>:
    <external-system>:
      <endpoint>: # optional - if more endpoints on single external-system are involved in integration
        id: <externalSystem>
        uri: ftp://...
```

`<in/out>` corresponds to consumers and producers respectively.
This means in case a message is received through a route using "from", then it is a consumer and "in" is used.
On the other hand, it is a producer when a message is sent via "to". In this case, "out" is used as key in the configuration file.  

`<external-system>` should match the name of the system or client the adapter is communicating with.  

`<endpoint>` in case there are multiple endpoints for an adapter that uses the same domain and external system, additional identification
is required. For this purpose we use an additional endpoint key to provide distinction.  

For example:

```yaml
endpoint:
  in:
    my-assurance-co:
      id: my-assurance-co
      uri: ftp://...
  out:
    their-assurance-co:
      id: their-assurance-co
      uri: https://...
```

Using this configuration can be easily achieved in Camel by following their placeholder syntax.
Here's what the example from above would look like in the Camel route:

```java
from("{{endpoint.in.my-assurance-co.uri}}")
    .id("{{endpoint.in.my-assurance-co.id}}")
    .to(...);

from(...)
    .process(...)    
    .to("{{endpoint.out.their-assurance-co.uri}}")
    .id("{{endpoint.out.their-assurance-co.id}}")
```

If this convention is followed in the configuration, it leads to a unified structure that makes it possible
to identify at a single glance which systems are communicating with each other and which communication technologies are 
being used.
It also makes routes more descriptive and adapters much easier to maintain.

**Setting processor and route IDs**

As we can see each external endpoint, definition is followed by explicit setting of id. Although it's not mandatory,
doing so is highly recommended especially in case of outgoing endpoints. This will provide a reference of the external
endpoints, which can be used for different functionalities, like custom health check, testing with test-kit or other 
that are yet to come.
Notice that in case of incoming endpoints (those in "from" statement), following id refers to the routeId.

### Configuration properties

By default, the following properties are included in the SIP Framework. To override them, simply add them to your configuration file with your desired values.
If you're using a YAML configuration file (typically found in the application module), make sure to adapt the properties to the correct YAML format.
A complete YAML configuration file with all available properties can be found [here](./sip-config-properties.yaml).

Name | Description | Value | Default |
--- | --- | --- | --- |
sip.core.translation.enabled | Enable SIP translation | boolean | true |
sip.core.translation.fileLocations | Sets locations of translation bundles | List | classpath:translations/translated-messages, classpath:translations/sip-core-messages |
sip.core.translation.default-encoding | Sets default encoding | String | UTF-8 |
sip.core.translation.fallback-to-system-locale | Use system language if none defined | boolean | false |
sip.core.translation.use-code-as-default-message | If key is not assigned use it in message | boolean | true |
sip.core.translation.lang | Set language of log messages | String | en |
sip.core.tracing.enabled | Enable SIP tracing and trace history | boolean | false |
sip.core.tracing.exchange-formatter.{property-name} | Sets value for specific property in [ExchangeFormatter](https://www.javadoc.io/static/org.apache.camel/camel-support/3.0.0/org/apache/camel/support/processor/DefaultExchangeFormatter.html) | / | / |
sip.core.tracing.trace-type | Sets how tracer should behave | String | "*" |
sip.core.metrics.external-endpoint-health-check.enabled | Enable health status calculation | boolean | true |
sip.core.metrics.external-endpoint-health-check.scheduler.fixed-delay | Sets health check execution interval | Integer | 900000 |
sip.core.metrics.external-endpoint-health-check.scheduler.initial-delay | Sets health check execution initial delay | Integer | 5000 |
sip.core.actuator.extensions.health.enabled | Enable additional SIP Health check | boolean | true |
sip.core.actuator.extensions.info.enabled | Expose additional adapter information through /actuator/info | boolean | true |
sip.core.actuator.adapter-routes.enabled | Enable controlling lifecycle of routes | boolean | true |
management.info.camel.enabled | Enable basic camel info under /actuator/info endpoint | boolean | false |
management.endpoints.web.exposure.include | Set which endpoints are included | String | health,info,metrics,loggers,prometheus |
management.endpoint.health.show-details | Enable health details in actuator | String | always |
springdoc.show-actuator | Show actuator API in Swagger docs| boolean | true |
springdoc.api-docs.path | Custom path to API docs | String | /api-docs |
springdoc.swagger-ui.path | Custom path to Swagger | String | /swagger-ui.html |
springdoc.swagger-ui.disable-swagger-default-url | Disables default petstore API in swagger | boolean | true |
sip.testkit.enabled | Enable SIP testkit | boolean | true |
sip.testkit.test-cases-path | Define path for file with test cases | String | test-case-definition.yml
sip.security.ssl.enabled | Enable [SIP SSL security](https://ikor-gmbh.github.io/sip-framework/security/#configuration) | boolean | false |
sip.security.ssl.server.client-auth | Enable authentication type - Possible values: NONE, WANT or NEED | String | none
sip.security.ssl.server.key-store | Location of keystore | String | / |
sip.security.ssl.server.key-store-password | Password of keystore | String | /
sip.security.ssl.server.key-store-type | Type of keystore file | String | / |
sip.security.ssl.server.key-alias | The alias (or name) under which the key is stored in the keystore | String | / |
sip.security.ssl.server.key-password | Password of the key | String | / | 
sip.security.ssl.client.enabled | Enable separate client certification | boolean | false |
sip.security.ssl.client.key-store | Location of client keystore | String | /
sip.security.ssl.client.key-store-password | Password of client keystore | String | /
sip.security.ssl.client.key-store-type | Type of client keystore file | String | / |
sip.security.ssl.client.key-alias | The alias (or name) under which the key is stored in the client keystore | String | / |
sip.security.ssl.client.key-password | Password of the client key | String | / |
