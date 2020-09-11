package edu.kit.datamanager.indexer;

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
import edu.kit.datamanager.messaging.client.configuration.RabbitMQConsumerConfiguration;
import edu.kit.datamanager.messaging.client.Application;

@SpringBootApplication
@EnableScheduling
@ComponentScan(
	basePackages = {"edu.kit.datamanager.indexer", "edu.kit.datamanager.messaging.client", "edu.kit.datamanager.python.gemma"},
	excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Application.class)
)
public class IndexerApplication {

	@Bean
    @Scope("prototype")
    public Logger logger(InjectionPoint injectionPoint) {
        Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
        return LoggerFactory.getLogger(targetClass.getCanonicalName());
    }

	@Bean
	private static IConsumerEngine consumer() {
		return new DummyConsumer();
	}

	public static void main(String[] args) {
		SpringApplication.run(IndexerApplication.class, args);
		System.out.println("Spring is running!");
	}

}
