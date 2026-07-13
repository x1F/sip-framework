-----------------------------
| SIP Test Execution Report |
-----------------------------

  <#if report.successfulExecution>
  Test "${report.testName}" executed successfully.
  </#if>
  <#if !report.successfulExecution>
  Test "${report.testName}" executed unsuccessfully.
  </#if>
  <#if report.adapterReport.validationResults?has_content>
    Validation details:
      <#list report.adapterReport.validationResults as validationResult>
      ${validationResult.message}
      </#list>
  </#if>
  <#if report.adapterReport.responseMessage?has_content>
    Actual response:
      Body: <#if report.adapterReport.responseMessage.body??>${report.adapterReport.responseMessage.body}</#if>
    <#if report.adapterReport.validatedHeaders?has_content>
      Validated headers:
      <#list report.adapterReport.validatedHeaders?keys as key>
      - ${key}: ${report.adapterReport.validatedHeaders[key]}
      </#list>
    </#if>
    <#if report.adapterReport.expectedResponse?has_content>
    Expected response:
    <#if report.adapterReport.expectedResponse.message.body??>
      Body: ${report.adapterReport.expectedResponse.message.body}
    </#if>
    <#if report.adapterReport.expectedResponse.message.headers?has_content>
      Headers:
      <#list report.adapterReport.expectedResponse.message.headers?keys as key>
      - ${key}: ${report.adapterReport.expectedResponse.message.headers[key]}
      </#list>
    </#if>
    </#if>
  </#if>
  <#if report.workflowExceptionMessage?? && report.workflowExceptionMessage?trim?has_content>
    ${report.workflowExceptionMessage}
  </#if>
  <#if report.adapterExceptionMessage?? && report.adapterExceptionMessage?trim?has_content>
    Adapter threw exception: ${report.adapterExceptionMessage}
  </#if>
  <#if report.mockReports??>
    Endpoints:
      <#list report.mockReports?keys as key>
      Endpoint "${key}" was mocked
      <#list report.mockReports[key] as endpointReport>
      Validation ${endpointReport.validated}
      <#if endpointReport.validationResults?has_content>
      Validation details:
        <#list endpointReport.validationResults as validationResult>
        ${validationResult.message}
        </#list>

      </#if>
      <#if endpointReport.actualMessage?has_content>
      Received:
       Body: <#if endpointReport.actualMessage.body??>${endpointReport.actualMessage.body}</#if>
      </#if>
       <#if endpointReport.validatedHeaders?has_content>
       Headers:
       <#list endpointReport.validatedHeaders?keys as mkey>
        - ${mkey}: ${endpointReport.validatedHeaders[mkey]}
       </#list>
       </#if>
      <#if endpointReport.expectedMessage?has_content>
      Expected:
       Body: <#if endpointReport.expectedMessage.body??>${endpointReport.expectedMessage.body}</#if>
      <#if endpointReport.expectedMessage.headers?has_content>
       Headers:
        <#list endpointReport.expectedMessage.headers?keys as mkey>
        - ${mkey}: ${endpointReport.expectedMessage.headers[mkey]}
        </#list>
      </#if>
      </#if>
      ---
          </#list>
      </#list>
  </#if>

-----------------------------