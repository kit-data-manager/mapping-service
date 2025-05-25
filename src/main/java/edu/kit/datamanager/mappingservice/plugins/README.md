# Mapping-Service - How to implement Plugins?

While the mapping-service itself is just a management and code execution service, the actual functionality comes from deployed plugins. 
A plugin implements a specific kind of mapping functionality, typically reading from one file format and mapping, e.g., contained metadata, 
following a set of rules into another file format, i.e., a structure metadata format like XML or JSON. 

Currently, there are two main types of mapping plugins: 

* Java-only plugin: This plugin type is fully implemented in Java. It is called within the service runtime environment and may only need additional libraries 
shipped together with the plugin. In special cases, they may even execute local binaries, but must handle availability checks and error handling on their own.
* Python-based plugins: As the name says, these plugins offer their functionality as Python implementation and serve as wrappers. 
The Python code is loaded from a Git repository and they are running in a virtual environment as process monitored by the mapping-service.

Depending on which plugin type you plan to implement, there is a slight difference where you start from. Let's begin with Java-only plugins.

## Implement Java-only plugins

Before you start implementing, you should setup a new Java project, e.g., using Gradle or Maven. If you are finished, add the following dependency to your project:

Gradle:
```
    implementation 'edu.kit.datamanager:mapping-plugin-core:1.1.2'     
```

Maven:
```
    <dependency>
        <groupId>edu.kit.datamanager</groupId>
        <artifactId>mapping-plugin-core</artifactId>
        <version>1.1.2</version>
    </dependency>
```

Please check that the version of 'mapping-plugin-core' matches the version of the mapping-service instance you plan to deploy the plugin for. 
If there are breaking changes they will be indicated by a major version change.

Now we can start implementing the actual plugin code. As an example, let's take the InOutPlugin shipped with the mapping-service and go through the single elements.

```java
public class InOutPlugin implements IMappingPlugin {

    static Logger LOG = LoggerFactory.getLogger(InOutPlugin.class);

    @Override
    public String name() {
        return "InOutPlugin";
    }

    @Override
    public String description() {
        return "Simple plugin for testing just returning the input file.";
    }

    @Override
    public String version() {
        return "1.1.2";
    }

    @Override
    public String uri() {
        return "https://github.com/kit-data-manager/mapping-service";
    }

    @Override
    public MimeType[] inputTypes() {
        return new MimeType[]{MimeType.valueOf("application/*")};
    }

    @Override
    public MimeType[] outputTypes() {
        return new MimeType[]{MimeType.valueOf("application/*")};
    }

    @Override
    public void setup(ApplicationProperties applicationProperties) {
        //nothing to do here
        LOG.trace("Plugin {} {} successfully set up.", name(), version());
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
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

1. InOutPlugin implements IMappingPlugin - All Java-only plugins must implement IMappingPlugin, which defined the interface used by the mapping-service to interact with the plugin.
2. name(), description(), version(), uri() - These methods provide metadata and to uniquely address plugins, e.g., the plugin id will be **name()_version()**