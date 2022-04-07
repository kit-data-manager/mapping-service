# Mapping-Service

[![Java CI with Gradle](https://github.com/maximilianiKIT/mapping-service/actions/workflows/CI.yml/badge.svg)](https://github.com/maximilianiKIT/mapping-service/actions/workflows/CI.yml)
![License](https://img.shields.io/github/license/kit-data-manager/indexing-service.svg)

:warning:
Not fully tested yet!
For mapping documents only Gemma is available currently!

The mapping service allows multiple schemas to be uploaded and managed and documents to be mapped over one of them at a time accordingly.
Therefore, the service is extensible to other mappers, although currently only Gemma, a service that can only map JSON files, is available.
The REST API is documented at the following link: [http://localhost:8095/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config](http://localhost:8095/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config)

## How to build

Dependencies that are needed to build and are not being downloaded via gradle:

- OpenJDK 17

`./gradlew -Pclean-release build`

## How to start

> TODO This section is a placeholder. It still needs to be written properly.

### Prerequisites

You might want to take a look at testbed4inf, which should make it easy to satisfy those.

- Gemma
- Python

### Install Python and Gemma
```
sudo apt install -y python3 python3-pip 
pip3 install xmltodict wget
```


## More information

## License

See [LICENSE file in this repository](LICENSE).