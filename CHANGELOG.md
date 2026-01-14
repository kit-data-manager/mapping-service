# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed

### New Features

### Changed

## [2.0.0] - data 2026-01-14
### Fixed
* Cloned Git repositories are now properly closed (only relevant for tests)

### New Features
* Added support for JWT authentication (enabled via property *mapping-service.authEnabled*)
* Integration of Prometheus monitoring
* Added endpoint /api/v1/mappingExecution/plugins/<PluginID>/execute for direct execution of plugins, e.g., for testing
* Added JoltPlugin to list of default plugins
* Added JsonataPlugin to list of default plugins
* New configuration property *mapping-service.mappingAdminRole* can be used to authenticate access to administrative endpoints
* plugin-core jar released as separate dependency and can be used for implementing own plugins easier 

### Changed
* Countless dependency updates
* Changed plugin loading behaviour (default plugins are now part of mapping-service and not shipped as jar files)
* Plugin code (Python-based plugins) is now checked out in a configurable folder (mapping-service.codeLocation)
* Changed base class for Python-based plugins to AbstractPythonMappingPlugin
* Plugin version should now match the Git release tag used by Python-based plugins
* Python-based plugins are now creating an own Venv (stored at the codeLocation) to avoid dependency conflicts
* MappingAdministration endpoints /api/v1/mappingAdministration/types|reloadTypes were renamed to /api/v1/mappingAdministration/plugins|reloadPlugins
* MappingAdministration /api/v1/mappingAdministration/reloadPlugins (GET) and PUT|POST /api/v1/mappingAdministration are now secured and can only be accessed from localhost (if authentication is disabled) or by users with the group role defined by property *mapping-service.mappingAdminRole*

## [1.1.1] - date 2025-01-20
### Fixed
- Fixed broken Docker package

## Changed
* Update plugin org.asciidoctor.jvm.convert to v4.0.4 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/150
* Update dependency org.postgresql:postgresql to v42.7.5 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/149
* Update plugin net.researchgate.release to v3.1.0 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/152
* Update plugin org.owasp.dependencycheck to v12 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/155
* Update dependency org.json:json to v20250107 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/154

## [1.1.0] - date 2025-01-20
### New Feature
* Support of asynchronous mapping via /api/v1/mappingExecution/schedule/ (see API docs for more details)

## Changed
* Bump org.postgresql:postgresql from 42.5.0 to 42.7.4 by @dependabot in https://github.com/kit-data-manager/mapping-service/pull/53
* Update dependency jacoco to v0.8.12 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/59
* Update dependency org.springframework.boot:spring-boot-starter-actuator to v2.7.18 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/65
* Update dependency org.springframework.boot:spring-boot-configuration-processor to v2.7.18 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/64
* Update dependency org.apache.httpcomponents:httpclient to v4.5.14 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/60
* Update dependency org.junit.jupiter:junit-jupiter-migrationsupport to v5.11.3 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/85
* Update plugin com.gorylenko.gradle-git-properties to v2.4.2 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/74
* Update dependency org.springframework.boot:spring-boot-starter-web to v2.7.18 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/70
* Update dependency org.springframework.boot:spring-boot-starter-security to v2.7.18 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/67
* Update dependency org.springframework.boot:spring-boot-starter-data-jpa to v2.7.18 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/66
* Update dependency org.springframework.boot:spring-boot-starter-test to v2.7.18 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/68
* Update dependency org.springframework.boot:spring-boot-starter-validation to v2.7.18 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/69
* Update dependency org.springframework:spring-test to v5.3.39 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/73
* Update dependency org.junit.vintage:junit-vintage-engine to v5.11.3 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/87
* Update dependency org.junit:junit-bom to v5.11.3 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/98
* Update dependency com.h2database:h2 to v2.3.232 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/91
* Update dependency org.projectlombok:lombok to v1.18.36 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/89
* Bump org.junit.jupiter:junit-jupiter from 5.9.0 to 5.11.3 by @dependabot in https://github.com/kit-data-manager/mapping-service/pull/106
* Bump com.github.jknack:handlebars from 4.3.0 to 4.4.0 by @dependabot in https://github.com/kit-data-manager/mapping-service/pull/105
* Update dependency org.springdoc:springdoc-openapi-webmvc-core to v1.8.0 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/102
* Update actions/setup-java action to v4 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/112
* Update actions/checkout action to v4 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/109
* Update dependency org.springdoc:springdoc-openapi-data-rest to v1.8.0 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/100
* Update actions/dependency-review-action action to v4 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/110
* Update actions/setup-python action to v5 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/113
* Update dependency org.springframework.security:spring-security-test to v5.8.15 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/103
* Update dependency org.springframework.restdocs:spring-restdocs-asciidoctor to v3.0.3 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/111
* Update dependency org.springdoc:springdoc-openapi-ui to v1.8.0 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/101
* Update github/codeql-action action to v3 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/131
* Update eclipse-temurin Docker tag to v23 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/130
* Update docker/build-push-action action to v6 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/129
* Update plugin io.freefair.maven-publish-java to v8.11 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/128
* Update plugin io.freefair.lombok to v8.11 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/127
* Update dependency org.javers:javers-spring-boot-starter-sql to v7.7.0 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/126
* Update dependency org.springframework.restdocs:spring-restdocs-mockmvc to v3.0.3 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/125
* Update dependency org.json:json to v20240303 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/119
* Update dependency commons-io:commons-io to v2.18.0 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/133
* Update dependency gradle to v8.12 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/146
* Update plugin io.spring.dependency-management to v1.1.7 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/145
* Update dependency com.google.guava:guava to v33.4.0-jre by @renovate in https://github.com/kit-data-manager/mapping-service/pull/144
* Update dependency net.bytebuddy:byte-buddy to v1.16.1 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/143
* Update dependency org.mockito:mockito-core to v5.15.2 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/148
* Update dependency edu.kit.datamanager:service-base to v1.3.3 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/147
* Update plugin com.gradle.enterprise to v3.19 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/142
* Update plugin org.owasp.dependencycheck to v11.1.1 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/141
* Update dependency org.eclipse.jgit:org.eclipse.jgit to v7.1.0.202411261347-r by @renovate in https://github.com/kit-data-manager/mapping-service/pull/140
* Update plugin org.springframework.boot to v3.4.1 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/136
* Update springDocVersion to v2.8.3 by @renovate in https://github.com/kit-data-manager/mapping-service/pull/137

## [1.0.5] . date 2024-08-27
### Changed
- Bump com.google.guava:guava from 31.1-jre to 33.3.0-jre
- Bump org.asciidoctor.jvm.convert from 3.3.0 to 4.0.3
- Bump org.apache.tika:tika-core from 2.7.0 to 2.9.2
- Bump com.gradle.enterprise from 3.9 to 3.18

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

[Unreleased]: https://github.com/kit-data-manager/mapping-service/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/kit-data-manager/mapping-service/compare/v1.1.1...v2.0.0
[1.1.1]: https://github.com/kit-data-manager/mapping-service/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.5...v1.1.0
[1.0.5]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/kit-data-manager/mapping-service/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/kit-data-manager/mapping-service/compare/v0.0.4...v1.0.0
[0.0.4]: https://github.com/kit-data-manager/mapping-service/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/kit-data-manager/mapping-service/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/kit-data-manager/mapping-service/tag/v0.0.2
