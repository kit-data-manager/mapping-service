package edu.kit.datamanager.mappingservice;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import edu.kit.datamanager.mappingservice.util.PythonRunnerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
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
    public PluginManager pluginManager() {
        return new PluginManager(applicationProperties());
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(MappingServiceApplication.class, args);

        PluginManager mgr = ctx.getBean(PluginManager.class);
        System.out.println("Found plugins: ");
        mgr.getPlugins().forEach((k, v) -> {
            System.out.println(String.format(" - %s (%s)", k, v));
        });
        System.out.println("Using Python Version: ");
        PythonRunnerUtil.printPythonVersion();
        String port = ctx.getEnvironment().getProperty("server.port");
        System.out.println(String.format("Mapping service is running on port %s.", port));
    }
}
