# Mapping-Service

[![Java CI with Gradle](https://github.com/maximilianiKIT/mapping-service/actions/workflows/CI.yml/badge.svg)](https://github.com/maximilianiKIT/mapping-service/actions/workflows/CI.yml)
![License](https://img.shields.io/github/license/kit-data-manager/indexing-service.svg)
[![codecov](https://codecov.io/gh/maximilianiKIT/mapping-service/branch/main/graph/badge.svg?token=XFhZruKFaE)](https://codecov.io/gh/maximilianiKIT/mapping-service)

:warning:
Not fully tested yet!

The mapping service allows multiple schemas to be uploaded and managed and documents to be mapped over one of them at a time accordingly.
Therefore, the service is extensible to other mappers, although currently only Gemma, a service that can only map JSON files, is available.
The REST API is documented at the following link: [http://localhost:8095/swagger-ui/index.html](http://localhost:8095/swagger-ui/index.html)

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
