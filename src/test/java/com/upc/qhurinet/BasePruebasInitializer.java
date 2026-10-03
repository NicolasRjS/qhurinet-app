package com.upc.qhurinet;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

// Impide que create-drop se ejecute por error contra la base del equipo.
public class BasePruebasInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        String url = context.getEnvironment().getProperty("spring.datasource.url", "");
        String base = url.substring(url.lastIndexOf('/') + 1).split("\\?")[0];
        if (!base.matches("[a-zA-Z0-9_]*_test(?:_[a-zA-Z0-9]+)*")) {
            throw new IllegalStateException("Las pruebas requieren una base aislada cuyo nombre termine en _test o _test_identificador");
        }
    }
}
