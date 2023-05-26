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
- pip

`./gradlew -Pclean-release build`

### Python Location

Currently, mapping-service requires Python to be installed in order to build and to run. By default, the Python location is set to `/usr/bin/python3`. In case
your Python installation is located elsewhere or you build the mapping-service under Windows, you can provide the Python location externally, i.e.:

```
`./gradlew -Pclean-release build -DpythonLocation=file:///C:/Python310/python.EXE`
```

## How to start

Before you can start the mapping-service, you  first have to create an `application.properties` file in the source folder. As an example you may use `config/application.default.properties`
and modify it according to your needs. Espacially the following properties (at the end of the file) are important:
- `spring.datasource.url=jdbc:h2:file:e:/tmp/mapping-service/database`
The path points to the location of the database in which your configured mappings are stored.
- `mapping-service.pythonLocation=${pythonLocation:'file:///usr/bin/python3'}` \
If no pythonLocation is provided externally (see above) the default `/usr/bin/python3` is used.
- `mapping-service.mappingsLocation:file:///tmp/mapping-service/` \
Enter the location where you want to store your mappings. This folder will be created if it does not exist yet.

You might want to add a plugin to make the service working. Normally, there should be a gemma-plugin-x.x.x.jar in the plugins folder.
If not, you can find its source code [here](https://github.com/maximilianiKIT/gemma-plugin).

After doing this, the mapping-service is ready for the first start. This can be achieved by executing:

`./gradlew bootRun`

### Python Location

Similar to configuring the Python location for the build process, you may also do the same for running the mapping-service via:

```
.\gradlew -DpythonLocation=file:///C:/Python310/python.EXE bootRun
```

## License

See [LICENSE file in this repository](LICENSE).
