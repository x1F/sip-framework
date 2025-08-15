# SIP Framework version upgrade

[TOC]

## Upgrade from 3.6.2 to 4.0.0

4.0.0 is a new major release that focuses mainly on SIP Cloud and SIP Cockpit related features.

Due to the merger of IKOR with x1F, all artifacts and packages have been moved to the `one.x1f.sip` namespace. 
This requires changes in the class imports as well as the Maven dependency structure (see below). Apart from those changes,
no substantial changes in the sources of existing adapters are required to make them work with the new framework release.

### Update Maven dependencies to x1f namespace

All listed dependencies in existing `pom.xml` files, as well as the parent declaration, must have their SIP related `<groupId>`switched
from `de.ikor.sip.foundation` to `one.x1f.sip.foundation`.

### Update imported framework classes to x1f namespace

All Java files importing SIP framework packages must switch those import statements from `de.ikor.sip.foundation` to `one.x1f.sip.foundation`.
We recommend using search / replace in your IDE if you have a lot of `.java` files importing SIP framework classes.

### Updated annotation class imports due to typo

In older SIP framework releases, some annotations resided in the mistyped `de.ikor.sip.foundation.core.declarative.annonation` package. 
We used the opportunity of the global namespace migration to move these annotations to `one.x1f.sip.foundation.core.declarative.annotation`, so
imports need to be changed where necessary.

### Attribute name change in ConnectorProcessor ordering annotations

The attribute in `@ExecuteBefore` and `@ExecuteAfter` annotations used for relative connector-processor ordering has been changed
from `processorName` to `extensionName`. This has been done as those annotations are also available for use with the broader connector-extensions API introduced in this release.
 
## Upgrade from 3.6.1 to 3.6.2

Building REST inbound connectors was changed. 
Usage of multiple HTTP methods in REST DSL of a single connector is no longer allowed.
REST Connectors which contain this should be split into their own connector implementations.
This change will not affect how the adapter behaves.

**Note:** Unique connector ID should be assigned to each new connector, 
since the automatically generated one may be duplicated.

## Upgrade from 3.5.0 to 3.6.0

This version contains upgrades to major libraries used by the framework.
There is a possibility that they may lead to some changes.

Please use the following Apache Camel guide if necessary: https://camel.apache.org/manual/camel-4x-upgrade-guide.html

## Upgrade from 3.4.0 to 3.5.0

This is a quality of life and bugfix update, it shouldn't introduce any breaking issues.

## Upgrade from 3.3.0 to 3.4.0

Version 3.4.0 allows fine-grained configuration and exception handling for connectors. 
If an adapter contains global configuration which was added directly with **RouteConfigurationBuilder**, 
it will no longer apply to connectors.
It should be refactored and provided via **ConfigurationDefinition**.

**Note:** When it comes to process orchestration global configuration will still be applicable.

**Deprecation Notice**

The `defineTransformationOrchestrator` method from **ConnectorBase** class is marked as deprecated as of version 3.4.0. 
This method may be removed in future versions.
It is recommended to migrate to new **Connector Processors** in all connectors.

## Upgrade from 3.2.0 to 3.3.0

This is mostly a dependencies update and bugfix version, so it's shouldn't introduce any braking changes. 
However, in the previous version, Integration Scenario model validation was triggered conditionally
depending on the presence of Connector's response model (instead of Integration Scenario's).
This has been changed, and the response transformation in the connector is now triggered unconditionally as well.

A runtime **validation** error will occur if an Adapter relied on that condition and **didn't conform** to Scenario's **domain model**.

## Upgrade from 3.1.0 to 3.2.0
Version 3.2.0 introduces changes in dependencies - most notably Spring 6, Apache Camel 4 and CXF 4. 
There should be no breaking changes in this release. If the adapter developers relied on deprecated features from underlying frameworks then some changes are necessary but those should be fixed on case-by-case basis.

Upgrade guide for specific use-cases:
* Adapters using **SOAP** Connectors

