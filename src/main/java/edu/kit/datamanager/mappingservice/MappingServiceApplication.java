package edu.kit.datamanager.mappingservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.util.PythonRunnerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.net.MalformedURLException;

@SpringBootApplication
@EnableScheduling
@ComponentScan({"edu.kit.datamanager.mappingservice", "edu.kit.datamanager.configuration"})
@EntityScan("edu.kit.datamanager")
public class MappingServiceApplication {

    @Bean
    @Scope("prototype")
    public Logger logger(InjectionPoint injectionPoint) {
        Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
        return LoggerFactory.getLogger(targetClass.getCanonicalName());
    }

    @Bean(name = "OBJECT_MAPPER_BEAN")
    public ObjectMapper jsonObjectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .serializationInclusion(JsonInclude.Include.NON_EMPTY) // Don’t include null values
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //ISODate
                .modules(new JavaTimeModule())
                .build();
    }

    @Bean
    @ConfigurationProperties("repo")
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }

    public static void main(String[] args) {
        SpringApplication.run(MappingServiceApplication.class, args);
        System.out.println("Mapping service is running! Access it at http://localhost:8095");

        ApplicationProperties properties = new ApplicationProperties();
        try {
//            properties.setPythonLocation(new File("/opt/homebrew/opt/python@3.10/bin/python3".trim()).toURI().toURL());
            properties.setPythonLocation(new File("/usr/bin/python3").toURI().toURL());
            properties.setGemmaLocation(new File("src/test/resources/python/mapping_single.py".trim()).toURI().toURL());
            properties.setMappingsLocation(new File("/tmp/mapping-service/".trim()).toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            PythonRunnerUtil runner = new PythonRunnerUtil(properties);
            runner.printPythonVersion();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
