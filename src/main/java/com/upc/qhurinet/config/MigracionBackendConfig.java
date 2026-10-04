package com.upc.qhurinet.config;

import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.UsuarioRepositorio;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@DependsOnDatabaseInitialization
public class MigracionBackendConfig implements ApplicationRunner {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ClaveFederadaConfig claveFederadaConfig;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) throws IOException {
        // JDBC ejecuta el script completo y conserva los bloques DO de PostgreSQL.
        String script =
                new ClassPathResource("db/cierre-backend.sql")
                        .getContentAsString(StandardCharsets.UTF_8);
        jdbcTemplate.execute(script);
        for (Usuario usuario : usuarioRepositorio.findByPasswordHashIsNull()) {
            usuario.setPasswordHash(claveFederadaConfig.generarHash());
            usuarioRepositorio.save(usuario);
        }
        usuarioRepositorio.flush();
        // El hash se calcula en Java antes de restaurar la restriccion.
        jdbcTemplate.execute("ALTER TABLE usuarios ALTER COLUMN password_hash SET NOT NULL");
    }
}
