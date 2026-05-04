package com.ims.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Data Source Configuration - Enable JPA and MongoDB repositories
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.ims.repository")
@EnableMongoRepositories(basePackages = "com.ims.repository")
@EnableTransactionManagement
public class DataSourceConfig {
    // Configuration is handled via application properties
    // This class enables the repositories
}