package com.greatestbanking.orchestrator.api.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(Environment environment) {
        HikariDataSource dataSource = new HikariDataSource();
        DatabaseSettings settings = resolveSettings(environment);
        dataSource.setJdbcUrl(settings.jdbcUrl());
        dataSource.setUsername(settings.username());
        dataSource.setPassword(settings.password());
        return dataSource;
    }

    private DatabaseSettings resolveSettings(Environment environment) {
        String renderDatabaseUrl = environment.getProperty("DATABASE_URL");
        if (renderDatabaseUrl != null && renderDatabaseUrl.startsWith("postgresql://")) {
            return fromRenderUrl(renderDatabaseUrl);
        }

        return new DatabaseSettings(
            environment.getRequiredProperty("spring.datasource.url"),
            environment.getRequiredProperty("spring.datasource.username"),
            environment.getProperty("spring.datasource.password", "")
        );
    }

    private DatabaseSettings fromRenderUrl(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String[] userInfo = uri.getUserInfo() == null ? new String[]{"", ""} : uri.getUserInfo().split(":", 2);
        String username = decode(userInfo[0]);
        String password = userInfo.length > 1 ? decode(userInfo[1]) : "";
        String query = uri.getRawQuery() == null || uri.getRawQuery().isBlank() ? "" : "?" + uri.getRawQuery();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath() + query;
        return new DatabaseSettings(jdbcUrl, username, password);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private record DatabaseSettings(String jdbcUrl, String username, String password) {}
}
