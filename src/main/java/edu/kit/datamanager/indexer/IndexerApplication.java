package edu.kit.datamanager.indexer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.python.gemma.GemmaConfiguration;
import edu.kit.datamanager.indexer.configuration.IndexerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

import edu.kit.datamanager.indexer.consumer.DummyConsumer;
import edu.kit.datamanager.indexer.consumer.IConsumerEngine;
import edu.kit.datamanager.messaging.client.Application;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
@EnableScheduling
@ComponentScan({"edu.kit.datamanager.indexer"})
public class IndexerApplication {

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
  public ApplicationProperties metastoreProperties(){
    return new ApplicationProperties();
  }

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(IndexerApplication.class, args);
		System.out.println("Spring is running!");
	}

}
