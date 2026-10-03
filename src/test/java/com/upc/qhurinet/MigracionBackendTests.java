package com.upc.qhurinet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

// La base se prepara con el esquema anterior y un usuario ficticio; no usa datos del equipo.
@EnabledIfEnvironmentVariable(named = "RUN_MIGRATION_TESTS", matches = "true")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/db_qhurinet_migration_test",
        "spring.jpa.hibernate.ddl-auto=update",
        "logging.file.name=target/migration-api.log"
})
@ActiveProfiles("test")
@ContextConfiguration(initializers = BasePruebasInitializer.class)
class MigracionBackendTests {
    @Autowired DataSource dataSource;
    @Test void conservaDatosYEsIdempotente() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        var usuario = jdbc.queryForMap("select * from usuarios where email='legacy@qhurinet.test'");
        assertEquals("hash-legacy", usuario.get("password_hash"));
        assertEquals("Usuario anterior", usuario.get("nombre_completo"));
        assertEquals("yape", usuario.get("metodo_pago_preferido"));
        long antes = jdbc.queryForObject("select count(*) from usuarios", Long.class);
        var script = new ResourceDatabasePopulator(new ClassPathResource("db/cierre-backend.sql"),
                new ClassPathResource("data.sql"), new ClassPathResource("data-demo.sql"));
        script.setSqlScriptEncoding("UTF-8");
        script.execute(dataSource); script.execute(dataSource);
        assertEquals(antes, jdbc.queryForObject("select count(*) from usuarios", Long.class));
        assertEquals(1L, jdbc.queryForObject("select count(*) from metodos_pago_usuario m join usuarios u on u.id=m.usuario_id where u.email='legacy@qhurinet.test'", Long.class));
        assertEquals(8L, jdbc.queryForObject("select count(*) from puntos_reciclaje", Long.class));
        assertEquals(0L, jdbc.queryForObject("select count(*) from puntos_reciclaje where activo=false", Long.class));
        assertEquals("YES", jdbc.queryForObject("select is_nullable from information_schema.columns where table_name='usuarios' and column_name='password_hash'", String.class));
    }
}
