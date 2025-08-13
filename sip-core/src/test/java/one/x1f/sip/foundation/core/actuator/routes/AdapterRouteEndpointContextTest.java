package one.x1f.sip.foundation.core.actuator.routes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import one.x1f.sip.foundation.core.CoreTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = CoreTestApplication.class)
@AutoConfigureMockMvc
@DirtiesContext
class AdapterRouteEndpointContextTest {

  private static final String NON_EXISTENT_ROUTE_ID = "falseRouteId";

  @Autowired private MockMvc mvcBean;

  @Test
  void When_callingAdapterRoutesEndpoint_Then_httpSuccessReceived() throws Exception {
    mvcBean.perform(get("/actuator/adapterroutes")).andExpect(status().is2xxSuccessful());
  }

  @Test
  void When_callingAdapterRouteEndpoint_With_ValidRoute_Then_httpSuccessReceived()
      throws Exception {
    mvcBean
        .perform(get("/actuator/adapterroutes/" + CoreTestApplication.TEST_ROUTE_ID))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  void When_callingAdapterRouteEndpoint_With_InvalidRoute_Then_httpNotFoundReceived()
      throws Exception {
    mvcBean
        .perform(get("/actuator/adapterroutes/" + NON_EXISTENT_ROUTE_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  void When_callingAdapterRouteResetEndpoint_With_InvalidRoute_Then_httpNotFoundReceived()
      throws Exception {
    mvcBean
        .perform(post("/actuator/adapterroutes/" + NON_EXISTENT_ROUTE_ID + "/reset"))
        .andExpect(status().isNotFound());
  }

  @Test
  void When_callingFilteredDetails_With_ValidRoute_Then_httpSuccessReceived() throws Exception {
    mvcBean
        .perform(get("/actuator/adapterroutes?ids=" + CoreTestApplication.TEST_ROUTE_ID))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  void When_callingFilteredDetails_With_InvalidRoute_Then_httpSuccessReceived() throws Exception {
    mvcBean
        .perform(get("/actuator/adapterroutes?ids=" + NON_EXISTENT_ROUTE_ID))
        .andExpect(status().isNotFound());
  }
}
