# SIP SOAP Starter

[TOC]

## Description

Starter project used to add necessary predefined dependencies for development of Adapters that use SOAP technology. It uses Apache CXF as an underlying library for SOAP implementation.
It can be used on both Inbound and Outbound connectors (to publish SOAP service or to consume SOAP service).
Usage of the soap-starter reduces boilerplate code and configuration needed to develop SOAP adapters. 

## Features

- Inbound connector Base
- Outbound connector Base
- Bean based configuration
- CXF generator plugin

## Installation

If **[SIP Archetype](./archetype.md)** was used during the adapter creation and **useSoap** question was answered positively then there is no need
to add any dependencies manually.

If that is not the case, or if the adapter was made before the SOAP support was added to SIP Framework,
then this manual steps are needed:
1. Add SIP SOAP Starter dependency to project _pom.xml_ (in the \<dependencies\> section):

```xml
<dependency>
  <groupId>one.x1f.sip.foundation</groupId>
  <artifactId>sip-soap-starter</artifactId>
</dependency>
```

2. Add Wsdl4j dependency to the project _pom.xml_ (in the \<dependencies\> section):
```xml
<dependency>
  <groupId>wsdl4j</groupId>
  <artifactId>wsdl4j</artifactId>
</dependency>
```

3. Add CXF Codegen Plugin to the project _pom.xml_ (in the \<build\>\<plugins\> section):

```xml
<plugin>
  <groupId>org.apache.cxf</groupId>
  <artifactId>cxf-codegen-plugin</artifactId>
</plugin>
```
## Usage

Exposed SOAP services (and their respective WSDL) can be seen on the address: **{base_url}/soap-ws** 

### Inbound connector

One SOAP service has, potentially, multiple methods that are exposed. Each method is represented by a separate SIP connector.
Prerequisite for the usage is to have a WSDL file that describes the service and generated necessary service classes.

To define an Inbound SOAP Connector you need to extend the `SoapInboundConnectorBase<>` class and annotate the class with standard @InboundConnector annotation.
SoapInboundConnectorBase<> class has a generic parameter which is the service class that is generated from the WSDL file.

Besides defining the service class in the generic, you need to define the method that is exposed by the connector by overriding the `getServiceOperationName()` method.

All the requests will be automatically handled by this connector and unmarshalled to the defined request model.

Example:
```java
@InboundConnector(
    connectorGroup = "soap-customer-service",
    requestModel = UpdateCustomer.class,
    integrationScenario = UpdateCustomerScenario.ID)
public class UpdateCustomerConnector extends SoapOperationInboundConnectorBase<CustomerService> {

  @Override
  public String getServiceOperationName() {
    return "updateCustomer";
  }

}
```

### Outbound connector

Similar rules apply to the Outbound connector as they do to the Inbound connector. You need to extend the `SoapOutboundConnectorBase<>` class and annotate the class with @OutboundConnector annotation.
SoapOutboundConnectorBase<> class has a generic parameter which is the service class and you need to extend getServiceOperationName() method to define the method.

Example:
```java
@OutboundConnector(
    connectorGroup = "soap-customer-service",
    requestModel = GetCustomersByName.class,
    responseModel = GetCustomersByNameResponse.class,
    integrationScenario = GetCustomersByNameScenario.ID)
public class GetCustomersByNameOutboundConnector
    extends SoapOperationOutboundConnectorBase<CustomerService> {

  @Override
  public String getServiceOperationName() {
    return "getCustomersByName";
  }

}
```

If there is no configuration for the default address of the service (see [Bean based configuration](./soap-starter.md#bean-based-configuration)), you need to define the address of the service by overriding the `getAddress()` method.
```java
  @Override
  protected String getServiceAddress() {
    return "http://localhost:8081/customerservice";
  }
```


### Bean based configuration

SOAP starter uses Apache CXF as an underlying library for SOAP implementation, and thus it has a lot of configuration options.

All the configuration options are available in the `CxfEndpoint` class and can be overridden by defining a bean of that type with the name of the service class (see [CxfEndpoint](https://www.javadoc.io/doc/org.apache.camel/camel-cxf/3.2.0/org/apache/camel/component/cxf/CxfEndpoint.html)).

SIP Framework will automatically find the inbound and outbound connectors that use the same service class and will use the configuration defined in the bean.

Example:
```java
  @Bean("CustomerService")
  public CxfEndpoint createCustomerServiceEndpoint() throws ClassNotFoundException {
    CxfEndpoint serviceEndpoint = new CxfEndpoint();
    serviceEndpoint.setAddress("http://localhost:8080/customerService");
    serviceEndpoint.setUsername("admin");
    serviceEndpoint.setPassword("admin");
    
    return serviceEndpoint;
  }
```

### CXF generator plugin

If the SIP archetype is used to create a project that uses the SOAP starter, the CXF generator plugin is already configured and ready to use.

Adding a .wsdl file to the adapter's resources and running `mvn compile` will generate all the CXF service classes.


If the archetype isn't used, then this configuration can be added to the .pom file of the adapter to enable the CXF generator plugin:
```xml
<plugin>
  <groupId>org.apache.cxf</groupId>
  <artifactId>cxf-codegen-plugin</artifactId>
</plugin>
```

_Note_: Please refer to the _sip-starter-parent_ pom.xml for the default plugin configuration. By default, it scans only *.wsdl files inside _resources_ folder.
That can be overridden in the adapter's own <plugin> configuration by providing an alternative configuration.
