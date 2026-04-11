

DROP TABLE IF EXISTS tz_smoke;
CREATE TABLE tz_smoke (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          ts TIMESTAMP(6) NOT NULL
);
INSERT INTO tz_smoke(ts) VALUES ('2026-04-10 10:00:00.123456');
SELECT id, ts, UNIX_TIMESTAMP(ts) AS epoch_seconds
FROM tz_smoke;