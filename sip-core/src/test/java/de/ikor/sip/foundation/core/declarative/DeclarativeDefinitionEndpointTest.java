package de.ikor.sip.foundation.core.declarative;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import de.ikor.sip.foundation.core.actuator.declarative.model.ConnectorGroupInfo;
import de.ikor.sip.foundation.core.actuator.declarative.model.ConnectorInfo;
import de.ikor.sip.foundation.core.actuator.declarative.model.DeclarativeStructureInfo;
import de.ikor.sip.foundation.core.actuator.declarative.model.IntegrationScenarioInfo;
import de.ikor.sip.foundation.core.apps.declarative.SimpleAdapter;
import java.io.IOException;
import java.util.List;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(
    classes = {SimpleAdapter.class},
    properties = {"camel.rest.binding-mode=auto", "camel.openapi.enabled=false"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisableJmx(false)
@DirtiesContext
class DeclarativeDefinitionEndpointTest {

  @Autowired private ObjectMapper mapper;

  @LocalServerPort private int localServerPort;

  private final int CONNECTORGROUPS_IN_TEST_ADAPTER = 2;
  private final int SCENARIOS_IN_TEST_ADAPTER = 2;
  private final int CONNECTORS_IN_TEST_ADAPTER = 5;

  @Test
  void when_ActuatorGetAdapterDefinitionInfo_then_RetrieveFullAdapterInfo() throws IOException {
    // arrange
    HttpUriRequest request =
        new HttpGet("http://localhost:" + localServerPort + "/actuator/adapterdefinition");

    // act
    CloseableHttpResponse response = HttpClientBuilder.create().build().execute(request);
    DeclarativeStructureInfo declarativeStructureInfo =
        mapper.readValue(response.getEntity().getContent(), DeclarativeStructureInfo.class);

    // assert
    assertThat(response.getCode()).isEqualTo(200);
    assertThat(declarativeStructureInfo.getScenarios()).hasSize(SCENARIOS_IN_TEST_ADAPTER);
    assertThat(
            declarativeStructureInfo.getConnectorgroups().stream()
                .mapToInt(connectorGroupInfo -> connectorGroupInfo.getConnectors().size())
                .sum())
        .isEqualTo(CONNECTORS_IN_TEST_ADAPTER);

    assertThat(declarativeStructureInfo.getConnectorgroups())
        .hasSize(CONNECTORGROUPS_IN_TEST_ADAPTER);
  }

  @Test
  void when_ActuatorGetScenarioInfo_then_RetrieveScenarios() throws IOException {
    // arrange
    HttpUriRequest request =
        new HttpGet(
            "http://localhost:" + localServerPort + "/actuator/adapterdefinition/scenarios");

    // act
    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
    CollectionType collectionType =
        mapper.getTypeFactory().constructCollectionType(List.class, IntegrationScenarioInfo.class);
    List<IntegrationScenarioInfo> scenarios =
        mapper.readValue(httpResponse.getEntity().getContent(), collectionType);

    // assert
    assertThat(httpResponse.getCode()).isEqualTo(200);
    assertThat(scenarios)
        .anyMatch(
            scenarioInfo ->
                "Scenario used for testing which appends static message"
                    .equals(scenarioInfo.getScenarioDescription()))
        .hasSize(SCENARIOS_IN_TEST_ADAPTER);
  }

  @Test
  void when_ActuatorGetConnectorGroupInfo_then_RetrieveConnectors() throws IOException {
    // arrange
    HttpUriRequest request =
        new HttpGet(
            "http://localhost:" + localServerPort + "/actuator/adapterdefinition/connectorgroups");

    // act
    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
    CollectionType collectionType =
        mapper.getTypeFactory().constructCollectionType(List.class, ConnectorGroupInfo.class);
    List<ConnectorGroupInfo> connectorGroups =
        mapper.readValue(httpResponse.getEntity().getContent(), collectionType);

    // assert
    assertThat(httpResponse.getCode()).isEqualTo(200);
    assertThat(connectorGroups)
        .anyMatch(
            connectorGroupInfo ->
                "Test Documentation for a connector group"
                    .equals(connectorGroupInfo.getConnectorGroupDescription()))
        .anyMatch(
            connectorGroupInfo ->
                "Test Documentation for an implicitly created connectorgroup"
                    .equals(connectorGroupInfo.getConnectorGroupDescription()))
        .hasSize(CONNECTORGROUPS_IN_TEST_ADAPTER);
  }

  @Test
  void when_ActuatorGetConnectorInfo_then_RetrieveEndpoints() throws IOException {
    // arrange
    HttpUriRequest request =
        new HttpGet(
            "http://localhost:" + localServerPort + "/actuator/adapterdefinition/connectors");

    // act
    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
    CollectionType collectionType =
        mapper.getTypeFactory().constructCollectionType(List.class, ConnectorInfo.class);
    List<ConnectorInfo> connectors =
        mapper.readValue(httpResponse.getEntity().getContent(), collectionType);

    // assert
    assertThat(httpResponse.getCode()).isEqualTo(200);
    assertThat(connectors)
        .anyMatch(
            connectorInfo ->
                "Provides messages from direct to AppendStaticMessage scenario"
                    .equals(connectorInfo.getConnectorDescription()))
        .anyMatch(
            connectorInfo ->
                "Generic connector description.".equals(connectorInfo.getConnectorDescription()))
        .hasSize(CONNECTORS_IN_TEST_ADAPTER);
  }
}
