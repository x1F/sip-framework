# SIP Test Kit (with Declarative Structure)

[TOC]

# Description

Using Test Kit with **Declarative Structure** is explained here and the content of this file should be treated as 
extension of base documentation
[SIP Test Kit](https://x1f.github.io/sip-framework/test-kit/).

Understanding material from the suggested link is a **prerequisite**!

# Features

### New test case definition parameter (connectorId)

Main addition is expansion of test case definition. In version 2 is possible to write test definitions only by using
`endpointId`. Version 3 introduced **Declarative Structure** and **Connectors** which are wrapping input and output endpoints.
In that manner, it is natural for Test Kit to enable creating test definitions with `connectorId` as well. Keep in
mind that using `connectorId` is possible only if **Declarative Structure** is enabled.

Both approaches can be used and combined within one test case definition, but for different endpoints!

That means you have to provide either `endpointId` or `connectorId` for one element within any of the test definition 
sections (`when-execute`, `with-mocks`, `then-expect`).

For example, this is possible:

```yaml
{
   "title": "Correct test case definition example",
   "when-execute": {
      "connectorId": "rest_connector",                  # input connector which targets its input endpoint
      "with": {
        "body": "Hello World"
      }
   },
   "with-mocks": [
      {
         "endpointId": "custom_processor_endpoint_id",
         "returning": {
            "body": "Good afternoon World"
         }
      }
   ],
   then-expect:
     - connectorId: "rest_connector"
       having:
         body: "Good afternoon World"
}
```

But, using both parameters `endpointId` and `connectorId` for same element is not possible:

```yaml
{
   "title": "Correct test case definition example",
   "when-execute": {
      "connectorId": "rest_connector",                          # wrong, choose only one
      "endpointId": "endpoint_id",                              # wrong, choose only one
      "with": {
        "body": "Hello World"
      }
   },
   "with-mocks": [
      {
        "connectorId": "jms_connector",                         # wrong, choose only one
         "endpointId": "custom_processor_endpoint_id",          # wrong, choose only one
         "returning": {
            "body": "Good afternoon World"
         }
      }
   ],
   then-expect:
     - endpointId: "rest_connector_endpoint_id"
       having:
         body: "Good afternoon World"
}
```

**Important note:**

If some external endpoints are written directly in Camel code and without using appropriate Connector, these endpoints
can be approached from test case definition only directly by using `endpointId`.

### Automatic conversion to POJO data model in Connectors

There is automatic conversion of given String JSON payload to the POJO object of **Connector's** data model. That is 
possible only for test case definition parts which are specified by using `connectorId`.
It is expected that correct format of JSON and correct payload are provided.

When using automatic conversions to POJO, different models are expected for different test definition sections:

1. In `when-execute` section your JSON payload should match with InboundConnector `requestModel`.
2. In `with-mocks` section your JSON payload should match with OutboundConnector `responseModel`.
3. In `then-expect` section your JSON payload should match with InboundConnector `responseModel` when validating final
response or with OutboundConnector `requestModel` when validating payload on mocks.

### Skip Camel input component

There is one more specific difference when using `endpointId` or `connectorId`, but only related to the
`when-execute` section.

When `endpointId` is used, testing payload is sent to the Camel consumer component (adapter's entry point) in 
InboundConnector. For this option, in most cases raw data payload should be sent, or data format which matches your 
Camel component transformation.

When `connectorId` is used, Camel consumer component (adapter's entry point) is skipped and testing payload is sent 
directly to the first processor of starting route in InboundConnector. For this option, in most cases matching 
JSON String payload should be sent.
