package com.mohaseeb.mgmt.tracking.sqlite;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SqliteConfiguration {

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.sqlite.JDBC");
        String dbPath = System.getProperty("user.home") + "/.track_sqlite.db";

        // sqlite3 -header -csv "$HOME/.track_sqlite.db" "select * from segment;" > segments_20190512.csv
        dataSourceBuilder.url("jdbc:sqlite:" + dbPath);
        return dataSourceBuilder.build();
    }
}


