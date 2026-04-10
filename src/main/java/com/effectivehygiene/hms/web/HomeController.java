
package com.effectivehygiene.hms.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }



    @GetMapping("/timeinstance")
    public Map<String, Instant> timeInstanceTest() {
        return Map.of(
                "now Instance.parse", Instant.parse("2026-04-10T10:00:00Z")
        );
    }

    @GetMapping("/timelocaldate")
    public Map<String, LocalDate> timeLocalDateTest() {
        return Map.of(
                "now LocalDate", LocalDate.of(2026, 4, 10)
        );
    }


}
