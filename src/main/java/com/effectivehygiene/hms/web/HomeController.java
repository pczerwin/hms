
package com.effectivehygiene.hms.web;


import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }

}
