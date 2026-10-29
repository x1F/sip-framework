# Security Notes

[TOC]

## Known Issues in SIP 4.x

SIP 4.x is built on Spring Boot 3.5, which is out of OSS support. SIP 4.5.1 ships the last OSS releases of the Spring projects
it depends on: Spring Boot 3.5.16, Spring Framework 6.2.19 and Spring Security 6.5.11. Vulnerabilities published since then
are fixed only in releases that Spring provides to Enterprise Support customers (Spring Framework 6.2.20) or that are not
available on Maven Central (Spring Security 6.5.12). SIP can therefore neither pin nor upgrade to a fixed version.

According to our dependency scan (OSV and NVD data), known vulnerabilities of other dependencies that have a fix on Maven
Central are resolved in SIP 4.5.1. The following high and critical vulnerabilities remain open. Status: SIP 4.5.1, reviewed on 2026-10-29.

| Component | Version | Open high/critical CVEs | Fix |
|---|---|---|---|
| Spring Framework | 6.2.19 | **Critical:** CVE-2026-47884, CVE-2026-47890, CVE-2026-47891, CVE-2026-47892, CVE-2026-59283, CVE-2026-59313<br>**High:** CVE-2026-47885, CVE-2026-47886, CVE-2026-47888, CVE-2026-47889, CVE-2026-47893, CVE-2026-59282 | 6.2.20 (Enterprise Support only). OSS fixes exist only in Spring Framework 7.x, which requires Spring Boot 4. |
| Spring Security | 6.5.11 | **Critical:** CVE-2026-59270<br>**High:** CVE-2026-47841, CVE-2026-41707 | 6.5.12 (not available on Maven Central) |

### Impact

Most of these vulnerabilities can only be exploited if specific features are used. SIP itself does not use `XsltView`,
server-sent events, SpEL evaluation contexts or WebFlux, and WebFlux and RSocket are not part of the dependencies of SIP.

Please check your adapter if it:

* **Uses SpEL** (CVE-2026-59283, CVE-2026-47886): Do not set `spring.expression.compiler.mode` to `IMMEDIATE` or `MIXED`, it is
  `OFF` by default. Do not evaluate user-supplied SpEL expressions.
* **Uses Spring MVC views or server-sent events** (CVE-2026-47884, CVE-2026-47890, CVE-2026-59313): This includes `XsltView`,
  and server-sent events with view fragments or functional endpoints.
* **Binds user-supplied property paths onto objects** (CVE-2026-59282): This is Spring's data binding.
* **Uses `sip-security` together with the embedded UnboundID LDAP server, WebAuthn or DPoP** (CVE-2026-59270, CVE-2026-47841,
  CVE-2026-41707): These are Spring Security features.

Details of each vulnerability can be found in the Spring advisories at `https://spring.io/security/<cve-id>`.

### Mitigation

If you have Spring Enterprise Support, you can override the standard Spring Boot version properties `spring-framework.version` and
`spring-security.version` in your adapter with the fixed releases.

The planned SIP 5 release, based on Spring Boot 4.1, returns to actively maintained OSS versions of Spring Framework and Spring Security.
