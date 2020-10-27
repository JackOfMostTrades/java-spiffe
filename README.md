# Java SPIFFE Library

<a href='https://travis-ci.org/spiffe/java-spiffe.svg?branch=master'><img src='https://travis-ci.org/spiffe/java-spiffe.svg?branch=master'></a>
[![Coverage Status](https://coveralls.io/repos/github/spiffe/java-spiffe/badge.svg)](https://coveralls.io/github/spiffe/java-spiffe?branch=master)

## Overview

The JAVA-SPIFFE library provides functionality to interact with the Workload API to fetch X.509 and JWT SVIDs and Bundles, 
and a Java Security Provider implementation to be plugged into the Java Security architecture. This is essentially 
an X.509-SVID based KeyStore and TrustStore implementation that handles the certificates in memory and receives the updates 
asynchronously from the Workload API. The KeyStore handles the Certificate chain and Private Key to prove identity 
in a TLS connection, and the TrustStore handles the trusted bundles (supporting federated bundles) and performs 
peer's certificate and SPIFFE ID verification. 

This library contains three modules:

* [java-spiffe-core](java-spiffe-core/README.md): Core functionality to interact with the Workload API, and to process and validate 
X.509 and JWT SVIDs and bundles.

* [java-spiffe-provider](java-spiffe-provider/README.md): Java Provider implementation.

* [java-spiffe-helper](java-spiffe-helper/README.md): Helper to store X.509 SVIDs and Bundles in Java Keystores in disk.

**Supports Java 8+**

Download
--------

The JARs can be downloaded from [Maven Central](https://search.maven.org/search?q=g:io.spiffe%20AND%20v:0.6.3). 

The dependencies can be added to `pom.xml`

To import the `java-spiffe-provider` component: 
```xml
<dependency>
  <groupId>io.spiffe</groupId>
  <artifactId>java-spiffe-provider</artifactId>
  <version>0.6.3</version>
</dependency>
```
The `java-spiffe-provider` component imports the `java-spiffe-core` component.

To just import the `java-spiffe-core` component:
```xml
<dependency>
  <groupId>io.spiffe</groupId>
  <artifactId>java-spiffe-core</artifactId>
  <version>0.6.3</version>
</dependency>
```

Using Gradle:

Import `java-spiffe-provider`:
```gradle
implementation group: 'io.spiffe', name: 'java-spiffe-provider', version: '0.6.3'
```

Import `java-spiffe-core`:
```gradle
implementation group: 'io.spiffe', name: 'java-spiffe-core', version: '0.6.3'
```

### MacOS Support

Add to your `pom.xml`:
```xml
<dependency>
  <groupId>io.spiffe</groupId>
  <artifactId>grpc-netty-macos</artifactId>
  <version>0.6.3</version>
  <scope>runtime</scope>
</dependency>
```

Using Gradle:
```gradle
runtimeOnly group: 'io.spiffe', name: 'grpc-netty-macos', version: '0.6.3'
```

### Note: `java-spiffe-helper` artifact

As the [java-spiffe-helper](java-spiffe-helper/README.md) artifact is meant to be used as a standalone JAR and not as a Maven dependency, 
it is not published to Maven Central, but to [Github releases](https://github.com/spiffe/java-spiffe/releases/tag/v0.6.3), for both 
[Linux](https://github.com/spiffe/java-spiffe/releases/download/v0.6.3/java-spiffe-helper-0.6.3-linux-x86_64.jar) and 
[MacOS](https://github.com/spiffe/java-spiffe/releases/download/v0.6.3/java-spiffe-helper-0.6.3-osx-x86_64.jar) versions.

### Build the JARs

On Linux or MacOS, run:

```
 $ ./gradlew assemble
 BUILD SUCCESSFUL 
```

All `jar` files are placed in `build/libs` folder.  

#### Jars that include all dependencies 

For the module [java-spiffe-provider](java-spiffe-provider), a fat jar is generated with the classifier `-all-[os-classifier]`.

Fhe module [java-spiffe-helper](java-spiffe-helper), a fat jar is generated with the classifier `[os-classifier]`

Based on the OS where the build is run, the `[os-classifier]` will be:

* `-linux-x86_64` for Linux
* `-osx-x86_64` for MacOS
