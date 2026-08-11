package one.x1f.sip.foundation.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("sip.security.ssl")
public class SslSecurityConfigurationProperties {
  /** Enable SIP SSL security */
  private boolean enabled;

  private SslClientSecurityConfigurationProperties client;
  private SslServerSecurityConfigurationProperties server;

  @Data
  public static class SslClientSecurityConfigurationProperties {
    /** Enable separate client certification */
    private boolean enabled;

    /** Location of client keystore */
    private String keyStore;

    /** Password of client keystore */
    private String keyStorePassword;

    /** Type of client keystore file */
    private String keyStoreType;

    /** The alias (or name) under which the key is stored in the client keystore */
    private String keyAlias;

    /** Password of the client key */
    private String keyPassword;
  }

  @Data
  public static class SslServerSecurityConfigurationProperties {
    /** Enable authentication type - Possible values: NONE, WANT or NEED */
    private boolean clientAuth;

    /** Location of keystore */
    private String keyStore;

    /** Password of keystore */
    private String keyStorePassword;

    /** Type of keystore file */
    private String keyStoreType;

    /** The alias (or name) under which the key is stored in the keystore */
    private String keyAlias;

    /** Password of the key */
    private String keyPassword;
  }
}
