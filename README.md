Ballerina MIME Library
===================

  [![Build](https://github.com/ballerina-platform/module-ballerina-mime/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-mime/actions/workflows/build-timestamped-master.yml)
  [![codecov](https://codecov.io/gh/ballerina-platform/module-ballerina-mime/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerina-mime)
  [![Trivy](https://github.com/ballerina-platform/module-ballerina-mime/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-mime/actions/workflows/trivy-scan.yml)
  [![GraalVM Check](https://github.com/ballerina-platform/module-ballerina-mime/actions/workflows/build-with-bal-test-graalvm.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-mime/actions/workflows/build-with-bal-test-graalvm.yml)
  [![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerina-mime.svg)](https://github.com/ballerina-platform/module-ballerina-mime/commits/master)

This library provides a set of APIs to work with messages, which follow the Multipurpose Internet Mail Extensions
(MIME) specification as specified in the [RFC 2045 standard](https://www.ietf.org/rfc/rfc2045.txt).

```
Entity refers to the header fields and the content of a message or a part of the body in a multipart entity. 
```

### Supported multipart types

The module supports `multipart/form-data`, `multipart/mixed`, `multipart/alternative`, `multipart/related`, and
`multipart/parallel` as multipart content types.

### Modify and retrieve the data in an entity

This module provides functions to set and get an entity body from different kinds of message types such as XML, text,
JSON, byte[], and body parts. Headers can be modified through functions such as `addHeader()`, `setHeader()`,
`removeHeader()`, etc.

### Handle large files

The entity object method `setFileAsEntityBody()` can be used to set large files as the entity body and
is able to read it as a stream using the `getByteStream()` function.

## Issues and projects 

Issues and Projects tabs are disabled for this repository as this is part of the Ballerina Standard Library. To report bugs, request new features, start new discussions, view project boards, etc. please visit Ballerina Standard Library [parent repository](https://github.com/ballerina-platform/ballerina-standard-library). 

This repository only contains the source code for the package.

## Build from the source

### Set up the prerequisites

1. Download and install Java SE Development Kit (JDK) version 17 (from one of the following locations).

   * [Oracle](https://www.oracle.com/java/technologies/downloads/)
   
   * [OpenJDK](https://adoptium.net/)
   
        > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.
     
### Build the source

Execute the commands below to build from the source.

1. To build the library:
    ```
    ./gradlew clean build
    ```
   
2. To run the integration tests:
    ```
    ./gradlew clean test
    ```

3. To run a group of tests
    ```
    ./gradlew clean test -Pgroups=<test_group_names>
    ```

4. To build the package without the tests:
    ```
    ./gradlew clean build -x test
    ```
   
5. To debug the tests:
    ```
    ./gradlew clean test -Pdebug=<port>
    ```
   
6. To debug with Ballerina language:
    ```
    ./gradlew clean build -PbalJavaDebug=<port>
    ```

7. Publish the generated artifacts to the local Ballerina central repository:
    ```
    ./gradlew clean build -PpublishToLocalCentral=true
    ```
   
8. Publish the generated artifacts to the Ballerina central repository:
    ```
    ./gradlew clean build -PpublishToCentral=true
    ```

## Contribute to Ballerina

As an open source project, Ballerina welcomes contributions from the community. 

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful links
* For more information go to the [`MIME` library](https://lib.ballerina.io/ballerina/mime/latest).
* For example demonstrations of the usage, go to [Ballerina By Examples](https://ballerina.io/learn/by-example/).
* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
