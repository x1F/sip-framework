# Spring Boot Configuration Properties

> Auto-generated from `spring-configuration-metadata.json`. Last updated: 2026-03-09 16:23.

---

## Table of Contents

- [sip-core](#sip-core)
- [sip-security](#sip-security)
- [sip-test-kit](#sip-test-kit)

## sip-core

### `actuator.adapter-routes`

*Defined in `one.x1f.sip.foundation.core.configuration.properties.ActuatorConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `actuator.adapter-routes.enabled` | Boolean | `true` | Enable controlling lifecycle of routes |

### `actuator.adapter-routes.scheduler`

*Defined in `one.x1f.sip.foundation.core.configuration.properties.ActuatorConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `actuator.adapter-routes.scheduler.fixed-delay` | String |  | Sets health check execution interval |
| `actuator.adapter-routes.scheduler.initial-delay` | String |  | Sets health check initial delay |

### `management.endpoint.adapterroutes`

*Defined in `one.x1f.sip.foundation.core.actuator.routes.AdapterRouteEndpoint`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `management.endpoint.adapterroutes.access` | Access | `unrestricted` | Permitted level of access for the adapterroutes endpoint. |
| `management.endpoint.adapterroutes.cache.time-to-live` | Duration | `0ms` | Maximum time that a response can be cached. |
| `management.endpoint.adapterroutes.enabled` | Boolean | `true` | ⚠️ **Deprecated** Use `management.endpoint.adapterroutes.access` instead.<br>Whether to enable the adapterroutes endpoint. |

### `sip.core.actuator.extensions.health`

*Defined in `one.x1f.sip.foundation.core.configuration.properties.ActuatorExtensionHealthConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.core.actuator.extensions.health.enabled` | Boolean | `true` | Enable additional SIP Health check |

### `sip.core.actuator.extensions.info`

*Defined in `one.x1f.sip.foundation.core.configuration.properties.ActuatorExtensionInfoConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.core.actuator.extensions.info.enabled` | Boolean | `true` | Expose additional adapter information through /actuator/info |

### `sip.core.declarativestructure`

*Defined in `one.x1f.sip.foundation.core.configuration.properties.DeclarativeStructureConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.core.declarativestructure.enabled` | Boolean | `true` | Enable declarative structure |

### `sip.core.metrics.external-endpoint-health-check`

*Defined in `one.x1f.sip.foundation.core.configuration.properties.HealthCheckConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.core.metrics.external-endpoint-health-check.enabled` | Boolean | `true` | Enable external endpoint health check |

### `sip.core.metrics.gauge`

*Defined in `one.x1f.sip.foundation.core.actuator.health.scheduler.HealthGaugeConfiguration`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.core.metrics.gauge.name` | String | `sip.core.metrics.health` | Name of the configurable health gauge |

### `sip.core.proxy`

*Defined in `one.x1f.sip.foundation.core.configuration.properties.ProcessorProxyConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.core.proxy.enabled` | Boolean | `true` | Enable Processor Proxy |

### `sip.core.tracing`

*Defined in `one.x1f.sip.foundation.core.trace.SIPTraceConfig`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.core.tracing.enabled` | Boolean | `false` | Enable SIP tracing and trace history |
| `sip.core.tracing.log` | Boolean | `false` | Enable logging tracing in console |

### `sip.core.tracing.exchange-formatter`

*Defined in `one.x1f.sip.foundation.core.trace.SIPExchangeFormatter`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.core.tracing.exchange-formatter.max-chars` | Integer |  |  |
| `sip.core.tracing.exchange-formatter.multiline` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.plain` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-all` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-all-properties` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-body` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-body-type` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-cached-streams` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-caught-exception` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-exception` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-exchange-id` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-exchange-pattern` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-files` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-future` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-headers` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-properties` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-route-group` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-route-id` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-stack-trace` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-streams` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.show-variables` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.skip-body-line-separator` | Boolean |  |  |
| `sip.core.tracing.exchange-formatter.style` | DefaultExchangeFormatter$OutputStyle |  |  |

### `sip.core.translation`

*Defined in `one.x1f.sip.foundation.core.translate.TranslateConfiguration`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.core.translation.default-encoding` | String | `UTF-8` | Sets default encoding |
| `sip.core.translation.enabled` | Boolean | `true` | Enable SIP translation |
| `sip.core.translation.fallback-to-system-locale` | Boolean | `false` | Use system language if none defined |
| `sip.core.translation.file-locations` | String> |  | Sets locations of translation bundles |
| `sip.core.translation.lang` | String | `en` | Set language of log messages |
| `sip.core.translation.sip-file-locations` | String> |  |  |
| `sip.core.translation.use-code-as-default-message` | Boolean | `true` | If key is not assigned use it in message |

---

## sip-security

### `sip.security.authentication`

*Defined in `one.x1f.sip.foundation.security.config.SecurityConfigProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.security.authentication.auth-providers` | SecurityConfigProperties$AuthProviderSettings> |  | The authentication providers that should be used for auth checks |
| `sip.security.authentication.disable-csrf` | Boolean | `true` | indicates if csrf should be disabled. Default is true, because usually sip adapters are server-to-server connections and not browser-based |
| `sip.security.authentication.ignored-endpoints` | String> |  | endpoints that should be left out of the complete authentication checks |
| `sip.security.authentication.matcher-patterns` | String[] | `['/**']` |  |
| `sip.security.authentication.order` | Integer | `1` |  |

### `sip.security.ssl`

*Defined in `one.x1f.sip.foundation.security.config.SslSecurityConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.security.ssl.enabled` | Boolean | `false` | Enable SIP SSL security |

### `sip.security.ssl.client`

*Defined in `one.x1f.sip.foundation.security.config.SslSecurityConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.security.ssl.client.enabled` | Boolean | `false` | Enable separate client certification |
| `sip.security.ssl.client.key-alias` | String |  | The alias (or name) under which the key is stored in the client keystore |
| `sip.security.ssl.client.key-password` | String |  | Password of the client key |
| `sip.security.ssl.client.key-store` | String |  | Location of client keystore |
| `sip.security.ssl.client.key-store-password` | String |  | Password of client keystore |
| `sip.security.ssl.client.key-store-type` | String |  | Type of client keystore file |

### `sip.security.ssl.server`

*Defined in `one.x1f.sip.foundation.security.config.SslSecurityConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.security.ssl.server.client-auth` | Boolean | `false` | Enable authentication type - Possible values: NONE, WANT or NEED |
| `sip.security.ssl.server.key-alias` | String |  | The alias (or name) under which the key is stored in the keystore |
| `sip.security.ssl.server.key-password` | String |  | Password of the key |
| `sip.security.ssl.server.key-store` | String |  | Location of keystore |
| `sip.security.ssl.server.key-store-password` | String |  | Password of keystore |
| `sip.security.ssl.server.key-store-type` | String |  | Type of keystore file |

---

## sip-test-kit

### `sip.adapter`

*Defined in `one.x1f.sip.foundation.testkit.config.AdapterConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.adapter.camel-cxf-endpoint-context-path` | String |  |  |
| `sip.adapter.camel-endpoint-context-path` | String |  |  |

### `sip.testkit`

*Defined in `one.x1f.sip.foundation.testkit.config.TestKitConfigurationProperties`*

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sip.testkit.batch-test` | Boolean | `false` | Enable batch tests in Test Kit |
| `sip.testkit.enabled` | Boolean | `false` | Enable Test Kit |
| `sip.testkit.test-cases-path` | String |  | Define path for file with test cases |

### `test-case-definitions`

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `test-case-definitions` | TestCaseDefinition> |  |  |

