package one.x1f.sip.foundation.security.authentication.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.security.authentication.ConditionalOnSIPAuthProvider;
import one.x1f.sip.foundation.security.authentication.common.validators.SIPTokenValidator;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * The file-based basic auth validator reads a given json file in this format:
 *
 * <pre>
 * [
 *   {"username": "user1", "password": "pw1"},
 *   {"username": "anotherUser", "password": "anotherPassword"}
 * ]
 * </pre>
 *
 * @author thomas.stieglmaier
 */
@Primary
@ConditionalOnSIPAuthProvider(
    listItemValue = SIPBasicAuthAuthenticationProvider.class,
    validationClass = SIPBasicAuthFileValidator.class)
@Component
public class SIPBasicAuthFileValidator
    implements SIPTokenValidator<SIPBasicAuthAuthenticationToken> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Map<String, String> userPwMap = new HashMap<>();

  /**
   * Autowired Constructor, which just needs the security config properties as bean
   *
   * @param config the configs to be read
   * @throws IOException if the configured basic auth settings file is not valid json
   */
  @Autowired
  public SIPBasicAuthFileValidator(SecurityConfigProperties config) throws IOException {
    try {
      File file =
          config
              .getSettingsForProvider(SIPBasicAuthAuthenticationProvider.class)
              .getValidation()
              .getFilePath()
              .getFile();
      OBJECT_MAPPER
          .readTree(file)
          .elements()
          .forEachRemaining(
              e -> userPwMap.put(e.get("username").textValue(), e.get("password").textValue()));
    } catch (Exception e) {
      throw new SIPFrameworkException(
          "Basic authentication users file could not be parsed properly", e);
    }
  }

  @Override
  public boolean isValid(SIPBasicAuthAuthenticationToken token) {
    return userPwMap.containsKey(token.getPrincipal())
        && userPwMap.get(token.getPrincipal()).equals(token.getCredentials());
  }
}
