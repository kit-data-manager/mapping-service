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
- pip (runtime only)

`./gradlew -Pclean-release build`

### Python Location

Currently, mapping-service requires Python to be installed in order to build and to run. At runtime, the Python executable is configured in 
`application.properties`(see below). For building the mapping-service Python executable is set to `/usr/bin/python3` by default. In case you want to build 
the mapping-service on a machine on which the Python installation is located elsewhere, e.g., under Windows, you can provide the Python location 
used at compile time externally, i.e.:

```
.\gradlew -Pclean-release "-DpythonExecutable=file:///C:/Python310/python.exe" build
```

## How to start

Before you can start the mapping-service, you  first have to create an `application.properties` file in the source folder. As an example you may use `config/application.default.properties`
and modify it according to your needs. Espacially the following properties (at the end of the file) are important:
- `spring.datasource.url=jdbc:h2:file:/tmp/mapping-service/database`
The path points to the location of the database in which your configured mappings are stored.
- `mapping-service.pythonExecutable=${pythonExecutable:'file:///usr/bin/python3'}` \
If no pythonExecutable is provided externally (see above) the default `/usr/bin/python3` is used.
- `mapping-service.pluginLocation=file:///tmp/mapping-service/plugins` \
The local folder where available plugins are located.
- `mapping-service.mappingsLocation:file:///tmp/mapping-service/` \
Enter the location where you want to store your mappings. This folder will be created if it does not exist yet.

In order to provide the mapping-service with mapping functionality, there are already some pre-compiled plugins available under in the `plugins` folder of this repository.
Copy them to your configured `mapping-service.pluginLocation` to make them available to the mapping-service. 
The source code of the gemma-plugin can be found [here](https://github.com/maximilianiKIT/gemma-plugin). The plugin shows how to integrate Python mappings easily.

There is also the possibility to add new plugins directly at the source tree and create a pluggable Jar out of them. Therefor, check 
`src/main/java/edu/kit/datamanager/mappingservice/plugins/impl`. Just add your new plugin, e.g., based on the `TestPlugin` example. 
In order to make the plugin usable by the mapping service, you then have to build a plugin Jar out of it. In order to do that, just call:

```
./gradlew buildPluginJar
```

This task creates a file `default-plugins-<VERSION>` at `build/libs` which has to be copied to `mapping-service.pluginLocation` to make it available. 

After doing this, the mapping-service is ready for the first start. This can be achieved by executing:

`java -jar build/lib/mapping-service-<VERSION>.jar`

This assumes, that the command is called from the source folder and that your `application.properties` is located in the same folder. 
Otherwise, you may use:

`java -jar build/lib/mapping-service-<VERSION>.jar --spring.config.location=/tmp/application.properties`

Ideally, for production use, you place everything (`mapping-service-<VERSION>.jar`, `application.properties`, `mapping-service.pluginLocation`, `mapping-service.mappingsLocation`,
and `spring.datasource.url`) in a separate folder from where you then call the mapping-service via: 

`java -jar mapping-service-<VERSION>.jar`

## License

See [LICENSE file in this repository](LICENSE).
