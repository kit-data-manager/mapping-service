# Mapping-Service

[![Java CI with Gradle](https://github.com/maximilianiKIT/mapping-service/actions/workflows/CI.yml/badge.svg)](https://github.com/maximilianiKIT/mapping-service/actions/workflows/CI.yml)
[![CodeQL](https://github.com/maximilianiKIT/mapping-service/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/maximilianiKIT/mapping-service/actions/workflows/codeql-analysis.yml)
[![codecov](https://codecov.io/gh/maximilianiKIT/mapping-service/branch/main/graph/badge.svg?token=XFhZruKFaE)](https://codecov.io/gh/maximilianiKIT/mapping-service)
[![License](https://img.shields.io/github/license/kit-data-manager/indexing-service.svg)](https://github.com/maximilianiKIT/mapping-service/blob/c3ab1c96643b3409774eafd4c8f0843cb9ae2aa1/LICENSE)

:warning:
Not fully tested yet!
The mapping-service provides a generic interface for mapping various documents. 
The tools required for this, such as Gemma, JOLT, XSLT, ... can be loaded into the mapping-service as plugins, regardless of the programming language in which they were developed. 
These tools are then made usable via the REST-API and a Web-UI. 
The mapping schemas are stored in a database and can be managed via the REST-API and the Web-UI.

The Web-UI is accessible via the following URL: [http://\<IP or hostname>:8095](http://localhost:8095)
The REST-API is documented at the following link: [http://\<IP or hostname>:8095/swagger-ui/index.html](http://localhost:8095/swagger-ui/index.html)

## How to build

Dependencies that are needed to build and are not being downloaded via gradle:

- OpenJDK 17
- Python 3

`./gradlew -Pclean-release build`

## How to start

`./gradlew bootRun`

### Prerequisites
Please make sure the application.properties file is configured correctly.
Espacially the following properties (at the end of the file) are important:
- `mapping-service.pythonLocation=file:///opt/homebrew/bin/python3` \
Enter the loacation of your python3 installation. You can determine it by typing `which python3` in your terminal.
- `mapping-service.mappingsLocation:file:///tmp/mapping-service/` \
Enter the location where you want to store your mappings. This folder will be created if it does not exist yet.

You might want to add a plugin to make the service working. Normally, there should be a gemma-plugin-x.x.x.jar in the plugins folder.
If not, you can find its source code [here](https://github.com/maximilianiKIT/gemma-plugin).

## License

See [LICENSE file in this repository](LICENSE).
