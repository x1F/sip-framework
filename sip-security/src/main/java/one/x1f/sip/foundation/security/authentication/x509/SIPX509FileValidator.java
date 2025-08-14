package one.x1f.sip.foundation.security.authentication.x509;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.security.authentication.ConditionalOnSIPAuthProvider;
import one.x1f.sip.foundation.security.authentication.common.validators.SIPTokenValidator;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

/**
 * The file-based x509 validator reads an acl file in this format:
 *
 * <pre>
 *   # this is a comment
 *   CN=Full Name, EMAILADDRESS=name@domain.de, O=[*], C=DE
 *   CN=Full Name2, EMAILADDRESS=name2@domain.de, O=[*], C=DE
 * </pre>
 *
 * And then validates the extracted certificate data with the contents in the file.
 *
 * @author thomas.stieglmaier
 */
@Primary
@ConditionalOnSIPAuthProvider(
    listItemValue = SIPX509AuthenticationProvider.class,
    validationClass = SIPX509FileValidator.class)
@Component
public class SIPX509FileValidator implements SIPTokenValidator<SIPX509AuthenticationToken> {

  private static final String WILDCARD_VALUE = "[*]";
  private static final String DN_DELIMITER = ",";
  private static final String COMMENT_START_INDICATOR = "#";
  private static final String KEY_VALUE_SEPARATOR = "=";

  private final List<Map<String, String>> validUsers = new ArrayList<>();

  /**
   * Creates a file-based x509 auth token validator
   *
   * @param config the config containing information on where to find the file that should be read
   */
  public SIPX509FileValidator(SecurityConfigProperties config) {
    try (Reader reader =
            new InputStreamReader(
                config
                    .getSettingsForProvider(SIPX509AuthenticationProvider.class)
                    .getValidation()
                    .getFilePath()
                    .getInputStream());
        BufferedReader bufferedReader = new BufferedReader(reader)) {

      bufferedReader
          .lines()
          .forEach(
              l -> {
                Map<String, String> userMap = parseEntityLine(l);
                if (!userMap.isEmpty()) {
                  validUsers.add(userMap);
                }
              });

    } catch (Exception e) {
      throw new SIPFrameworkException("X509 Acl file could not be parsed properly", e);
    }
  }

  private static Map<String, String> parseEntityLine(String line) {
    String lineWithoutComment =
        line.contains(COMMENT_START_INDICATOR)
            ? line.substring(0, line.indexOf(COMMENT_START_INDICATOR)).trim()
            : line;
    return distinguishedNameToMap(lineWithoutComment);
  }

  @Override
  public boolean isValid(SIPX509AuthenticationToken token) {
    Map<String, String> toBeAuthedUser = distinguishedNameToMap(token.getPrincipal().toString());

    // check that the to be authorized user has all necessary fields/values for at least one user
    // in the allowed users list
    return validUsers.stream().anyMatch(u -> toBeAuthedUser.entrySet().containsAll(u.entrySet()));
  }

  private static Map<String, String> distinguishedNameToMap(String dn) {
    if (StringUtils.isBlank(dn)) {
      return new HashMap<>();
    }

    try {
      return Stream.of(dn.split(DN_DELIMITER))
          .map(p -> p.split(KEY_VALUE_SEPARATOR))
          // filter out wildcard values, this is safe to do for expected and incoming users
          // for expected it is safe because we want to allow any value (and therefore don't need to
          // check it further)
          // for incoming it is safe because if they had a wildcard value there, and it is different
          // than expected the match wouldn't work anyway
          .filter(pkv -> !pkv[1].trim().equals(WILDCARD_VALUE))
          .collect(Collectors.toMap(pkv -> pkv[0].trim(), pkv -> pkv[1].trim()));
    } catch (Exception e) {
      throw new BadCredentialsException(
          "Distinguished name of certificate was not in a valid form", e);
    }
  }
}
