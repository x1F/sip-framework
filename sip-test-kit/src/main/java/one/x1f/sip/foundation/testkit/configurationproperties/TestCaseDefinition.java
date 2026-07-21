package one.x1f.sip.foundation.testkit.configurationproperties;

import com.fasterxml.jackson.annotation.*;
import java.util.*;
import lombok.Data;
import one.x1f.sip.foundation.testkit.configurationproperties.models.EndpointProperties;

/** Definition of a single test case. */
@Data
public class TestCaseDefinition {
  @JsonProperty("schema_version")
  private double schemaVersion;

  private String title = UUID.randomUUID().toString();

  @JsonProperty("when-execute")
  private EndpointProperties whenExecute;

  @JsonProperty("with-mocks")
  private List<EndpointProperties> withMocks = new ArrayList<>();

  @JsonProperty("then-expect")
  private List<EndpointProperties> thenExpect = new ArrayList<>();

  private Map<String, String> variables = new HashMap<>();

  @JsonProperty("real-endpoints")
  private List<String> realEndpoints = new ArrayList<>();
}
