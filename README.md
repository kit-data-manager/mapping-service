# Mapping-Service

[![Java CI with Gradle](https://github.com/kit-data-manager/mapping-service/actions/workflows/CI.yml/badge.svg)](https://github.com/kit-data-manager/mapping-service/actions/workflows/CI.yml)
[![CodeQL](https://github.com/kit-data-manager/mapping-service/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/kit-data-manager/mapping-service/actions/workflows/codeql-analysis.yml)
[![codecov](https://codecov.io/gh/kit-data-manager/mapping-service/branch/main/graph/badge.svg?token=XFhZruKFaE)](https://codecov.io/gh/kit-data-manager/mapping-service)
[![License](https://img.shields.io/github/license/kit-data-manager/indexing-service.svg)](https://github.com/kit-data-manager/mapping-service/blob/c3ab1c96643b3409774eafd4c8f0843cb9ae2aa1/LICENSE)

The mapping-service provides a generic interface for mapping various documents. The tools required for this, such as Gemma, JOLT, XSLT, ... can be 
loaded into the mapping-service as plugins, regardless of the programming language in which they were developed. These tools are then made usable via the REST-API and a Web-UI. 
The mapping schemas are stored in a database and can be managed via the REST-API and the Web-UI.

The Web-UI is accessible via the following URL: [http://\<IP or hostname>:8095](http://localhost:8095)
The REST-API is documented at the following link: [http://\<IP or hostname>:8095/swagger-ui/index.html](http://localhost:8095/swagger-ui/index.html)

## How to build

Dependencies that are needed to build and are not being downloaded via gradle:

- OpenJDK 17
- (Optional) Python 3
- (Optional) pip (runtime only)

`./gradlew build`

The build can be further customized via different build profiles. Available profiles are: 

* default - Default build including tests. Used by default.
* minimal - Minimal build without tests for fast local builds
* deploy - Full build including tests, packaging of mapping-plugin-core, and 
           deployment to maven-central. This build profile is supposed to be 
           used inside a CI environment, as it requires further configuration, 
           i.e., credentials for deployment.

The different build profiles can be activated via: 

`./gradlew build -PbuildProfile=minimal`

### Python Location

The mapping-service supports plugins running Python code. To provide basic testing for this feature, some tests require configured Python in order to be executable.
While at runtime, the Python executable is configured in application.properties, at build time the Python location may differ depending on the build environment.
By default, '/usr/bin/python3' is assumed as Python location. If you are using a different Python installation, e.g., under Windows or MacOS, you may either modify 
'build.gradle' (look out for pythonExecutable) or you provide the Python executable as command line argument, e.g.,

```
.\gradlew "-DpythonExecutable=file:///C:/Python310/python.exe" build
```

## How to start

Before you can start the mapping-service, you  first have to create an `application.properties` file in the source folder. As an example you may use `settings/application.default.properties`
and modify it according to your needs. Espacially the following properties (at the end of the file) are important:

| Property | Description | Default |
|----------|-------------|---------|
| spring.datasource.url | The path points to the location of the database in which your configured mappings are stored. For production use it is not recommended to use the pre-configured H2 database!    | jdbc:h2:file:/tmp/mapping-service/database   |
| mapping-service.pythonExecutable | The path to your local Python executable. The default uses the pythonExecutable system property provided via -DpythonExecutable= or  file:///usr/bin/python3 if no such system property is provided. | ${pythonExecutable:'file:///usr/bin/python3'}  |
| mapping-service.pluginLocation | The local folder from where plugins are loaded. The folder will be created on startup if it does not exist. | None  |
| mapping-service.codeLocation | The local folder where plugins can checkout code from GitHub. For Python-based plugins, also the virtual env is created in this folder. The folder will be created on startup if it does not exist. | None  |
| mapping-service.mappingSchemasLocation | The local folder where the mapping files are stored. The folder will be created on startup if it does not exist. | None  |
| mapping-service.jobOutput | The local folder where asynchronous mapping execution job outputs are stored. The folder will be created on startup if it does not exist. | None  |
| mapping-service.packagesToScan | Packages scanned for mapping plugins in addition to plugins located in mapping-service.pluginLocation. Typically, this property has not the be changed. | edu.kit.datamanager.mappingservice.plugins.impl  |
| mapping-service.executionTimeout | The timeout in seconds a plugin process, i.e., Python of Shell, may take before it is assumed to be stale. | 30 |
| mapping-service.authEnabled | Defines if authentication is enabled or not. If enabled, additional keycloak configuration is required. | false |
| mapping-service.mappingAdminRole | Defines the user role which must be present to be able to administrate the mapping service, i.e., add or remove mappings. | MAPPING_ADMIN |
| management.metrics.export.prometheus.enabled | Enables or disabled capturing of prometheus metrics. | true |
| management.endpoint.metrics.enabled | Enables or disabled the metrics actuator endpoint. This is only needed, if metrics are captured at all. | true |

## Starting the Mapping-Service

The executable jar of the mapping-service is located at 'build/libs/mapping-service-<VERSION>.jar' You should copy it to some dedicated folder, 
place 'application.properties' next to it, adapt it according to your needs, and start he mapping-service by calling:

`java -jar mapping-service.jar`

If your 'application.properties' is located in another folder, you may use the following call:

`java -jar mapping-service.jar --spring.config.location=/myConfigFolder/application.properties`

## Installation
There are three ways to install the mapping-service as a system service:

- [Using](#Installation-via-GitHub-Packages) the image available via [GitHub Packages](https://github.com/orgs/kit-data-manager/packages?repo_name=mapping-service) (***recommended***)
- [Building](#Build-docker-container-locally) docker image locally
- [Building](#Build-and-run-locally) and running locally

## Installation via GitHub Packages
### Prerequisites
In order to run the mapping-service via docker you'll need:

* [Docker](https://www.docker.com/) 

### Installation
Typically, there is no need for locally building images as all version are accessible via GitHub Packages.
Have a look of available images and their tags [here](https://github.com/orgs/kit-data-manager/packages?repo_name=mapping-service) 
Just follow instructions [below](#Build-docker-container).

## Build docker container locally
### Prerequisites
In order to run this microservice via docker you'll need:

* [Docker](https://www.docker.com/) 
* [git](https://git-scm.com/) 

### Installation
#### Clone repository
First of all you'll have to clone this repository:
```
user@localhost:/home/user/$ git clone https://github.com/kit-data-manager/mapping-service.git
Clone to 'mapping-service'
[...]
user@localhost:/home/user/$ cd mapping-service
user@localhost:/home/user/mapping-service$
```

#### Create image
Now you'll have to create an image containing the mapping-service. This can be done via a script.
On default the created images will be tagged as follows:

*'latest tag'-'actual date(yyyy-mm-dd)'* (e.g.: 1.1.0-2023-06-27)

```
user@localhost:/home/user/mapping-service$ bash docker/buildDocker.sh
---------------------------------------------------------------------------
Build docker container ghcr.io/kit-data-manager/mapping-service:1.2.0-2023-06-27
---------------------------------------------------------------------------
[...]
---------------------------------------------------------------------------
Now you can create and start the container by calling ...
---------------------------------------------------------------------------
user@localhost:/home/user/mapping-service$
```

#### Build docker container
After building image you have to create (and start) a container for executing the mapping-service:
```
# If you want to use a specific image you may list all possible tags first.
user@localhost:/home/user/mapping-service$ docker images ghcr.io/kit-data-manager/mapping-service --format {{.Tag}}
1.2.0-2023-06-27
user@localhost:/home/user/mapping-service$ docker run -d -p8095:8095 --name mapping4docker ghcr.io/kit-data-manager/mapping-service:1.2.0-2023-06-27
57c973e7092bfc3778569f90632d60775dfecd12352f13a4fd2fdf4270865286
user@localhost:/home/user/mapping-service$
```

#### Customize settings
If you want to overwrite default configuration of your docker container you have to
'mount' a config directory containing 'application.properties' with your adapted settings.
Therefor you have to provide an additional flag to the command mentioned before:
```
# Overwriting default settings
# Create config folder
user@localhost:/home/user/mapping-service$ mkdir config
# Place your own 'application.properties' inside the config directory
# Create/run container
user@localhost:/home/user/mapping-service$ docker run -d -p8095:8095 -v `pwd`/config:/spring/mapping-service/config --name mapping4docker ghcr.io/kit-data-manager/mapping-service:1.2.0-2023-06-27
57c973e7092bfc3778569f90632d60775dfecd12352f13a4fd2fdf4270865286
user@localhost:/home/user/mapping-service$
```

#### Stop docker container
If you want to stop container just type
```
user@localhost:/home/user/mapping-service$ docker stop mapping4docker
```

#### (Re)start docker container
If you want to (re)start container just type
```
user@localhost:/home/user/mapping-service$ docker start mapping4docker
```

## Build and run locally
### Prerequisites
In order to run this microservice via docker you'll need:

* [Java SE Development Kit >= 17](https://openjdk.java.net/) 
* [git](https://git-scm.com/) 

### Installation
#### Clone repository
First of all you'll have to clone this repository:
```
user@localhost:/home/user/$ git clone https://github.com/kit-data-manager/mapping-service.git
Clone to 'mapping-service'
[...]
user@localhost:/home/user/$ cd mapping-service
user@localhost:/home/user/mapping-service$
```
#### Build service 
To build service just execute the build.sh script:
```
user@localhost:/home/user/mapping-service$bash build.sh /PATH/TO/EMPTY/INSTALLATION/DIRECTORY
---------------------------------------------------------------------------
Build microservice of mapping-service at /PATH/TO/EMPTY/INSTALLATION/DIRECTORY
---------------------------------------------------------------------------
[...]
---------------------------------------------------------------------------
Now you can start the service by calling /PATH/TO/EMPTY/INSTALLATION/DIRECTORY/run.sh
---------------------------------------------------------------------------
user@localhost:/home/user/mapping-service$
```
#### Customize settings
If you want to overwrite default configuration of your docker container you have to
add a file named 'application.properties' to the 'config' directory inside your installation
path (/PATH/TO/EMPTY/INSTALLATION/DIRECTORY)selected before. The added file should
only contain your adapted settings. e.g. in case you want to change only the port to '1234' your
'application.properties' should look like this:
```
# Overwriting default settings from ../application.properties
# Server settings
server.port: 1234
```

## License

See [LICENSE file in this repository](LICENSE).
