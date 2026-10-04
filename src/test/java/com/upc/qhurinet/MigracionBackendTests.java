package com.upc.qhurinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.upc.qhurinet.dtos.MetodoPagoUsuarioDTO;
import com.upc.qhurinet.services.UsuarioService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Set;

@EnabledIfEnvironmentVariable(named = "RUN_MIGRATION_TESTS", matches = "true")
class MigracionBackendTests {
    private static final Set<String> BASES =
            Set.of("db_qhurinet_migration_v79_test", "db_qhurinet_migration_cierre_test");
    private static final Set<String> TABLAS =
            Set.of(
                    "roles",
                    "usuarios",
                    "documentos_verificacion",
                    "categorias_material",
                    "publicaciones_material",
                    "puntos_reciclaje",
                    "punto_reciclaje_materiales",
                    "solicitudes_recoleccion",
                    "rutas_recoleccion",
                    "ruta_paradas",
                    "mensajes",
                    "notificaciones",
                    "tickets_soporte");

    @Test
    void migraV79DosVecesSinPerderDatos() throws Exception {
        comprobar("db_qhurinet_migration_v79_test", "db/esquema-v79.sql", false);
    }

    @Test
    void migraCierreAnteriorDosVecesSinPerderDatos() throws Exception {
        comprobar("db_qhurinet_migration_cierre_test", "db/esquema-cierre-anterior.sql", true);
    }

