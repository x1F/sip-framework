# SIP Starter Parent

[TOC]

## Description

This project takes care of versions (transitive dependencies) for SIP, Spring Boot and Camel dependencies.

It inherits spring-boot-parent-starter, so that it will, by default, bring everything needed for comfortable development of Spring Boot applications.
This includes transitive dependency management of all dependencies used by the Spring Boot starters, but also other dependencies for SIP Adapters,
plus additional Maven plugin management and configuration.

## Usage

This project should be used as parent for SIP Adapters.

```xml
<parent>
    <groupId>one.x1f.sip.foundation</groupId>
    <artifactId>sip-starter-parent</artifactId>
    <version>${sip.starter.parent}</version>
</parent>
```
