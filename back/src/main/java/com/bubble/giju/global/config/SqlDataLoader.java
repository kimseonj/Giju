package com.bubble.giju.global.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SqlDataLoader implements ApplicationRunner {

    private final DataSource dataSource;

    public SqlDataLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> sqlFiles = List.of(
                "sql/userData.sql",
                "sql/drinkData.sql",
                "sql/deliveryCompainesData.sql",
                "sql/orderData.sql",
                "sql/likesData.sql",
                "sql/reviews.sql"
        );

        try (Connection conn = dataSource.getConnection()) {
            for (String path : sqlFiles) {
                ClassPathResource resource = new ClassPathResource(path);
                String sql = new BufferedReader(new InputStreamReader(resource.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    System.out.println("실행 완료: " + path);
                }
            }
        }
    }
}
