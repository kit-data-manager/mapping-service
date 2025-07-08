/*
 * Copyright 2022 Karlsruhe Institute of Technology.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.mappingservice.plugins;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.exception.PluginInitializationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * Class for loading plugins.
 *
 * @author maximilianiKIT
 */
@Component
public class PluginLoader {

    /**
     * Logger for this class.
     */
    static Logger LOG = LoggerFactory.getLogger(PluginLoader.class);

    private ClassLoader cl = null;

    private final ApplicationProperties applicationProperties;

    @Autowired
    public PluginLoader(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void unload() {
        cl = null;
        System.gc();
    }

    /**
     * Load plugins from a given directory.
     *
     * @param pluginDir Directory containing plugins.
     * @param packagesToScan Packages to scan in addition for plugins.
     *
     * @return Map of plugins.
     *
     * @throws IOException If there is an error with the file system.
     * @throws MappingPluginException If there is an error with the plugin or
     * the input.
     */
    public Map<String, IMappingPlugin> loadPlugins(File pluginDir, String[] packagesToScan) throws IOException, MappingPluginException {
        Map<String, IMappingPlugin> result = new HashMap<>();
        File[] pluginJars = new File[0];
        if (pluginDir == null || pluginDir.getAbsolutePath().isBlank()) {
            LOG.warn("Plugin folder {} is not defined. MappingService will only use plugins in classpath.", pluginDir);
        } else {
            pluginJars = pluginDir.listFiles(new JARFileFilter());
        }

        if (pluginJars != null && pluginJars.length > 0) {
            cl = new URLClassLoader(fileArrayToURLArray(pluginJars), Thread.currentThread().getContextClassLoader());
        } else {
            cl = Thread.currentThread().getContextClassLoader();
        }

        List<Class<IMappingPlugin>> pluginClasses = extractClassesFromJARs(pluginJars, packagesToScan, cl);
        List<IMappingPlugin> IMappingPluginList = createPluggableObjects(pluginClasses);

        for (IMappingPlugin i : IMappingPluginList) {
            try {
                i.setup(applicationProperties);
                LOG.trace(" - Adding new plugin {}, v{} to available list", i.name(), i.version());
                result.put(i.id(), i);
            } catch (PluginInitializationFailedException re) {
                LOG.error("Failed to initialize plugin {}, version {}. Plugin will be ignored.", i.name(), i.version(), re);
            }
        }

        return result;
    }

    private URL[] fileArrayToURLArray(File[] files) throws MalformedURLException {
        URL[] urls = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }

    private List<Class<IMappingPlugin>> extractClassesFromJARs(File[] jars, String[] packagesToScan, ClassLoader cl) throws IOException, MappingPluginException {
        LOG.trace("Extracting classes from plugin JARs.");
        List<Class<IMappingPlugin>> classes = new ArrayList<>();
        if (jars != null) {
            for (File jar : jars) {
                LOG.trace("Processing file {}.", jar.getAbsolutePath());
                classes.addAll(extractClassesFromJAR(jar, cl));
            }
        }
        LOG.trace("Found {} plugin classes in jar files.", classes.size());

        if (packagesToScan != null) {
            LOG.trace("Extracting classes from classpath.");
            int pluginCnt = 0;

            findAllClasses("edu.kit.datamanager.mappingservice", cl);

            for (String pkg : packagesToScan) {
                LOG.trace(" - Scanning package {}", pkg);

                List<Class<?>> result = findAllClasses(pkg, cl);

                for (Class<?> res : result) {
                    classes.add((Class<IMappingPlugin>) res);
                    pluginCnt++;
                }
            }
            LOG.trace("Found {} plugin classes in classpath.", pluginCnt);
        }

        return classes;
    }

    private List<Class<IMappingPlugin>> extractClassesFromJAR(File jar, ClassLoader cl) throws IOException, MappingPluginException {
        LOG.trace("Extracting plugin classes from file {}.", jar.getAbsolutePath());
        List<Class<IMappingPlugin>> classes = new ArrayList<>();
        try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jar))) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".class")) {
                    try {
                        Class<?> cls = cl.loadClass(entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.'));
                        LOG.trace("Checking {}.", cls);
                        if (isPluggableClass(cls)) {
                            LOG.trace("Plugin class found.");
                            classes.add((Class<IMappingPlugin>) cls);
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError e) {
                        LOG.info("Can't load Class {}", entry.getName());
                        throw new MappingPluginException(MappingPluginState.UNKNOWN_ERROR(), "Can't load Class " + entry.getName(), e);
                    }
                }
            }
        }
        return classes;
    }

    private boolean isPluggableClass(Class<?> cls) {
        //this should be much easier and faster
        return IMappingPlugin.class.isAssignableFrom(cls) && !cls.isInterface();
    }

    private List<IMappingPlugin> createPluggableObjects(List<Class<IMappingPlugin>> pluggable) throws MappingPluginException {
        LOG.trace("Instantiating plugins from list: {}", pluggable);
        List<IMappingPlugin> plugs = new ArrayList<>(pluggable.size());
        for (Class<IMappingPlugin> plug : pluggable) {
            LOG.trace("Instantiating plugin from class {}.", plug);
            try {
                plugs.add(plug.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                LOG.info("Can't instantiate plugin: {}", plug.getName());
                throw new MappingPluginException(MappingPluginState.UNKNOWN_ERROR(), "Can't instantiate plugin: " + plug.getName(), e);
            } catch (IllegalAccessException e) {
                LOG.info("IllegalAccess for plugin: {}", plug.getName());
                throw new MappingPluginException(MappingPluginState.UNKNOWN_ERROR(), "IllegalAccess for plugin: " + plug.getName(), e);
            }
        }
        return plugs;
    }

    protected List<Class<?>> findAllClasses(String packageName, ClassLoader loader) {
        List<Class<?>> result = new ArrayList<>();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
                loader);
        try {
            Resource[] resources = scan(loader, packageName);
            for (Resource resource : resources) {
                Class<?> clazz = loadClass(loader, metadataReaderFactory, resource);
                if (clazz != null && isPluggableClass(clazz)) {
                    result.add(clazz);
                }
            }
        } catch (IOException ex) {
            //throw new IllegalStateException(ex);
            return result;
        }
        return result;
    }

    private Resource[] scan(ClassLoader loader, String packageName) throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
                loader);
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(packageName) + "/**/*.class";
        return resolver.getResources(pattern);
    }

    private Class<?> loadClass(ClassLoader loader, MetadataReaderFactory readerFactory,
            Resource resource) {
        try {
            MetadataReader reader = readerFactory.getMetadataReader(resource);
            return ClassUtils.forName(reader.getClassMetadata().getClassName(), loader);
        } catch (Throwable ex) {
            return null;
        }
    }
}
