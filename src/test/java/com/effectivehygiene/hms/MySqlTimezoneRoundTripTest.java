package com.effectivehygiene.hms;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@ActiveProfiles("dev")
@SpringBootTest(properties = "spring.flyway.enabled=false")
class MySqlTimezoneRoundTripTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void instant_written_is_same_when_read_back() {
        jdbc.execute("DROP TABLE IF EXISTS tz_smoke");
        jdbc.execute("""
            CREATE TABLE tz_smoke (
              id INT PRIMARY KEY AUTO_INCREMENT,
              ts TIMESTAMP(6) NOT NULL
            )
        """);

        Instant expected = Instant.parse("2026-04-10T10:00:00.123456Z");

        // Write
        jdbc.update("INSERT INTO tz_smoke(ts) VALUES (?)", Timestamp.from(expected));

        // Read
        Timestamp readTs = jdbc.queryForObject(
                "SELECT ts FROM tz_smoke WHERE id = 1",
                Timestamp.class
        );


        String sessionTz = jdbc.queryForObject("SELECT @@session.time_zone", String.class);
        assertThat(sessionTz).isIn("UTC", "+00:00");


        Instant actual = readTs.toInstant();

        // Assert: must match exactly
        assertThat(actual).isEqualTo(expected);
    }
}
