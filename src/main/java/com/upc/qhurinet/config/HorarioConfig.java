package com.upc.qhurinet.config;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@Configuration
@EnableScheduling
public class HorarioConfig {

    @PostConstruct
    public void configurar() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
    }
}
