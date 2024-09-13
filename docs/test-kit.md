# SIP Test Kit

[TOC]

# Description

SIP Test Kit is a testing system designed to work with SIP integration adapters. 
It's main purpose is to test how an adapter works internally, by mocking all external communication. 
It executes a test workflow that is defined in a specific file format, 
providing a possibility to create tests without code change. 
This file format is referred to as <i><b>TestCaseDefinition</i></b>. 
The flow itself and thus the TestCaseDefinition file is split into three phases (sections)

- when-execute - The test is executed by triggering one of adapter's endpoints
- with-mocks - External calls which are to be replaced with defined mock behaviour
- then-expect - The test outcome that should be compared with a defined expected outcome

# Features

## External endpoint mocking

This feature is active whenever a test is executed via Test Kit.
It will provide default behavior (forwarding the request without processing it) and mock all external endpoints.
Adding specific behavior for each endpoint can be done in test case definition in 'with-mocks' section.

## Validation

Validation is one of key properties to any testing system. SIP Test Kit supports validation for batch testing.
Validation is configured trough `then-expect` section of test case definition, by setting expected properties of endpoint
we want to validate. It could be the entering endpoint of the adapter, for example we want to validate HTTP response of the
adapter, or it could be any external system (mocked) endpoint, where we can validate the input that mocked endpoint has received;
this way we could validate, for instance, if a properly transformed file reached the outgoing FTP endpoint.  
Validation is performed on two levels, _*body*_ - where the data is validated and _*headers*_ - where metadata is validated. Body 
validation is performed as plain text comparison, XML comparison and JSON comparison. Binary payload is not yet supported.
Headers comparison is comparing textual key value maps. Both body and header validation support regex pattern as expected value.
Given that all SIP mocks are internal, meaning that the actual endpoint is replaced with the mock, any URI options defined 
on the mock will not apply and behavior produced by them is not possible to verify. 


## Reports

Each test case will be executed as its own unit test, so for each a test report will be generated
and printed in console.
First section of report shows the response. It will display the validated body and headers, as well as expected ones.
The following `endpoints` section is for mocked endpoint reports, with similar data as in the first section.

## Endpoint reports

Reports for all mocked endpoints will be provided, both with default (set by Test Kit) and user defined behavior.
For each test report in the `endpoints` section there will be an overview of request that each received.

# How to use

Test kit is enabled by default when the adapter is generated from SIP archetype.

A test class needs to be created inside test package of the adapter, which extends SIPBatchTest. 
Running this class would execute the test, but also it will be executed in the testing step during build time.

```java
public class TFWTest extends SIPBatchTest { }
```

Configuring Spring profile is needed. Make sure that the following configuration property inside your test resources is defined:
```yaml
spring:
  profiles:
    active: test
```

The next step is to provide the TestCaseDefinition file in yaml format in the `test/resources` package
(detailed description in next section):
``` yaml
test-case-definitions:
- TITLE: "Title of individual test"
  WHEN-execute:
    endpointId: "id of starting route under test"
    with:
      body: "Content that will be send as request body to the adapter input endpoint (plain text, JSON String)"
      headers:
        header-key: "Value of the header"
        another-header-key: "Another value"
  WITH-mocks:
  - endpointId: "id of Camel processor having out endpoint, the one that should be mocked"
    returning:
      body: "Response message that real out endpoint is expected to return"
      headers:
        header-key: "Value of the header"
  THEN-expect:
  - endpointId: "id of starting route under test"   # matches endpointId under test defined in WHEN-execute phase
    having:
      body: "Regex expression (java) which will be compered to the reponse of the test"
      headers:
        header-key: "Regex expression (java) which will be compered to the value of this header key"
  - endpointId: "endpointId of mocked endpoint"     # matches endpointId with defined or default mocked behavior
    having:
      body: "Regex expression (java) which will be compered to the request which arrived on the adapter"
      headers:
        header-key: "Regex expression (java) which will be compered to the header key value from request which arrived on the adapter"
```

Given that body can vary in length, it can be set as a reference to a file where the content resides. For example:
``` yaml
  WHEN-execute:
    endpointId: "id of starting route under test"
    with:
      body: "resource-file:filename.ext"
```
This approach is possible in all three phases of test: WHEN-execute, WITH-mocks and THEN-expect. It this case, payload 
files must be on classpath.
 
The default file for placing your test case definitions is `test-case-definition.yml` which can be found under
`test/resources` path within the SIP archetype generated adapter. When using default file, you can avoid any additional 
setting.

If filename or location needs to be customized, location of the TestCaseDefinition file can be provided to the Test Kit 
by setting the following property inside adapter configuration:
`sip.testkit.test-cases-path: myTests.yml`

Each test case definition will execute as a separate unit test with its own name and report displayed.

![alt-text](./img/ConsoleReport.png)

To disable SIP Test Kit, use the following configuration:
```yaml
sip:
  testkit:
    enabled: false
```


