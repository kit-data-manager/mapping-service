# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

##[Unreleased]

## [1.0.1]
### Fixed
- No more empty downloads in GUI
- User input and result data for mapping execution gets now reliably removed in case of success and error

## [1.0.0] - date 2023-06-09
### New Features
- Plugin location is now configurable in application.properties
- Included mapping UI now returns results with proper mime type and extension

### Fixed
- Fixed format/extension detection issues of mapping result
- Fixed inconsistent response codes in endpoints
- Fixed several endpoint documentation issues
- Fixed issues in included mapping UI (wrong links to sub-pages, failed loading of existing mappings, hard-coded return of JSON result)

## [0.0.4] - date 2020-12-16
### Fixed
- Dockerfile for dockerhub

## [0.0.3] - date 2020-12-16
### Added
- Dockerfile for dockerhub
- Travis for CI

## [0.0.2] - date 2020-12-15
First version supporting registering of mappings (Gemma only)
and mapping of metadata documents delivered by RabbitMQ
### Added
- Registration of mapping documents.
- Mapping of metadata documents with Gemma
- Ingest to elasticsearch

[Unreleased]: https://github.com/kit-data-manager/indexing-service/compare/v1.0.1...HEAD
[1.0.1]: https://github.com/kit-data-manager/indexing-service/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/kit-data-manager/indexing-service/compare/v0.0.4...v1.0.0
[0.0.4]: https://github.com/kit-data-manager/indexing-service/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/kit-data-manager/indexing-service/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/kit-data-manager/metastore2/indexing-service/tag/v0.0.2
