package one.x1f.sip.foundation.core.actuator.health.camel;

import static one.x1f.sip.foundation.core.CoreTestApplication.TEST_ROUTE_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import one.x1f.sip.foundation.core.CoreTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(classes = CoreTestApplication.class)
@AutoConfigureMockMvc
@DirtiesContext
class CamelHealthCheckRepositoryOverrideTest {

  @Autowired private MockMvc mvcBean;

  @Test
  void When_callingHealthCheckEndpoint_Verify_httpSuccessReceived() throws Exception {
    mvcBean.perform(get("/actuator/health")).andExpect(status().is2xxSuccessful());
  }

  @Test
  void When_suspendingRoute_Verify_statusDown() throws Exception {
    // arrange
    mvcBean
        .perform(post("/actuator/adapterroutes/" + TEST_ROUTE_ID + "/suspend"))
        .andExpect(status().is2xxSuccessful());

    // act
    ResultActions healthEndpointResult = mvcBean.perform(get("/actuator/health"));

    // assert
    healthEndpointResult
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.components.camelHealth.details.consumer:" + TEST_ROUTE_ID).value("DOWN"))
        .andExpect(
            jsonPath("$.components.camelHealth.details.route:" + TEST_ROUTE_ID).value("DOWN"));

    // cleanup
    mvcBean
        .perform(post("/actuator/adapterroutes/" + TEST_ROUTE_ID + "/resume"))
        .andExpect(status().is2xxSuccessful());
  }
}
