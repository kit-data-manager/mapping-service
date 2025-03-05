/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.kit.datamanager.mappingservice;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.plugins.PluginLoader;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jejkal
 */
@Configuration
@ComponentScan("edu.kit.datamanager.mappingservice")
public class TestConfig {

    @Bean
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }

    @Bean
    public PluginLoader pluginLoader() {
        return new PluginLoader(applicationProperties());
    }

    @Bean
    public PluginManager pluginManager() {
        return new PluginManager(applicationProperties(), pluginLoader());
    }
}
