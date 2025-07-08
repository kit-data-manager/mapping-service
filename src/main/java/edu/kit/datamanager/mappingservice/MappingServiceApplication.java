package edu.kit.datamanager.mappingservice;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import io.micrometer.core.instrument.MeterRegistry;
import edu.kit.datamanager.mappingservice.plugins.PluginLoader;
import edu.kit.datamanager.mappingservice.util.PythonRunnerUtil;
import edu.kit.datamanager.mappingservice.util.ShellRunnerUtil;
import edu.kit.datamanager.security.filter.KeycloakJwtProperties;
import edu.kit.datamanager.security.filter.KeycloakTokenFilter;
import edu.kit.datamanager.security.filter.KeycloakTokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EntityScan("edu.kit.datamanager")
@Configuration
@EnableAsync
public class MappingServiceApplication {

    @Autowired
    private final MeterRegistry meterRegistry;

    MappingServiceApplication(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

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
        PythonRunnerUtil.init(applicationProperties());
        ShellRunnerUtil.init(applicationProperties());
        return new PluginManager(applicationProperties(), pluginLoader(), meterRegistry);
    }

    @Bean
    public KeycloakJwtProperties keycloakProperties() {
        return new KeycloakJwtProperties();
    }

    @Bean
    @ConditionalOnProperty(
            value = "mapping-service.authEnabled",
            havingValue = "true",
            matchIfMissing = false)
    public KeycloakTokenFilter keycloakTokenFilterBean() {
        return new KeycloakTokenFilter(KeycloakTokenValidator.builder()
                .readTimeout(keycloakProperties().getReadTimeoutms())
                .connectTimeout(keycloakProperties().getConnectTimeoutms())
                .sizeLimit(keycloakProperties().getSizeLimit())
                .jwtLocalSecret("vkfvoswsohwrxgjaxipuiyyjgubggzdaqrcuupbugxtnalhiegkppdgjgwxsmvdb")
                .build(keycloakProperties().getJwkUrl(), keycloakProperties().getResource(), keycloakProperties().getJwtClaim()));
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(MappingServiceApplication.class, args);

        PluginManager mgr = ctx.getBean(PluginManager.class);
        System.out.println("Found plugins: ");
        mgr.getPlugins().forEach((k, v) -> {
            System.out.printf(" - %s (%s)%n", k, v);
        });
        System.out.println("Using Python Version: ");
        PythonRunnerUtil.printPythonVersion();
        String port = ctx.getEnvironment().getProperty("server.port");
        System.out.printf("Mapping service is running on port %s.%n", port);
    }
}