### Important development note!
To be able to fully utilize the Test Kit and write test case definitions properly, all the endpoints written with 
Camel code need to have a defined ID which will be referenced in the endpointId parameter of the test case.

**How to initialize `endpointId` for input endpoints in Camel code?**

```
from("rest:POST:/say/hello")      // Adapters input endpoint
    .routeId("say_hello_id")      // Providing endpointId for endpoint
                                  // Value 'say_hello_id' is endpointId which is used in test case definition
```

**How to initialize `endpointId` for output endpoints in  Camel code?**

Providing `endpointId` for output endpoints is done through simple Camel mechanism, just by providing id for the Camel 
processor which is calling the external endpoint.

```
from("...")
    .
    .
    .
    .to(http://otherSystem/hi)   // Adapter calling output endpoint
    .id("other_system_hi_id")    // Providing endpointId for endpoint
    .                            // Value 'other_system_hi_id' is endpointId which is used in test case definition
    .
    .
```

# Defining a Test Case

The TestCaseDefinition file starts with `test-case-definitions` property, which consists of a list of test cases.

## WHEN-execute

In this section a payload that should be sent to the adapter is defined.

`endpointId` refers to `routeId` of the starting route in adapter integration to which Test Kit sends a test request.
In `with` part you define content of the request you wish to send, meaning body and headers are added here.
The body can also be defined as plain text or JSON string, which matches appropriate POJO model.

```yaml
    WHEN-execute:
      endpointId: "rest-endpoint"
      with:
        body: "body of request"
```

## WITH-mocks

This section contains a list of endpoints for which we wish to have specific mocked response.
`endpointId` matches the endpoint which will be mocked.
`returning` should have body and headers, that we expect as the response from real call of external endpoint.

```yaml
    WITH-mocks:
      - endpointId: "external-service"
        returning:
          body: "response message from service"
```

## THEN-expect

Validation of adapter response is defined by setting the `endpointId` of endpoint under test and defining the expected
body or headers.

Validation of requests which outgoing endpoints received from the adapter is defined by setting the `endpointId` 
parameter to the endpoint's ID of mocked endpoint and defining the expected body or headers.

Body and header validation is possible by either defining regex (Java) expression or matching exact String content.

```yaml
    THEN-expect:
      - endpointId: "rest-endpoint"                 # matches endpoint under test
        having:
          body: "response .* from service"
          headers:
            CamelHttpResponseCode: "200"
      - endpointId: "external-service"              # matches endpoint with mocked behavior
        having:
          body: "body of request"
          headers:
            Authorization: "Basic .*"
```

# Supported Camel components

Following Camel components are supported for testing with Test Kit:

- REST
- SOAP (by using CXF)
- File
- FTP, FTPS, SFTP
- JMS
- Mail (imap, imaps, pop3, pop3s, smtp, smtps)
- Kafka

Please check the special notes for these components in following chapters, since there are some special conditions
which must be met.

### REST

Running Test Kit tests with REST component is straightforward. Keep in mind that REST headers could be provided in 
<i>when-execute</i> phase. There are no special considerations and our general [example](#complete-example) is shown 
with REST component. 

### SOAP

Testing SOAP requests is possible if the adapter is using Camel CXF component. When writing body payloads within TestCaseDefinition file,
it is required for all three phases (<i>when-execute</i>, <i>with-mocks</i>, <i>then-expect</i>) to provide soap xml
content as a String in one line and to do String escape. Be sure that you meet these requirements, otherwise tests 
could fail.

### File

File content should be provided as body in <i>when-execute</i> phase. 

Outside of testing, the File component will read the actual file from a specified location. 
In that case Exchange within the route will have File component specific exchange headers (for example `CamelFileName`, `CamelFileLength`, 
`CamelFileLastModified`... ). These headers are listed and explained in Camel File component docs.
If these headers are needed in tests for route processing, they can be provided in <i>when-execute</i> configuration 
part, under <i>headers</i> field. Header keys must be specified exactly the same as they are specified in Camel docs.

When specifying some File headers, Test Kit will automatically set a few other headers which are populated under same conditions and 
calculations as it is done in Camel File component:

1) `CamelFileLength` - calculated and set automatically according to body length.
2) `CamelFileName` - you can provide this header, and we will set `CamelFileNameConsumed` and `CamelFileNameOnly` 
additionally.
3) `CamelFileLastModified` - by providing this header, `CamelMessageTimestamp` will be set additionally.

Any of these automatically set headers can be overridden by setting the header explicitly.
All other File component headers, which are not mentioned here, can be provided by setting them explicitly.

### FTP, FTPS, SFTP

FTP, FTPS and SFTP have same behavior and testing approach in Test Kit. Security differences between them are not
relevant for testing and all three components are equally supported.

FTP component behaves mostly the same as File component. For better understanding, check the File component chapter.

On top of File component headers, FTP components have few more. However, benefits that we provide in setting 
headers automatically are different, and they are not the same as for File headers:

