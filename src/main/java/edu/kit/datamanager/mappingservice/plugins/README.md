# Mapping-Service - How to implement Plugins?

While the mapping-service itself is just a management and code execution service, the actual functionality comes from deployed plugins. 
A plugin implements a specific kind of mapping functionality, typically reading from one file format and mapping, e.g., contained metadata, 
following a set of rules into another file format, i.e., a structure metadata format like XML or JSON. 

Currently, there are three main types of mapping plugins: 

* Java-only plugin: This plugin type is fully implemented in Java. It is called within the service runtime environment and may only need additional libraries 
shipped together with the plugin. In special cases, they may even execute local binaries, but must handle availability checks and error handling on their own.
* Commandline tool plugin: As the name says, this plugin type uses a commandline tool installed on the host machine to perform the mapping operation. Typically, the commandline tools must be installed in beforehand and will just be called from the installation path.
* Python-based plugins: Mostly similar to the commandline tool plugin, these plugins offer their functionality as Python implementation and serve as wrappers. 
The Python code is loaded from a Git repository and they are running in a virtual environment as process monitored by the mapping-service.

Depending on which plugin type you plan to implement, there is a slight difference where you start from. All plugins have in commons, that they require the mapping-plugin-core dependency at compile time. For Gradle and Maven , you can add this dependency as follows:

Gradle:
```groovy
    implementation 'edu.kit.datamanager:mapping-plugin-core:1.1.2'     
```

Maven:
```xml
    <dependency>
        <groupId>edu.kit.datamanager</groupId>
        <artifactId>mapping-plugin-core</artifactId>
        <version>1.1.2</version>
    </dependency>
```

Please check that the version of 'mapping-plugin-core' matches the version of the mapping-service instance you plan to deploy the plugin for. If there are breaking changes they will be indicated by a major version change.

Now you may start implementing the actual plugin. Please see below a simple example based on the InOutPlugin, which is available as part of the mapping-service:

```java
public class InOutPlugin implements IMappingPlugin {

    static Logger LOG = LoggerFactory.getLogger(InOutPlugin.class);

    @Override
    public String name() {
        //Returns the plugin name, which is, together with the plugin version, used to create the unique plugin id.
        return "InOutPlugin";
    }

    @Override
    public String description() {
        //A user readable description of the plugin, which may be used in user frontends.
        return "Simple plugin for testing just returning the input file.";
    }

    @Override
    public String version() {
        //Returns the version of the plugin, which is, together with the plugin version, used to create the unique plugin id.
        return "1.1.2";
    }

    @Override
    public String uri() {
        //Returns the uri of the plugin source code repository, which may be used in user frontends for information, or to check out external plugin code, e.g., in Python-based plugins.
        return "https://github.com/kit-data-manager/mapping-service";
    }

    @Override
    public String[] inputTypes() {
        //Returns a list of input mime types the plugin accepts (currently not checked).
        return new MimeType[]{MimeType.valueOf("application/*")};
    }

    @Override
    public String[] outputTypes() {
        //Returns a list of output mime types the plugin produces (currently not checked).
        return new MimeType[]{MimeType.valueOf("application/*")};
    }

    @Override
    public void setup(ApplicationProperties applicationProperties) {
        //Plugin setup called at startup time of the mapping-service. In case of an error, this method should throw PluginInitializationFailedException to disable the plugin from being used.
        LOG.trace("Plugin {} {} successfully set up.", name(), version());
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        //The actual mapping functionality which uses an implementation-specific mappingFile to transform inputFile into outputFile. As a result, MappingPluginState is returned and may be also 
        //used to propagate errors occured during execution time.
        MappingPluginState result = MappingPluginState.SUCCESS();
        try {
            Files.copy(inputFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | MappingException ex) {
            LOG.error("Failed to execute plugin.", ex);
            result = MappingPluginState.EXECUTION_ERROR();
            result.setDetails("Failed to copy input to output, probably due to an I/O error.");
        }
        return result;
    }

}
```

For more information and concrete implementation examples, please check included plugins available at `src/main/java/edu/kit/datamanager/mappingservice/plugins/impl`. You'll find examples for all three plugin types, i.e.,

* GemmaPlugin: Python-based
* IdentifyPlugin: Commandline tool
* InOutPlugin: Simple example for testing
* JoltPlugin: Java-only plugin

> [!NOTE]
> For Python-based plugins you must additionally provide a properties file telling the plugin which tag to check out and optionally, which minimal Python version is required. The properties file must be located
> in the base folder of the plugin jar file and must be named <LOWERCASE_PLUGIN_NAME>.properties, i.e., myplugin.properties for a plugin named 'MyPlugin'. In the following, an example properties document is shown.

```properties
version=v1.0.0
min.python=3.0.0
```

> [!WARNING]
> To avoid conflicts with properties files of multiple plugin versions it is recommended to remove old versions of a plugin if a new version is deployed.

## Deployment of custom Plugins

Once implemented, the question is how to make own plugins available. Basically, there are two options: 

1. (Recommended) Build a jar file and place it (including its dependencies, if required) at the configured `mapping-service.pluginLocation`
2. Use an own fork of mapping-service, `src/main/java/edu/kit/datamanager/mappingservice/plugins/impl`, and build a custom boot jar

If added correctly, you may find your plugin registered with id **name()_version()** by accessing `http://localhost:8095/api/v1/mappingAdministration/types` (for default deployments). If you add/modify your plugin jar 
at runtime, you may call `http://localhost:8095/api/v1/mappingAdministration/reloadTypes` (for default deployments) to refresh the plugin classloader, before you may find your plugin. Afterwards, you maay create a 
new mapping using your plugin or you can also call it directly for testing purposes using the endpoint `http://localhost:8095/api/v1/mappingAdministration/types/{pluginID}/execute`

> [!IMPORTANT]
> All mappingAdministration endpoints might be protected and only accessible by users with specific roles. During testing it is recommended to disable authentication and use the mapping-service only locally. 