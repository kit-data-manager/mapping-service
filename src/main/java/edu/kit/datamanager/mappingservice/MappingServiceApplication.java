package edu.kit.datamanager.mappingservice;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import edu.kit.datamanager.mappingservice.util.PythonRunnerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EntityScan("edu.kit.datamanager")
@Configuration
public class MappingServiceApplication {
    private static final Logger LOG = LoggerFactory.getLogger(MappingServiceApplication.class);

    @Bean
    @ConfigurationProperties("repo")
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }

    @Bean
    public PluginManager pluginManager(){
        return new PluginManager(applicationProperties());
    }
    
    public static void main(String[] args) {
        SpringApplication.run(MappingServiceApplication.class, args);

        //pluginManager().getListOfAvailableValidators().forEach((value) -> LOG.info("Found validator: " + value));
        //PythonRunnerUtil.printPythonVersion();

        System.out.println("Mapping service is running! Access it at http://localhost:8095");
    }
}
