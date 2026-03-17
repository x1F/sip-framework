package one.x1f.sip.foundation.testkit.configurationproperties.models;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

/** Class that holds a single message used in test cases */
@Data
public class MessageProperties {
  private static final String RESOURCE_FILE_PREFIX = "classpath:";
  private PayloadProperties body = new PayloadProperties();
  private Map<String, PayloadProperties> headers = new HashMap<>();

  public PayloadProperties getEvaluatedBody() {
    var payload = new PayloadProperties();
    payload.setValue(getBodyAsString());
    payload.setEval(getEvalAsString());
    return payload;
  }

  @SneakyThrows
  public String getBodyAsString() {
    return getPayloadOrReadFromFile(body.getValue());
  }

  @SneakyThrows
  public String getEvalAsString() {
    return getPayloadOrReadFromFile(body.getEval());
  }

  @SneakyThrows
  private String getPayloadOrReadFromFile(String value) {
    if (isNotBlank(value) && value.startsWith(RESOURCE_FILE_PREFIX)) {
      String bodyLocation = value.substring(RESOURCE_FILE_PREFIX.length());
      return FileUtils.readFileToString(
          new ClassPathResource(bodyLocation).getFile(), StandardCharsets.UTF_8);
    }
    return value;
  }

  public Map<String, PayloadProperties> getHeaders() {
    return headers.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                  var payload = new PayloadProperties();
                  payload.setValue(getPayloadOrReadFromFile(e.getValue().getValue()));
                  payload.setEval(getPayloadOrReadFromFile(e.getValue().getEval()));
                  return payload;
                }));
  }
}
