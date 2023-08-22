package com.my.api.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 기본 DataSource 설정
 *
 * read / write 데이터 소스를 routingDataSource 생성 시 메서드 호출 방식로 생성하지 않고 빈으로 선언한 이유는
 * yml 의 property 를 주입받을 수 있도록 하기 위해서이다.
 * @see ConfigurationPropertiesBindingPostProcessor
 */
@Profile("!local")
@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "database.datasource-read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "database.datasource-write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public DataSource routingDataSource(@Qualifier("readDataSource") DataSource readDataSource,
                                        @Qualifier("writeDataSource") DataSource writeDataSource) {

        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("read", readDataSource);
        dataSourceMap.put("write", writeDataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(readDataSource);

        return routingDataSource;
    }

    /**
     * DataSource 타입의 빈이 여러개이므로 Jpa 가 EntityManager 를 생성할 때 사용할
     * EntityManagerFactoryBuilder 선정에 이 빈을 사용할 수 있도록 @Primary 설정을 해야 한다.
     */
    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    private class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

        @Override
        protected Object determineCurrentLookupKey() {
            String dataSourceType = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "read" : "write";
            log.debug("### current dataSourceType : {}", dataSourceType);
            return dataSourceType;
        }
    }
}
