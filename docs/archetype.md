# SIP Archetype

[TOC]

## Description

This Maven Archetype creates a basic SIP Adapter project with a defined structure and necessary dependencies.

## How to create a SIP Adapter using SIP Archetype

In order to make setting up a new integration adapter project an easy task, we have created a dedicated SIP (Adapter) Maven archetype, that gives the ability to quickly bootstrap a new integration adapter, along with all needed Maven dependencies and goodies, as well as the recommended, preferred organization of the project.

You can create a SIP Adapter by using the following Maven command:

```shell
  mvn archetype:generate -DarchetypeGroupId=one.x1f.sip.foundation -DarchetypeArtifactId=sip-archetype -DarchetypeVersion=<latest.sip-framework.version> -DgroupId=one.x1f.sip.adapter -DartifactId=demo -DprojectName=DemoAdapter -Dversion=1.0.0-SNAPSHOT
```


When executing the command, pay attention to use the latest archetype version for the `-DarchetypeVersion` parameter.
Check available versions [here](https://search.maven.org/search?q=one.x1f.sip.foundation).

**Overview of the command**
```shell
mvn archetype:generate
-DarchetypeGroupId=one.x1f.sip.foundation
-DarchetypeArtifactId=sip-archetype
-DarchetypeVersion=<latest.sip-archetype.version>
-DgroupId=one.x1f.sip.adapter
-DartifactId=demo
-DprojectName=DemoAdapter
-Dversion=1.0.0-SNAPSHOT
```

The parameters `-DgroupId`, `-DartifactId`, `-DprojectName` and `-Dversion` should be adjusted to better match your project,
as they set the properties for the adapter. It is recommended to follow the Maven naming convention since they are going to be presented in the _pom.xml_ file of the generated adapter.

After executing given Maven command, you will be requested to insert additional parameters about project structure:

- **connectorGroup1**/**connectorGroup2** are representing names of your connector group packages inside the project. It will also be reflected in the generated package structure (for example - all of the sources for connectorGroup1 will be in the _one.x1f.sip.adapter.sufix.connectorGroup1_ package)
- **packageSuffix** is used to create project package name by concatenating its value to the **groupId**. There is a strict validation
  for using only lower case letters and should be a single word. This will reflect the generated package structure for the generated project files. (for example - all of the sources will be in the _one.x1f.sip.adapter.sufix_ java package) 
- **useLombok** is used for including or excluding Lombok dependency in adapter. Lombok is included by default. If you wish to exclude lombok you should set 
  useLombok to anything other than 'y' or 'Y'.
- **useSoap** is used for including or excluding _SIP SOAP Starter_ dependency in adapter. It is useful to include this starter if the adapter is planned to use SOAP since it has pre-built bootstrap elements included. It is false by default.
- **package** (optional) is used to override previous package naming and provide full package name. This can be skipped by leaving value empty.
  It is strongly recommended to follow package naming convention, otherwise your project will be created but it will have
  package naming errors.
- **createDemoAdapter** should be set to 'y' or 'Y' when the inclusion of a demo adapter is desired.
    It will generate example classes using declarative structure, including process and scenario orchestration.
- **useSipCloudFramework** should be set to 'y' or 'Y' to enable the premium version of the SIP framework. 
  When enabled, the adapter will include the necessary dependencies for SIP Cloud Framework. 
  For access or licensing details, please contact [sip@x1f.one](mailto:sip@x1f.one)


_Note_: All the parameters can be provided via command line if needed:

```shell
mvn archetype:generate -DarchetypeGroupId=one.x1f.sip.foundation -DarchetypeArtifactId=sip-archetype -DarchetypeVersion=<latest.sip-framework.version> -DgroupId=one.x1f.sip.adapter -DartifactId=demo -DprojectName=DemoAdapter -Dversion=1.0.0-SNAPSHOT -DconnectorGroup1=group1 -DconnectorGroup2=group2 -DpackageSuffix=project -DuseLombok=y -DuseSoap=n -Dpackage=one.x1f.sip.adapter.demo.project -DcreateDemoAdapter=n -DuseSipCloudFramework=n
```

After a successful build, a project with the following structure will be created:

- SIPApplication.java
- config
- connectorgroups
  - {connectorGroup1}
    - config
    - models
    - processors
    - connectors
    - transformers
    - validators
  - {connectorGroup2}
    - config
    - models
    - processors
    - connectors
    - transformers
    - validators
- scenarios
    - models
    - definitions
    - processes
    

More about packages and internal SIP structure can be found [here](./README.md).

More information about Maven archetypes is available here:
[Maven Archetype](https://maven.apache.org/guides/introduction/introduction-to-archetypes.html)