From adapter pom.xml file remove this plugin:
```xml
<plugin>
  <groupId>de.codecentric</groupId>
  <artifactId>cxf-spring-boot-starter-maven-plugin</artifactId>
...
</plugin>
```
and change it to:
```xml
<plugin>
  <groupId>org.apache.cxf</groupId>
  <artifactId>cxf-codegen-plugin</artifactId>
</plugin>
```
Plugin is now managed in the parent pom so no further configuration is necessary. It is a standard CXF plugin and adapter developers can customize its behaviour if needed.


* Adapters redefining **Spring Security**:
If the adapter has redefined Spring Security configuration, exclusion of SIP Security was necessary:
For example: 
```java
@SIPIntegrationAdapter(exclude = SIPSecurityAutoConfiguration.class)
```
That should no longer be necessary, SIP has upgraded to Spring Security 6 and should work with specific behaviour redefined in the adapter.


## Upgrade from 1.0.0 to 2.0.0

The following text represents the complete guide how to update SIP framework version in your adapter from version 
`1.0.0` to `2.0.0`.

In case adapter is generated with archetype of `2.0.0` framework version, following steps are not necessary. To insure
your adapter uses all functionalities, the following changes have to be done manually. Please follow the guide
step by step.

Single lines necessary for updating are marked with `update` comment.

First step is to update the parent field `version` in project root `pom.xml` file:

```xml
<parent>
    <groupId>de.ikor.sip.foundation</groupId>
    <artifactId>sip-framework</artifactId>
    <version>2.0.0</version>    <!-- update -->
    <relativePath/>
</parent>
```

In the same file, profile fields `id` and `maxJdkVersion` should be updated to java version 11.

```xml
<profiles>
    <profile>
        <id>enforce-java-11</id>                <!-- update -->
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-enforcer-plugin</artifactId>
                    <version>3.0.0-M3</version>
                    <dependencies>
                        <dependency>
                            <groupId>org.codehaus.mojo</groupId>
                            <artifactId>extra-enforcer-rules</artifactId>
                            <version>1.3</version>
                        </dependency>
                    </dependencies>
                    <executions>
                        <execution>
                            <id>enforce-bytecode-version</id>
                            <goals>
                                <goal>enforce</goal>
                            </goals>
                            <configuration>
                                <rules>
                                    <enforceBytecodeVersion>
                                        <maxJdkVersion>11</maxJdkVersion>   <!-- update -->
                                    </enforceBytecodeVersion>
                                </rules>
                                <fail>true</fail>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

Next step is to update `pom.xml` file in adapter's application module. Change the parent field `version`:

```xml
<parent>
    <groupId>de.ikor.sip.foundation</groupId>
    <artifactId>sip-starter-parent</artifactId>
    <version>2.0.0</version>    <!-- update -->
    <relativePath/>
</parent>
```

In the same file, `spring-boot-maven-plugin` must be modified. Add goal `build-info` and it's additional
configuration. Simply copy the lines marked with comment `update` and add them to your code in a proper place:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <fork>false</fork>
        <skip>false</skip>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>
                <goal>build-info</goal>     <!-- update -->
            </goals>
            <configuration>                         <!-- update -->
                <additionalProperties>                  <!-- update -->
                    <sipFrameworkVersion>2.0.0</sipFrameworkVersion>    <!-- update -->
                </additionalProperties>                 <!-- update -->
            </configuration>                        <!-- update -->
        </execution>
    </executions>
</plugin>
```

Newer version requires moving your markdown files in adapter's application module under `main/resources/documents` directory.
Furthermore, it is strictly required to create `documents` folder and to keep markdown files inside, so the
[markdown files API](./core.md#sip-details-in-actuator-info-endpoint) can work properly.

If you intend to use SIP Test Kit, you should add `application.yml` in your application module `src/test/resources`
directory with test profile configured:

```yaml
spring:
  profiles:
    active: test
```

Additionally `logback-test.xml` should be added in the same test directory. You can find the content of this file 
[here](../sip-archetype/src/main/resources/archetype-resources/__rootArtifactId__-application/src/test/resources/logback-test.xml).