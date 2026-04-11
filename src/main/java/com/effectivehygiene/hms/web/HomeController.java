
package com.effectivehygiene.hms.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Profile("dev")
@RestController
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }



    @GetMapping("/timeinstance")
    public Map<String, Instant> timeInstanceTest() {
        return Map.of(
                "format check of Instance.parse", Instant.parse("2026-04-10T10:00:00Z"),
                "now actual time", Instant.now()
        );
    }

    @GetMapping("/timelocaldate")
    public Map<String, LocalDate> timeLocalDateTest() {
        return Map.of(
                "now LocalDate", LocalDate.of(2026, 4, 10)
        );
    }


    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/api/ping")
    public String ping() {
        log.info("Ping called");
        return "ok";
    }
}
