package edu.kit.datamanager.mappingservice;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan({"edu.kit.datamanager.mappingservice"})
@EntityScan("edu.kit.datamanager")
@Configuration
@EnableAsync
public class MappingServiceApplication {
    private static final Logger LOG = LoggerFactory.getLogger(MappingServiceApplication.class);

    @Bean
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