    private void comprobar(String base, String script, boolean cierreAnterior) throws Exception {
        StandardEnvironment entorno = new StandardEnvironment();
        entorno.setActiveProfiles("test");
        ConfigDataEnvironmentPostProcessor.applyTo(entorno);
        String url = entorno.getRequiredProperty("spring.datasource.url");
        String servidor = url.substring(0, url.lastIndexOf('/') + 1);
        String usuario = entorno.getRequiredProperty("spring.datasource.username");
        String clave = entorno.getRequiredProperty("spring.datasource.password");
        validarBase(base);
        try {
            eliminar(servidor, usuario, clave, base);
            try (var conexion = DriverManager.getConnection(servidor + "postgres", usuario, clave);
                    var sentencia = conexion.createStatement()) {
                sentencia.execute("CREATE DATABASE " + base);
            }
            try (var conexion = DriverManager.getConnection(servidor + base, usuario, clave);
                    var sentencia = conexion.createStatement()) {
                sentencia.execute(
                        new ClassPathResource(script).getContentAsString(StandardCharsets.UTF_8));
            }
            Map<String, Object> primerEstado = null;
            long primerosUsuarios = 0;
            String primerHash = null;
            for (int arranque = 1; arranque <= 2; arranque++) {
                try (ConfigurableApplicationContext contexto =
                        new SpringApplicationBuilder(QhurinetApplication.class)
                                .profiles("test")
                                .initializers(new BasePruebasInitializer())
                                .run(
                                        "--server.port=0",
                                        "--spring.datasource.url=" + servidor + base,
                                        "--spring.jpa.hibernate.ddl-auto=update",
                                        "--logging.file.name=target/" + base + ".log")) {
                    JdbcTemplate jdbcTemplate = contexto.getBean(JdbcTemplate.class);
                    var tablas =
                            jdbcTemplate.queryForList(
                                    "select table_name from information_schema.tables where"
                                        + " table_schema='public' and table_type='BASE TABLE'",
                                    String.class);
                    assertEquals(13, tablas.size());
                    assertEquals(TABLAS, Set.copyOf(tablas));
                    var columnas =
                            jdbcTemplate.queryForList(
                                    "select column_name from information_schema.columns "
                                            + "where table_name='usuarios'",
                                    String.class);
                    assertTrue(
                            columnas.containsAll(
                                    Set.of(
                                            "proveedor_externo",
                                            "id_externo",
                                            "pago_tarjeta_ultimos4",
                                            "pago_yape_celular",
                                            "pago_plin_celular",
                                            "pago_transferencia_cuenta",
                                            "pago_efectivo",
                                            "materiales")));
                    var actual = jdbcTemplate.queryForMap("select * from usuarios where id=101");
                    assertEquals("legacy@qhurinet.test", actual.get("email"));
                    assertEquals("Usuario anterior", actual.get("nombre_completo"));
                    assertEquals("hash-legacy", actual.get("password_hash"));
                    assertEquals("yape", actual.get("metodo_pago_preferido"));
                    assertEquals(
                            "Material anterior",
                            jdbcTemplate.queryForObject(
                                    "select descripcion from publicaciones_material where id=501",
                                    String.class));
                    assertEquals(
                            "QR_PREVIO",
                            jdbcTemplate.queryForObject(
                                    "select codigo_qr from solicitudes_recoleccion where id=601",
                                    String.class));
                    assertEquals(
                            1L,
                            jdbcTemplate.queryForObject(
                                    "select count(*) from publicaciones_material", Long.class));
                    assertEquals(
                            1L,
                            jdbcTemplate.queryForObject(
                                    "select count(*) from solicitudes_recoleccion", Long.class));
                    assertEquals(
                            "NO",
                            jdbcTemplate.queryForObject(
                                    "select is_nullable from information_schema.columns where"
                                        + " table_name='usuarios' and column_name='password_hash'",
                                    String.class));
                    assertEquals(
                            0L,
                            jdbcTemplate.queryForObject(
                                    "select count(*) from usuarios where password_hash is null",
                                    Long.class));
                    assertEquals(
                            true,
                            jdbcTemplate.queryForObject(
                                    "select pago_efectivo from usuarios where id=102",
                                    Boolean.class));
                    if (cierreAnterior) {
                        assertEquals("google", actual.get("proveedor_externo"));
                        assertEquals("legacy-google", actual.get("id_externo"));
                        assertEquals("987654321", actual.get("pago_yape_celular"));
                        assertEquals("1234", actual.get("pago_tarjeta_ultimos4"));
                        assertEquals("912345678", actual.get("pago_plin_celular"));
                        assertEquals(
                                "12345678901234567890", actual.get("pago_transferencia_cuenta"));
                        assertEquals(true, actual.get("pago_efectivo"));
                        assertEquals("1,2", actual.get("materiales"));
                        assertEquals(
                                "manana",
                                jdbcTemplate.queryForObject(
                                        "select franja_horaria from publicaciones_material where"
                                            + " id=501",
                                        String.class));
                        assertEquals(
                                "Comentario previo",
                                jdbcTemplate.queryForObject(
                                        "select comentario_calificacion from"
                                            + " solicitudes_recoleccion where id=601",
                                        String.class));
                        String hash =
                                jdbcTemplate.queryForObject(
                                        "select password_hash from usuarios where id=103",
                                        String.class);
                        assertNotNull(hash);
                        assertTrue(hash.matches("\\$2[aby]\\$\\d{2}\\$.{53}"));
                        if (primerHash == null) {
                            primerHash = hash;
                        } else {
                            assertEquals(primerHash, hash);
                        }
                        var perfil =
                                contexto.getBean(UsuarioService.class)
                                        .obtenerPerfil("legacy@qhurinet.test");
                        assertEquals(5, perfil.getMetodosPago().size());
                        assertEquals(2, perfil.getMateriales().size());
                        assertTrue(
                                perfil.getMetodosPago().stream()
                                        .map(MetodoPagoUsuarioDTO::getId)
                                        .toList()
                                        .containsAll(Set.of(11L, 12L, 13L, 14L, 15L)));
                    } else {
                        assertNull(actual.get("pago_yape_celular"));
                        assertNull(actual.get("materiales"));
                        assertNull(actual.get("proveedor_externo"));
                    }
                    long cantidad =
                            jdbcTemplate.queryForObject(
                                    "select count(*) from usuarios", Long.class);
                    if (primerEstado == null) {
                        primerEstado = actual;
                        primerosUsuarios = cantidad;
                    } else {
                        assertEquals(primerEstado, actual);
                        assertEquals(primerosUsuarios, cantidad);
                    }
                    Files.writeString(
                            Path.of("target/" + base + "-resultado.txt"),
                            "Tablas: "
                                    + tablas.size()
                                    + "; arranques: "
                                    + arranque
                                    + "; datos conservados: correcto\n");
                }
            }
        } finally {
            eliminar(servidor, usuario, clave, base);
        }
        try (var conexion = DriverManager.getConnection(servidor + "postgres", usuario, clave);
                var sentencia =
                        conexion.prepareStatement("select 1 from pg_database where datname=?")) {
            sentencia.setString(1, base);
            try (var resultado = sentencia.executeQuery()) {
                assertFalse(resultado.next());
            }
        }
    }

    private void eliminar(String servidor, String usuario, String clave, String base)
            throws Exception {
        validarBase(base);
        try (var conexion = DriverManager.getConnection(servidor + "postgres", usuario, clave);
                var sentencia = conexion.createStatement()) {
            sentencia.execute("DROP DATABASE IF EXISTS " + base + " WITH (FORCE)");
        }
    }

    private void validarBase(String base) {
        if (!BASES.contains(base)) {
            throw new IllegalArgumentException("Base no autorizada para pruebas de migración");
        }
    }
}
