# Change Log
This file contains all the notable changes done to the Ballerina MIME package through the releases.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to 
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

## [2.10.1] - 2024-10-15

## Changed
- [Fix packing snapshot dependencies](https://github.com/ballerina-platform/ballerina-library/issues/7266)

## [2.10.0] - 2024-08-20

### Added
- [Add MIME entity methods to handle server sent events](https://github.com/ballerina-platform/ballerina-library/issues/6739)

## Changed
- [Make some of the Java classes proper utility classes](https://github.com/ballerina-platform/ballerina-standard-library/issues/4940)

## [2.9.0] - 2023-09-15

### Changed
- [Change `com.sun.activation:jakarta.activation` to `jakarta.activation:jakarta.activation-api`](https://github.com/ballerina-platform/ballerina-standard-library/issues/4789)

## [2.6.0] - 2022-02-20

### Fixed
- [Binary payload retrieved from the `http:Request` has different content-length than the original payload](https://github.com/ballerina-platform/ballerina-standard-library/issues/3662)

## [2.5.0] - 2022-11-29

### Changed
- [API docs updated](https://github.com/ballerina-platform/ballerina-standard-library/issues/3463)

## [2.0.0] - 2021-10-10

### Fixed
- [Unable to get more than 8kb size of byte[] chunks in http byte[] stream](https://github.com/ballerina-platform/ballerina-standard-library/issues/2002)

## [1.1.0-alpha6] - 2021-04-02

### Changed
 - Update Stream return type with nil
 - Update unused variables with inferred type including error
 
