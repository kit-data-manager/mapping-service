package edu.kit.datamanager.mappingservice.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author jejkal
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "edu.kit.datamanager.mappingservice")
@EntityScan(basePackages = {"edu.kit.datamanager.mappingservice.domain"})
public class JPAPersistenceConfig{

}
