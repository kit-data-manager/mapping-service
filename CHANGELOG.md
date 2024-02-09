# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
### New Features
### Changed

## [1.0.4] . date 2024-02-09
### Fixed
- Missing project name while building docker via build script.
- Fix relative paths for JS and CSS 

### Changed
- Bump gradle from 7.5.1 to 7.6.4
- Add default context path (mapping-service)

## [1.0.3] - date 2024-02-07
### Fixed
- Improved file extension correction for download allowing to distinguish between different textfile flavours

### New Features
- Dockerization of service

## [1.0.2] - date 2023-07-27
### New Features
- Plugins loading their code via GitHub now also pull changes while being reloaded

### Fixed
- Mappings can now be deleted from the UI
- Mappings with missing mapping files are now visible in the UI and can be deleted

## [1.0.1] - date 2023-06-13
### New Features
- Simple alert is shown if mapping fails to provide some feedback

### Fixed
- No more empty downloads in GUI
- User input and result data for mapping execution gets now reliably removed in case of success and error
- Fixed pythonExecutable config variable name and default value

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

[Unreleased]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.4...HEAD
[1.0.4]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/kit-data-manager/mapping-service/compare/v0.0.4...v1.0.0
[0.0.4]: https://github.com/kit-data-manager/mapping-service/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/kit-data-manager/mapping-service/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/kit-data-manager/mapping-service/tag/v0.0.2
