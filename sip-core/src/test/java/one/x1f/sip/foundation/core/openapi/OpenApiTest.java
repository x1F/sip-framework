package one.x1f.sip.foundation.core.openapi;

import static one.x1f.sip.foundation.core.CoreTestApplication.REST_ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.swagger.v3.oas.models.OpenAPI;
import one.x1f.sip.foundation.core.CoreTestApplication;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = CoreTestApplication.class,
    properties = {"camel.servlet.mapping.context-path=/adapter/*"})
@AutoConfigureMockMvc
@DirtiesContext
class OpenApiTest {

  @Autowired OpenAPI camelRestDSLOpenApi;
  @Autowired CamelContext camelContext;
  @Autowired ProducerTemplate producerTemplate;
  @Autowired private MockMvc mvcBean;

  @Test
  void When_resolveCamelContextPathInOpenApi_Expect_ContextPathAdded() {
    // arrange
    String contextPath = camelContext.getRestConfiguration().getContextPath();

    // act
    Exchange target = producerTemplate.send("rest-api://api-doc", Exchange::getMessage);
    String body = target.getMessage().getBody(String.class);

    // assert
    assertThat(body).contains(REST_ENDPOINT).doesNotContain(contextPath + REST_ENDPOINT);
    assertThat(camelRestDSLOpenApi.getPaths()).containsKey(contextPath + REST_ENDPOINT);
  }

  @Test
  void When_fetchingSwaggerUI_THEN_UI_is_shown() throws Exception {
    // act & assert
    mvcBean
        .perform(get("/swagger-ui/index.html"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("<title>Swagger UI</title>")));
  }
}