1) `CamelFileLength` - calculated and set automatically according to body length.
2) `CamelFileAbsolute` - calculated automatically based on component `directoryName` endpoint option.
3) `CamelFileParent` - calculated automatically based on component `directoryName` endpoint option.
4) `CamelFileHost` - calculated automatically based on component `host` endpoint option.
5) `CamelRemoteFileInputStream` - calculated automatically if endpoint option `streamDownload` is set.
6) `CamelFileName` - by providing this header, we set automatically headers `CamelFileNameConsumed`,
`CamelFileNameOnly`, `CamelFileRelativePath`, `CamelFileAbsolutePath` and `CamelFilePath`.
7) `CamelFileLastModified` - by providing this header, `CamelMessageTimestamp` will be set additionally.

Same rules as in File chapter for overriding and providing other headers apply here as well.

### JMS

When testing JMS component, there are a few limitations.

Original JMS `Message` and JMS `Session` are not provided within the exchange. That means if there is some logic within 
the route which is based on these elements, tests for that kind of route could not be created. Instead of original JMS
Message, we provide our custom implementation `SIPJmsTextMessage` which is there to support Test Kit testing purpose.

When providing camel JMS specified headers within test case definition, there are 3 following headers which could not 
be provided with simple String value, hence we skip adding these values and keep default ones (`JMSDestination`, 
`JMSReplyTo`, `JMSCorrelationIDAsBytes`).

If the logic of a route leans on JMS component type converters or custom type converter option, tests can not be 
created. Currently, type converters are not supported and only possible values are simple String or JSON String. 
But in case of json, type conversion should be done somewhere in the route and outside JMS component.

### Mail

All mail protocols are supported in Test Kit as well as their security variants.

Its usage is quite straightforward, the content of an email is set as body of the `when-execute` phase,
while `To`, `From`, `Subject`, etc. are defined as headers.

Attachments are not supported.

### Kafka

As for some of previous components, our support of Test Kit Camel Kafka component provides automatically setting Camel 
Kafka specific headers. Few automatically configured headers for each test are listed here:

1) `kafka.TOPIC` - has the same value as topic name in URI endpoint format
2) `kafka.TIMESTAMP` - generated current timestamp
3) `CamelMessageTimestamp` - generated current timestamp

Keep in mind that other specific headers from Camel Kafka API should be provided under double quotes and squared 
parenthesis. This is necessary because of Camel Kafka headers API which is using header keys with dot in between and
yaml format for <i><b>TestCaseDefinition</i></b> file will process dots as complex object with subfields. If you want 
this to be ignored, you need to specify these headers by using mentioned special format.

Examples: `"[kafka.TOPIC]"`, `"[kafka.OFFSET]"`, etc.

Another limitation is in data type conversions which same as for JMS component. Only String and JSON String are 
supported.

# Complete example

**Sample Route**

```java
public class SampleRestRoute extends RouteBuilder {
    public void configure() throws Exception {
        restConfiguration().component("servlet").port("8080").host("localhost");
        
        // Endpoint under test
        from("rest:POST:/say/hello").routeId("rest-endpoint").to("sipmc:bridge");

        from("sipmc:bridge")
                .routeId("http-route")
                .setHeader("Authorization", constant("Basic am9obkBleGFtcGxlLmNvbTphYmMxMjM="))
                .transform(body().append(" now looks better"))
                // Mocked output endpoint
                .to("http:localhost:8081/hello?bridgeEndpoint=true")
                .id("external-service");
    }
}
```

**Sample test case definition**
``` yaml
  - TITLE: "Test case 1"
    WHEN-execute:
      endpointId: "rest-endpoint"
      with:
        body: "body of request"
    WITH-mocks:
      - endpointId: "external-service"
        returning:
          body: "response message from service"
    THEN-expect:
      - endpointId: "rest-endpoint"                           # matches the endpoint under test
        having:
          body: "response .* from service"
          headers:
            CamelHttpResponseCode: "200"
      - endpointId: "external-service"                        # matches the already mocked endpoint within test
        having:
          body: "body of request now looks better"
          headers:
            Authorization: "Basic .*"
```

**Sample Console Report**

```
-----------------------------
| SIP Test Execution Report |
-----------------------------

    Test "Test case 1" executed successfully.
    Validation details:
      Body validation successful
      Header validation successful
    Actual response:
      Body: response message from service
      Validated headers:
      - CamelHttpResponseCode: 200
    Expected response:
      Body: response .* from service
      Headers:
      - CamelHttpResponseCode: 200
    Endpoints:
      Endpoint "external-service" was mocked
      Validation successful
      Validation details:
        Body validation successful
        Header validation successful
      Received:
       Body: body of request now looks better
       Headers:
        - Authorization: Basic am9obkBleGFtcGxlLmNvbTphYmMxMjM=
      Expected:
       Body: body of request now looks better
       Headers:
        - Authorization: Basic .*

-----------------------------
```

