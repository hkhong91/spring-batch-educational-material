package com.example.demo.infrastructure;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {
        "com.example.demo.domain.repository"
    },
    entityManagerFactoryRef = "demoEntityManagerFactory",
    transactionManagerRef = "demoTransactionManager"
)
public class DemoDataManagerConfig {

  private final DataSource demoDataSource;

  public DemoDataManagerConfig(
      @Qualifier("demoDataSource") DataSource demoDataSource) {
    this.demoDataSource = demoDataSource;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean demoEntityManagerFactory() {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(Boolean.TRUE);

    em.setDataSource(this.demoDataSource);
    em.setPersistenceUnitName("demoEntityManager");
    em.setPackagesToScan(
        "com.example.demo.domain.entity"
    );
    em.setJpaVendorAdapter(vendorAdapter);
    em.setJpaProperties(demoJpaProperties());
    return em;
  }

  private Properties demoJpaProperties() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.ddl-auto", "update");
    properties.setProperty("hibernate.show_sql", "true");
    return properties;
  }
}
