package ro.facultate.pos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RefreshScope
@RestController
public class ConfigDemoController {

    @Value("${app.mesaj-bun-venit:mesaj neconfigurat}")
    private String mesajBunVenit;

    @GetMapping("/api/config-demo")
    public Map<String, String> getMesaj() {
        return Map.of("mesaj", mesajBunVenit);
    }
}
