package one.x1f.sip.foundation.core.declarative;

import static org.assertj.core.api.Assertions.assertThat;

import one.x1f.sip.foundation.core.apps.declarative.MappingAdapter;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.FrontEndTypes;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(
    classes = {MappingAdapter.class},
    properties = {"camel.rest.binding-mode=auto", "camel.openapi.enabled=false"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisableJmx(false)
@MockEndpoints("log:message*")
@DirtiesContext
class ModelMappingTest {

  @EndpointInject("mock:log:message")
  private MockEndpoint mockedLogger;

  @Autowired private TestRestTemplate restTemplate;

  @LocalServerPort private int localServerPort;

  @BeforeEach
  void setup() {
    mockedLogger.reset();
  }

  @Test
  void When_UsingPOSTScenario_With_Mappers_Then_RequestIsLoggedAndValidResponseReturned()
      throws InterruptedException {

    mockedLogger.expectedBodiesReceivedInAnyOrder("{\"id\":111,\"resourceTypeName\":\"USER\"}");

    // act
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    FrontEndTypes.UserResponse response =
        restTemplate.postForObject(
            "/adapter/user",
            new HttpEntity<>(FrontEndTypes.UserRequest.builder().userId(111).build(), headers),
            FrontEndTypes.UserResponse.class);

    // assert
    assertThat(response.getUserId()).isEqualTo(111);
    assertThat(response.getUsername()).isEqualTo("TEST");
    mockedLogger.assertIsSatisfied();
  }
}
