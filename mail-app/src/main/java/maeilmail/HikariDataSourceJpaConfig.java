package maeilmail;

import static maeilmail.BasePackages.MAEIL_MAIL;
import static maeilmail.BasePackages.MAEIL_WIKI;
import static maeilmail.HikariDataSourceJpaConfig.ENTITY_MANAGER_FACTORY;
import static maeilmail.HikariDataSourceJpaConfig.TRANSACTION_MANAGER;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = {MAEIL_MAIL, MAEIL_WIKI},
        transactionManagerRef = TRANSACTION_MANAGER,
        entityManagerFactoryRef = ENTITY_MANAGER_FACTORY
)
@EntityScan(basePackages = {MAEIL_MAIL, MAEIL_WIKI})
class HikariDataSourceJpaConfig {

    public static final String TRANSACTION_MANAGER = "maeilmailTransactionManager";
    public static final String ENTITY_MANAGER_FACTORY = "maeilmailEntityManagerFactory";
    public static final String JPA_PROPERTY = "maeilmailJpaProperty";
    public static final String HIBERNATE_PROPERTY = "maeilmailHibernateProperty";
    public static final String DATA_SOURCE = "maeilmailDataSource";
    public static final String PERSIST_UNIT = "maeilmail";

    @Bean(name = TRANSACTION_MANAGER)
    public PlatformTransactionManager transactionManager(@Qualifier(ENTITY_MANAGER_FACTORY) EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            @Qualifier(JPA_PROPERTY) JpaProperties jpaProperties,
            @Qualifier(HIBERNATE_PROPERTY) HibernateProperties hibernateProperties
    ) {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), jpaProperties.getProperties(), null)
                .dataSource(dataSource())
                .properties(
                        hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(),
                                new HibernateSettings()))
                .persistenceUnit(PERSIST_UNIT)
                .packages(new String[]{MAEIL_MAIL, MAEIL_WIKI})
                .build();
    }

    @Bean(name = JPA_PROPERTY)
    @ConfigurationProperties(prefix = "spring.jpa")
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean(name = HIBERNATE_PROPERTY)
    @ConfigurationProperties(prefix = "spring.jpa.hibernate")
    public HibernateProperties hibernateProperties() {
        return new HibernateProperties();
    }

    @Primary
    @Bean(name = DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }
}
