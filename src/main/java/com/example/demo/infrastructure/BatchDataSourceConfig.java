package com.example.demo.infrastructure;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchDataSourceConfig {

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource.hikari.batch")
  public DataSource batchDataSource() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }

  @Bean
  @Primary
  public PlatformTransactionManager batchTransactionManager(
      @Qualifier("batchDataSource") DataSource batchDataSource) {
    return new DataSourceTransactionManager(batchDataSource);
  }

}
