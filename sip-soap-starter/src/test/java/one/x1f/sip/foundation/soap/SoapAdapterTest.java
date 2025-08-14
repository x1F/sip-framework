package one.x1f.sip.foundation.soap;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.customerservice.GetCustomersByNameResponse;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@CamelSpringBootTest
@SpringBootTest(
    classes = {SOAPAdapter.class},
    webEnvironment = WebEnvironment.RANDOM_PORT)
class SoapAdapterTest {

  @Autowired private FluentProducerTemplate template;

  @Test
  void when_SendingGetCustomerByNameMessage_then_AdapterReturnsResponse() {

    Exchange exchange =
        template.withBody("My customer").to(direct("triggerAdapter-getCustomerByName")).send();

    GetCustomersByNameResponse response =
        exchange.getMessage().getBody(GetCustomersByNameResponse.class);
    assertThat(response.getReturn().get(0)).hasFieldOrPropertyWithValue("name", "Max Mustermann");
  }
}
