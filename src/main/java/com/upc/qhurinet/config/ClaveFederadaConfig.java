package com.upc.qhurinet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Component
public class ClaveFederadaConfig {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String generarHash() {
        byte[] aleatorio = new byte[24];
        new SecureRandom().nextBytes(aleatorio);
        String clave =
                UUID.randomUUID()
                        + Base64.getUrlEncoder().withoutPadding().encodeToString(aleatorio);
        return passwordEncoder.encode(clave);
    }
}
