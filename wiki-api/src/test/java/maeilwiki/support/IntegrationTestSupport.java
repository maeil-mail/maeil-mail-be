package maeilwiki.support;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import maeilwiki.TestApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Import(IntegrationTestSupport.TestConfig.class)
public abstract class IntegrationTestSupport {

    @TestConfiguration
    @ConfigurationPropertiesScan(basePackageClasses = TestApplication.class)
    static class TestConfig {

        @Bean(name = "authIntegrationTestJpaQueryFactory")
        public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
        }
    }
}
