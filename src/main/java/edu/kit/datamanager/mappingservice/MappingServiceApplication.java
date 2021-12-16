package edu.kit.datamanager.mappingservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kit.datamanager.mappingservice.indexer.configuration.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan({"edu.kit.datamanager.mappingservice.indexer", "edu.kit.datamanager.messaging.client.configuration", "edu.kit.datamanager.messaging.client.receiver", "edu.kit.datamanager.configuration"})
@EntityScan("edu.kit.datamanager")
public class MappingServiceApplication {

	@Bean
	@Scope("prototype")
	public Logger logger(InjectionPoint injectionPoint) {
		Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
		return LoggerFactory.getLogger(targetClass.getCanonicalName());
	}

	@Bean(name = "OBJECT_MAPPER_BEAN")
	public ObjectMapper jsonObjectMapper(){
		return Jackson2ObjectMapperBuilder.json()
				.serializationInclusion(JsonInclude.Include.NON_EMPTY) // Don’t include null values
				.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //ISODate
				.modules(new JavaTimeModule())
				.build();
	}
	@Bean
	@ConfigurationProperties("repo")
	public ApplicationProperties applicationProperties(){
		return new ApplicationProperties();
	}

	public static void main(String[] args) {
		SpringApplication.run(MappingServiceApplication.class, args);
		System.out.println("Mapping service is running!");
	}
}
