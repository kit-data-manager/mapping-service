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
- Python 3
- pip (runtime only)

`./gradlew build`

### Python Location

Currently, mapping-service requires Python to be installed in order to build and to run. At runtime, the Python executable is configured in 
`application.properties`(see below). For building the mapping-service Python executable is set to `/usr/bin/python3` by default. In case you want to build 
the mapping-service on a machine on which the Python installation is located elsewhere, e.g., under Windows, you can provide the Python location 
used at compile time externally, i.e.:

```
.\gradlew "-DpythonExecutable=file:///C:/Python310/python.exe" build
```

## How to start

Before you can start the mapping-service, you  first have to create an `application.properties` file in the source folder. As an example you may use `settings/application.default.properties`
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

## Installation
There are three ways to install metaStore2 as a microservice:
- [Using](#Installation-via-GitHub-Packages) the image available via [GitHub Packages](https://github.com/orgs/kit-data-manager/packages?repo_name=mapping-service) (***recommended***)
- [Building](#Build-docker-container-locally) docker image locally
- [Building](#Build-and-run-locally) and running locally

## Installation via GitHub Packages
### Prerequisites
In order to run this microservice via docker you'll need:

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
Now you'll have to create an image containing the microservice. This can be done via a script.
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
After building image you have to create (and start) a container for executing microservice:
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
