## 4.3.0 - 2026-01-29

### ⭐ Features
- Replace 'camel.rest.port' and 'camel.rest.host' with 'camel.rest.host-name-resolver' and 'use-x-forward-headers' in adapter's default configuration. [#338](https://github.com/IKOR-GmbH/sip-framework/pull/338) by [Nemikor](https://github.com/Nemikor)
- Use logback-spring.xml instead of logback.xml in SIP Archetype and reflect log level to profile that is set. [#337](https://github.com/IKOR-GmbH/sip-framework/pull/337) by [Nemikor](https://github.com/Nemikor)

### 🐞 Bugfixes
- Use DEBUG level instead of WARN for messages in SIPExchangeHelper. [#339](https://github.com/IKOR-GmbH/sip-framework/pull/339) by [Nemikor](https://github.com/Nemikor)
- Update versions of spring boot to 3.5.10 and apache camel to 4.14.4 [#342](https://github.com/IKOR-GmbH/sip-framework/pull/342) by [Nemikor](https://github.com/Nemikor)

### 📔 Documentation
- Add recommendation to use @DirtiesContext in TestKit batch/build tests. [#341](https://github.com/IKOR-GmbH/sip-framework/pull/341) by [Nemikor](https://github.com/Nemikor)

