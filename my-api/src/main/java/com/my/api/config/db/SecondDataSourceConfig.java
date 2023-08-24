package com.my.api.config.db;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Profile("!local")
@Slf4j
@ConditionalOnProperty(name = "database2.datasource-write.jdbc-url")
@Configuration
public class SecondDataSourceConfig {

    @Bean("secondReadDataSource")
    @ConfigurationProperties(prefix = "database2.datasource-read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("secondWriteDataSource")
    @ConfigurationProperties(prefix = "database2.datasource-write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("secondRoutingDataSource")
    public DataSource routingDataSource(@Qualifier("secondReadDataSource") DataSource readDataSource,
                                        @Qualifier("secondWriteDataSource") DataSource writeDataSource) {
        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("read", readDataSource);
        dataSourceMap.put("write", writeDataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(readDataSource);

        return routingDataSource;
    }

    @Bean("secondDataSource")
    public DataSource dataSource(@Qualifier("secondRoutingDataSource") DataSource routingDataSource) {
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
