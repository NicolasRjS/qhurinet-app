package com.upc.qhurinet;

import com.upc.qhurinet.security.util.JwtUtil;
import com.upc.qhurinet.repositories.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import tools.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpServer;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = BasePruebasInitializer.class)
class QhurinetApplicationTests {
    @org.springframework.beans.factory.annotation.Value("${local.server.port}") int port;
    @Autowired JwtUtil jwt;
    @Autowired UsuarioRepositorio usuarios;
    @Autowired SolicitudRecoleccionRepositorio solicitudes;
    @Autowired PublicacionMaterialRepositorio publicaciones;
    @Autowired NotificacionRepositorio notificaciones;
    @Autowired com.upc.qhurinet.services.ReportePdfService pdfService;
    @Autowired com.upc.qhurinet.services.RutaRecoleccionService rutaService;
    @Autowired PuntoReciclajeRepositorio puntosRepositorio;
    @Autowired com.upc.qhurinet.serviceimpl.CorreoServiceImpl correoService;
    @Autowired com.upc.qhurinet.serviceimpl.SeguimientoServiceImpl seguimientoService;
    static final JsonMapper JSON = new JsonMapper();
    static final HttpClient HTTP = HttpClient.newHttpClient();
    static HttpServer externos;
    static RSAKey clave;
    static volatile boolean fallaRuta;
    static volatile boolean fallaIa;
    static volatile boolean categoriaAjena;
    static volatile boolean tokenFacebookInvalido;
    static {
        try {
            clave = new RSAKeyGenerator(2048).keyID("pruebas").generate();
            externos = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            externos.createContext("/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                String body;
                int status = 200;
                if (path.equals("/jwks")) body = new JWKSet(clave.toPublicJWK()).toString();
                else if (path.contains("debug_token")) body = JSON.writeValueAsString(Map.of("data", Map.of("is_valid", !tokenFacebookInvalido,
                        "app_id", "facebook-test", "user_id", "fb-1", "expires_at", Instant.now().plusSeconds(3600).getEpochSecond())));
                else if (path.endsWith("/me")) body = JSON.writeValueAsString(Map.of("id", "fb-1", "name", "Facebook Prueba", "email", "fb@qhurinet.test"));
                else if (path.contains("generateContent")) {
                    status = fallaIa ? 503 : 200;
                    body = JSON.writeValueAsString(Map.of("candidates", List.of(Map.of("content", Map.of("parts", List.of(Map.of("text",
                            JSON.writeValueAsString(Map.of("categoria", categoriaAjena ? "no existe" : "PET", "confianza", 0.95)))))))));
                } else if (path.contains("/trip/")) {
                    int cantidad = path.substring(path.lastIndexOf('/') + 1).split(";").length;
                    List<Map<String,Object>> puntos = new ArrayList<>();
                    for (int i=0;i<cantidad;i++) puntos.add(Map.of("waypoint_index", i, "trips_index", 0));
                    body = JSON.writeValueAsString(Map.of("code", "Ok", "waypoints", puntos, "trips", List.of(Map.of("distance", 2500, "duration", 600))));
                    status = fallaRuta ? 503 : 200;
                } else body = JSON.writeValueAsString(Map.of("code", "Ok", "routes", List.of(Map.of("duration", 300))));
                exchange.getRequestBody().readAllBytes();
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
            });
            externos.start();
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    @DynamicPropertySource
    static void proveedores(DynamicPropertyRegistry r) {
        String url = "http://localhost:" + externos.getAddress().getPort();
        r.add("rutas.osrm.url", () -> url); r.add("ia.url", () -> url);
        r.add("ia.api-key", () -> "credencial-de-prueba"); r.add("ia.modelo", () -> "modelo-de-prueba");
        r.add("oauth.google.client-id", () -> "google-test"); r.add("oauth.google.jwks", () -> url + "/jwks");
        r.add("oauth.facebook.url", () -> url); r.add("oauth.facebook.app-id", () -> "facebook-test");
        r.add("oauth.facebook.app-secret", () -> "secreto-prueba");
    }
    static final java.util.Queue<Map<String,Object>> comprobaciones = new java.util.concurrent.ConcurrentLinkedQueue<>();
    @AfterAll static void cerrarProveedores() throws Exception {
        externos.stop(0);
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/backend-http-checks.json"), JSON.writerWithDefaultPrettyPrinter().writeValueAsString(comprobaciones));
    }

    HttpResponse<byte[]> request(String method, String path, String token, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + path)).timeout(Duration.ofSeconds(30));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.header("Content-Type", "application/json");
        return HTTP.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body))).build(), HttpResponse.BodyHandlers.ofByteArray());
    }
    Map<String,Object> object(HttpResponse<byte[]> response, int expected) {
        status(expected, response);
        return JSON.readValue(response.body(), Map.class);
    }
    void status(int expected, HttpResponse<byte[]> response) {
        assertEquals(expected, response.statusCode(), () -> new String(response.body(), StandardCharsets.UTF_8));
        comprobaciones.add(Map.of("metodo", response.request().method(), "ruta", response.uri().getPath(), "esperado", expected, "resultado", "correcto"));
    }
    List<Map<String,Object>> list(HttpResponse<byte[]> response) {
        status(200,response); return JSON.readValue(response.body(), List.class);
    }
    String user(String email, int rol) throws Exception {
        object(request("POST", "/auth/register", null, Map.of("nombreCompleto", "Usuario de prueba " + rol, "email", email,
                "password", "Prueba2026!", "telefono", "987654321", "rolId", rol)), 201);
        status(200,request("POST", "/auth/verify-email", null, Map.of("token", jwt.generarTokenVerificacion(email))));
        return (String) object(request("POST", "/auth/login", null, Map.of("email", email, "password", "Prueba2026!")),200).get("token");
    }
    Map<String,Object> publicacion(int categoria) {
        return new HashMap<>(Map.of("categoriaMaterialId", categoria, "cantidad", 5, "direccion", "Av. Pruebas 100",
                "distrito", "Lima", "latitud", -12.05, "longitud", -77.04,
                "fechaDisponibilidad", LocalDate.now().plusDays(1).toString(), "franjaHoraria", "manana"));
    }
    long nueva(String token, int categoria) throws Exception {
        return ((Number)object(request("POST", "/publications", token, publicacion(categoria)),201).get("id")).longValue();
    }
    HttpResponse<byte[]> multipart(String path, String token, String campo, String nombre, byte[] bytes, Map<String,String> campos) throws Exception {
        String boundary = "qhuri" + UUID.randomUUID(); var out = new ByteArrayOutputStream();
        for (var entry : campos.entrySet()) out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+entry.getKey()+"\"\r\n\r\n"+entry.getValue()+"\r\n").getBytes(StandardCharsets.UTF_8));
        if (bytes != null) {
            out.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+campo+"\"; filename=\""+nombre+"\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(bytes); out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        out.write(("--"+boundary+"--\r\n").getBytes(StandardCharsets.UTF_8));
        var req = HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1"+path)).timeout(Duration.ofSeconds(30)).header("Authorization","Bearer "+token)
                .header("Content-Type","multipart/form-data; boundary="+boundary).POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray())).build();
        return HTTP.send(req,HttpResponse.BodyHandlers.ofByteArray());
    }
    byte[] png() throws Exception {
        var out = new ByteArrayOutputStream(); javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",out); return out.toByteArray();
    }

    @Test void flujoCompletoContratosPermisosYConcurrencia() throws Exception {
        String g=user("generador@qhurinet.test",1), r=user("recolector@qhurinet.test",2), ajeno=user("ajeno@qhurinet.test",2);
        status(401,request("GET","/users/me",null,null));
        status(401,request("GET","/users/me",jwt.generarTokenVerificacion("generador@qhurinet.test"),null));
        status(401,request("POST","/auth/login",null,Map.of("email","generador@qhurinet.test","password","incorrecta")));
        status(400,request("POST","/auth/register",null,Map.of()));
        status(409,request("POST","/auth/register",null,Map.of("nombreCompleto","Otro","email","generador@qhurinet.test","password","Prueba2026!","telefono","987654321","rolId",1)));
        status(409,request("POST","/auth/verify-email",null,Map.of("token",jwt.generarTokenVerificacion("generador@qhurinet.test"))));
        var categorias=list(request("GET","/material-categories",g,null));
        int cat=((Number)categorias.getFirst().get("id")).intValue();
        long gid=((Number)object(request("GET","/users/me",g,null),200).get("id")).longValue();
        long rid=((Number)object(request("GET","/users/me",r,null),200).get("id")).longValue();
        var perfil=object(request("PUT","/users/me",g,Map.of("nombreCompleto","Ana Pérez","telefono","987654321","descripcion","Materiales limpios","materialesIds",List.of(cat))),200);
        assertEquals(1,((List<?>)perfil.get("materiales")).size());
        status(400,request("PUT","/users/me",g,Map.of("nombreCompleto","Ana","telefono","12")));
        status(200,request("PATCH","/users/me/availability",r,Map.of("enLinea",false)));
        var pago=object(request("PATCH","/users/me/payment-method",g,Map.of("metodos",List.of(
                Map.of("tipo","yape","dato","987654321","predeterminado",true),Map.of("tipo","efectivo","predeterminado",false)))),200);
        assertEquals("yape",pago.get("metodoPagoPreferido"));
        status(400,request("PATCH","/users/me/payment-method",g,Map.of("metodos",List.of(Map.of("tipo","tarjeta","dato","1234567890123456","predeterminado",true)))));
        status(200,multipart("/users/me/avatar",g,"archivo","foto.png",png(),Map.of()));
        var documento=object(multipart("/users/me/documents",g,"archivo","dni.png",png(),Map.of("tipo","dni")),201);
        String docUrl=(String)documento.get("urlArchivo");
        assertNotNull(docUrl);
        status(200,request("GET",docUrl.substring(7),g,null));
        status(403,request("GET",docUrl.substring(7),r,null));
        assertEquals(1,list(request("GET","/users/me/documents",g,null)).size());
        assertEquals(true,object(request("GET","/users/"+gid+"/reputation",r,null),200).get("verificado"));
        assertEquals(true,object(request("GET","/users/me",g,null),200).get("verificado"));
        status(400,multipart("/users/me/avatar",g,"archivo","falsa.png","texto".getBytes(),Map.of()));
        status(400,request("POST","/publications",g,Map.of()));
        status(403,request("POST","/publications",r,publicacion(cat)));
        long pub=nueva(g,cat);
        var anuncioFlujo=publicacion(cat);anuncioFlujo.put("direccion","Flujo integral 100");
        status(200,request("PUT","/publications/"+pub,g,anuncioFlujo));
        status(403,request("PUT","/publications/"+pub,ajeno,publicacion(cat)));
        String foto=(String)object(multipart("/publications/"+pub+"/photo",g,"archivo","foto.png",png(),Map.of()),200).get("fotoUrl");
        status(200,request("GET",foto.substring(7),r,null));
        status(204,request("DELETE","/publications/"+pub+"/photo",g,null));
        status(404,request("GET",foto.substring(7),r,null));
        var mapa=list(request("GET","/publications?material="+cat+"&q=integral",r,null));
        assertEquals(1,mapa.size());
        assertEquals(pub,((Number)mapa.getFirst().get("id")).longValue());
        status(200,request("GET","/publications/"+pub,r,null));
        var pubPago=publicacion(cat);pubPago.put("montoPago",12.50);pubPago.put("metodoPago","yape");
        status(200,request("PUT","/publications/"+pub,g,pubPago));
        long sol=((Number)object(request("POST","/collection-requests",r,Map.of("publicacionId",pub)),201).get("id")).longValue();
        status(409,request("POST","/collection-requests",ajeno,Map.of("publicacionId",pub)));
        status(409,request("PUT","/publications/"+pub,g,publicacion(cat)));
        var misPublicaciones=list(request("GET","/publications/mine?estado=activos",g,null));
        assertEquals("kg",misPublicaciones.getFirst().get("unidadMedida"));
        assertEquals(sol,((Number)misPublicaciones.getFirst().get("solicitudId")).longValue());
        var misSolicitudes=list(request("GET","/collection-requests?estado=activos",r,null));
        assertEquals("kg",misSolicitudes.getFirst().get("unidadMedida"));
        assertEquals(pub,((Number)misSolicitudes.getFirst().get("publicacionId")).longValue());
        String path="/collection-requests/"+sol;
        status(403,request("GET",path,ajeno,null));
        status(200,request("GET",path,g,null));
        status(409,request("GET",path+"/qr",g,null));
        Map<String,Object> horario=Map.of("fechaCoordinada",LocalDate.now().plusDays(2)+"T10:00:00","franjaHoraria","manana");
        status(200,request("PATCH",path+"/coordinate",g,horario));
        String qr=(String)object(request("GET",path+"/qr",g,null),200).get("codigoQr");
        status(200,request("PATCH",path+"/reschedule",r,horario));
        assertEquals(qr,object(request("GET",path+"/qr",g,null),200).get("codigoQr"));
        status(403,request("PATCH",path+"/reschedule",ajeno,horario));
        status(200,request("PATCH",path+"/priority",g,Map.of("prioritaria",true)));
        status(201,request("POST",path+"/messages",g,Map.of("contenido","Llego a las 10")));
        assertNull(list(request("GET",path+"/messages",g,null)).getFirst().get("fechaLectura"));
        assertNotNull(list(request("GET",path+"/messages",r,null)).getFirst().get("fechaLectura"));
        status(403,request("GET",path+"/messages",ajeno,null));
        status(400,request("POST",path+"/messages",r,Map.of("contenido","")));
        status(409,request("GET",path+"/tracking",g,null));
        status(403,request("PATCH",path+"/start",g,null));
        status(200,request("PATCH",path+"/start",r,null));
        status(409,request("GET",path+"/tracking",g,null));
        status(204,request("PUT",path+"/location",r,Map.of("latitud",-12.1,"longitud",-77.1)));
        assertEquals(5,object(request("GET",path+"/tracking",g,null),200).get("minutosEstimados"));
        Object caducidad=org.springframework.test.util.ReflectionTestUtils.getField(seguimientoService,"caducidad");
        try {
            org.springframework.test.util.ReflectionTestUtils.setField(seguimientoService,"caducidad",0L);
            status(409,request("GET",path+"/tracking",g,null));
        } finally { org.springframework.test.util.ReflectionTestUtils.setField(seguimientoService,"caducidad",caducidad); }
        status(403,request("GET",path+"/tracking",ajeno,null));
        status(403,request("PUT",path+"/location",ajeno,Map.of("latitud",0,"longitud",0)));
        status(400,request("POST",path+"/confirm",r,Map.of()));
        status(409,request("POST",path+"/confirm",r,Map.of("codigo","incorrecto")));
        status(409,request("POST",path+"/validate-qr",r,Map.of("codigo","incorrecto")));
        status(200,request("POST",path+"/validate-qr",r,Map.of("codigo",qr)));
        var fin=object(request("POST",path+"/confirm",r,Map.of("codigo",qr)),200);
        assertEquals("ejecutada",fin.get("estado")); assertEquals(12.5,((Number)fin.get("montoPago")).doubleValue());
        long antes=notificaciones.count();
        status(409,request("POST",path+"/confirm",r,Map.of("codigo",qr)));
        assertEquals(antes,notificaciones.count());
        status(409,request("GET",path+"/tracking",g,null));
        status(409,request("PATCH",path+"/cancel",g,Map.of("motivo","No")));
        status(400,request("POST",path+"/rating",g,Map.of("calificacion",6)));
        status(200,request("POST",path+"/rating",g,Map.of("calificacion",5,"comentario","Buen servicio")));
        status(409,request("POST",path+"/rating",g,Map.of("calificacion",4)));
        assertEquals(5.0,((Number)object(request("GET","/users/"+rid+"/reputation",g,null),200).get("calificacionPromedio")).doubleValue());
        long pub2=nueva(g,cat);
        long cancelada=((Number)object(request("POST","/collection-requests",r,Map.of("publicacionId",pub2)),201).get("id")).longValue();
        status(200,request("PATCH","/collection-requests/"+cancelada+"/cancel",r,
                Map.of("motivo","Imprevisto, contactar al +51 987 654 321 o contacto@qhurinet.test")));
        assertEquals("disponible",object(request("GET","/publications/"+pub2,r,null),200).get("estado"));
        status(200,request("PATCH","/publications/"+pub2+"/cancel",g,Map.of("motivo","Ya no disponible")));
        assertEquals(2,list(request("GET","/history",g,null)).size());
        assertEquals(1,list(request("GET","/history?tipo=ejecutadas",g,null)).size());
        assertNotNull(list(request("GET","/history",g,null)).getFirst().get("solicitudId"));
        assertTrue(list(request("GET","/history",ajeno,null)).isEmpty());
        status(200,request("GET","/reports/summary",g,null)); status(200,request("GET","/reports/materials",g,null));
        var csv=request("GET","/reports/export?formato=csv&tipo=ejecutadas",g,null); status(200,csv);
        assertTrue(new String(csv.body(),StandardCharsets.UTF_8).contains("ejecutada"));
        assertFalse(new String(csv.body(),StandardCharsets.UTF_8).contains("cancelada"));
        var pdf=request("GET","/reports/export?formato=pdf",g,null); status(200,pdf);
        try(var doc=org.apache.pdfbox.Loader.loadPDF(pdf.body())) {
            String text=new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
            assertTrue(text.contains("Ana Pérez")); assertTrue(text.contains("Periodo")); assertFalse(text.contains("@qhurinet.test"));
        }
        status(400,request("GET","/reports/export?formato=pdf",ajeno,null));
        var notif=list(request("GET","/notifications",g,null));
        var avisoCancelacion=notif.stream().map(n -> n.get("mensaje").toString())
                .filter(m -> m.contains("Imprevisto")).findFirst().orElseThrow();
        assertFalse(avisoCancelacion.contains("987"));
        assertFalse(avisoCancelacion.contains("contacto@qhurinet.test"));
        assertTrue(avisoCancelacion.contains("[correo omitido]"));
        status(403,request("PATCH","/notifications/"+notif.getFirst().get("id")+"/read",ajeno,null));
        status(200,request("PATCH","/notifications/"+notif.getFirst().get("id")+"/read",g,null));
        status(204,request("PATCH","/notifications/read-all",g,null));
        assertEquals(0,((Number)object(request("GET","/notifications/unread-count",g,null),200).get("cantidad")).intValue());
        var ticket=object(request("POST","/support-tickets",g,Map.of("categoria","otro","asunto","Consulta","descripcion","Prueba","solicitudId",sol)),201);
        status(200,multipart("/support-tickets/"+ticket.get("id")+"/evidence",g,"archivo","evidencia.png",png(),Map.of()));
        status(200,request("GET","/support-tickets?estado=abierto",g,null));
        status(403,request("POST","/support-tickets",ajeno,Map.of("categoria","otro","asunto","Consulta","descripcion","Prueba","solicitudId",sol)));
        status(200,request("GET","/faqs",r,null));status(200,request("GET","/support/contact",r,null));
        var puntos=list(request("GET","/recycling-points",r,null));assertEquals(8,puntos.size());
        status(200,request("GET","/recycling-points/"+puntos.getFirst().get("id"),r,null));
        Map<String,Object> ruta=Map.of("latitudOrigen",-12.1,"longitudOrigen",-77.1,"puntosIds",List.of(puntos.get(0).get("id"),puntos.get(1).get("id")));
        var optimizada=object(request("POST","/routes/optimize",r,ruta),200);assertEquals(2.5,((Number)optimizada.get("distanciaTotalKm")).doubleValue());
        fallaRuta=true;status(503,request("POST","/routes/optimize",r,ruta));fallaRuta=false;
        long rutaId=((Number)object(request("POST","/routes",r,Map.of("nombre","Ruta de prueba","paradas",optimizada.get("paradas"),"distanciaTotalKm",2.5,"tiempoEstimadoMin",10)),201).get("id")).longValue();
        status(200,request("GET","/routes",r,null));status(200,request("GET","/routes/"+rutaId,r,null));
        status(403,request("GET","/routes/"+rutaId,ajeno,null));status(204,request("DELETE","/routes/"+rutaId,r,null));
        status(404,request("GET","/routes/"+rutaId,r,null));
        assertNotNull(object(multipart("/materials/classify",g,null,null,null,Map.of("descripcion","Botella PET")),200).get("categoriaMaterialId"));
        categoriaAjena=true; assertNull(object(multipart("/materials/classify",g,null,null,null,Map.of("descripcion","Otro")),200).get("categoriaMaterialId"));categoriaAjena=false;
        fallaIa=true;status(503,multipart("/materials/classify",g,null,null,null,Map.of("descripcion","PET")));fallaIa=false;
        // Dos recolectores compiten por el mismo anuncio: solo uno obtiene la reserva.
        long concurrente=nueva(g,cat);
        try(var executor=Executors.newFixedThreadPool(2)) {
            var uno=executor.submit(()->request("POST","/collection-requests",r,Map.of("publicacionId",concurrente)).statusCode());
            var dos=executor.submit(()->request("POST","/collection-requests",ajeno,Map.of("publicacionId",concurrente)).statusCode());
            assertEquals(Set.of(201,409),Set.of(uno.get(),dos.get()));
        }
    }

    String googleToken(String audience, String subject, String email, Instant expires) throws Exception {
        SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("pruebas").build(),
                new JWTClaimsSet.Builder().issuer("https://accounts.google.com").subject(subject).audience(audience)
                        .expirationTime(Date.from(expires)).issueTime(new Date()).claim("email",email).claim("email_verified",true).claim("name","OAuth Prueba").build());
        token.sign(new RSASSASigner(clave));return token.serialize();
    }
    @Test void oauthConFirmaRealYProveedorControlado() throws Exception {
        String token=googleToken("google-test","google-1","oauth-test@gmail.com",Instant.now().plusSeconds(3600));
        var auth=object(request("POST","/auth/oauth/google",null,Map.of("idToken",token,"rolId",1,"telefono","987654321")),200);
        status(200,request("GET","/users/me",(String)auth.get("token"),null));
        status(200,request("POST","/auth/oauth/google",null,Map.of("idToken",token)));
        status(401,request("POST","/auth/oauth/google",null,Map.of("idToken",googleToken("otra-app","google-1","oauth-test@gmail.com",Instant.now().plusSeconds(3600)))));
        status(401,request("POST","/auth/oauth/google",null,Map.of("idToken",googleToken("google-test","google-1","oauth-test@gmail.com",Instant.now().minusSeconds(600)))));
        status(401,request("POST","/auth/oauth/google",null,Map.of("idToken","falso")));
        var facebook=object(request("POST","/auth/oauth/facebook",null,Map.of("accessToken","token-prueba","rolId",2,"telefono","987654321")),200);
        assertEquals("pendiente_verificacion",((Map<?,?>)facebook.get("usuario")).get("estado"));
        status(400,request("POST","/auth/oauth/otro",null,Map.of()));
        status(400,request("POST","/auth/oauth/google",null,Map.of("idToken",googleToken("google-test","admin","admin-test@gmail.com",Instant.now().plusSeconds(3600)),"rolId",3,"telefono","987654321")));
    }
    @Test void limitesDeEntradaYDatosPersistidos() throws Exception {
        String g=user("limitesg@qhurinet.test",1), r=user("limitesr@qhurinet.test",2);
        int cat=((Number)list(request("GET","/material-categories",g,null)).getFirst().get("id")).intValue();
        for (Map<String,Object> invalida : List.of(
                Map.<String,Object>of("cantidad",0), Map.<String,Object>of("cantidad",1000000),
                Map.<String,Object>of("latitud",91), Map.<String,Object>of("longitud",-181),
                Map.<String,Object>of("fechaDisponibilidad",LocalDate.now().minusDays(1).toString()),
                Map.<String,Object>of("franjaHoraria","inexistente"),Map.<String,Object>of("descripcion","<b>No</b>"),
                Map.<String,Object>of("descripcion","a".repeat(201)),Map.<String,Object>of("montoPago",-1))) {
            var datos=publicacion(cat);datos.putAll(invalida);status(400,request("POST","/publications",g,datos));
        }
        var inexistente=publicacion(cat);inexistente.put("categoriaMaterialId",999999);
        status(404,request("POST","/publications",g,inexistente));
        status(400,request("GET","/publications?min_kg=no-numero",r,null));
        status(400,request("GET","/reports/summary?desde=2026-12-31&hasta=2026-01-01",g,null));
        status(400,request("GET","/history?tipo=invalido",g,null));
        status(400,request("GET","/recycling-points?type=invalido",r,null));
        status(404,request("GET","/collection-requests/999999",g,null));
        status(404,request("GET","/users/999999/reputation",g,null));
        status(400,multipart("/users/me/documents",g,"archivo","dni.png",png(),Map.of()));
        status(400,multipart("/users/me/avatar",g,"archivo","grande.png",new byte[5*1024*1024+1],Map.of()));
        status(400,multipart("/materials/classify",g,null,null,null,Map.of()));
        status(200,multipart("/materials/classify",g,"foto","foto.png",png(),Map.of()));
        var puntos=list(request("GET","/recycling-points",r,null));long puntoId=((Number)puntos.getFirst().get("id")).longValue();
        assertNull(puntos.getFirst().get("calificacionPromedio"));
        var punto=puntosRepositorio.findById(puntoId).orElseThrow();punto.setActivo(false);puntosRepositorio.save(punto);
        assertFalse(list(request("GET","/recycling-points",r,null)).stream().anyMatch(v -> ((Number)v.get("id")).longValue()==puntoId));
        punto.setActivo(true);puntosRepositorio.save(punto);
        status(400,request("POST","/routes/optimize",r,Map.of("latitudOrigen",-12,"longitudOrigen",-77,"puntosIds",List.of(puntoId,puntoId))));
        status(400,request("POST","/routes/optimize",r,Map.of("latitudOrigen",-12,"longitudOrigen",-77,"puntosIds",List.of(puntoId))));
        for (Object distancia : List.of(-1,10000,1.234)) {
            status(400,request("POST","/routes",r,Map.of("nombre","Ruta inválida","distanciaTotalKm",distancia,
                    "tiempoEstimadoMin",10,"paradas",List.of(Map.of("puntoReciclajeId",puntoId,"orden",1)))));
        }
        var pagos=object(request("PATCH","/users/me/payment-method",g,Map.of("metodos",List.of(
                Map.of("tipo","plin","dato","987654321","predeterminado",true),Map.of("tipo","efectivo","predeterminado",false)))),200);
        status(200,request("PATCH","/users/me/payment-method",g,Map.of("metodoPagoPreferido","efectivo")));
        var perfil=object(request("GET","/users/me",g,null),200);
        assertEquals(1,((List<Map<String,Object>>)perfil.get("metodosPago")).stream().filter(m -> Boolean.TRUE.equals(m.get("predeterminado"))).count());
        var ajeno=((List<Map<String,Object>>)pagos.get("metodos")).getFirst();
        status(403,request("PATCH","/users/me/payment-method",r,Map.of("metodos",List.of(ajeno))));
        long pub=nueva(g,cat);long id=((Number)object(request("POST","/collection-requests",r,Map.of("publicacionId",pub)),201).get("id")).longValue();
        String path="/collection-requests/"+id;
        Map<String,Object> horario=Map.of("fechaCoordinada",LocalDate.now().plusDays(1)+"T10:00:00","franjaHoraria","manana");
        status(200,request("PATCH",path+"/coordinate",g,horario));
        String qr=(String)object(request("GET",path+"/qr",g,null),200).get("codigoQr");
        try(var pool=Executors.newFixedThreadPool(2)) {
            var a=pool.submit(()->request("POST",path+"/confirm",r,Map.of("codigo",qr)).statusCode());
            var b=pool.submit(()->request("POST",path+"/confirm",r,Map.of("codigo",qr)).statusCode());
            assertEquals(Set.of(200,409),new HashSet<>(List.of(a.get(),b.get())));
            var c=pool.submit(()->request("POST",path+"/rating",g,Map.of("calificacion",5)).statusCode());
            var d=pool.submit(()->request("POST",path+"/rating",g,Map.of("calificacion",4)).statusCode());
            assertEquals(Set.of(200,409),new HashSet<>(List.of(c.get(),d.get())));
        }
        long pubCancel=nueva(g,cat);long cancelId=((Number)object(request("POST","/collection-requests",r,Map.of("publicacionId",pubCancel)),201).get("id")).longValue();
        String cancelPath="/collection-requests/"+cancelId;
        status(200,request("PATCH",cancelPath+"/coordinate",g,horario));
        String cancelQr=(String)object(request("GET",cancelPath+"/qr",g,null),200).get("codigoQr");
        try(var pool=Executors.newFixedThreadPool(2)) {
            var a=pool.submit(()->request("POST",cancelPath+"/confirm",r,Map.of("codigo",cancelQr)).statusCode());
            var b=pool.submit(()->request("PATCH",cancelPath+"/cancel",g,Map.of("motivo","Cambio" )).statusCode());
            assertEquals(Set.of(200,409),new HashSet<>(List.of(a.get(),b.get())));
        }
        String finalSolicitud=solicitudes.findById(cancelId).orElseThrow().getEstado();
        assertEquals("ejecutada".equals(finalSolicitud)?"recolectado":"disponible",publicaciones.findById(pubCancel).orElseThrow().getEstado());
        tokenFacebookInvalido=true;
        try { status(401,request("POST","/auth/oauth/facebook",null,Map.of("accessToken","invalido"))); }
        finally { tokenFacebookInvalido=false; }
    }

    @Test void reportesConUnidadesDiferentesYCaracteresCsv() throws Exception {
        String g=user("reporteg@qhurinet.test",1), r=user("reporter@qhurinet.test",2);
        status(200,request("PUT","/users/me",r,Map.of("nombreCompleto","=SUM(1,2) \"Prueba\"","telefono","987654321")));
        int cat=((Number)list(request("GET","/material-categories",g,null)).getFirst().get("id")).intValue();
        status(400,request("GET","/publications?min_kg=-1",r,null));
        for(String unidad : List.of("g","t","unidad")) {
            var datos=publicacion(cat);datos.put("unidadMedida",unidad);datos.put("direccion","Reporte unidades 100");
            long pub=((Number)object(request("POST","/publications",g,datos),201).get("id")).longValue();
            var filtradas=list(request("GET","/publications?min_kg=1&q=Reporte",r,null));
            assertEquals("t".equals(unidad),filtradas.stream().anyMatch(p -> ((Number)p.get("id")).longValue()==pub));
            long id=((Number)object(request("POST","/collection-requests",r,Map.of("publicacionId",pub)),201).get("id")).longValue();
            String path="/collection-requests/"+id;
            status(200,request("PATCH",path+"/coordinate",g,Map.of("fechaCoordinada",LocalDate.now().plusDays(1)+"T10:00:00","franjaHoraria","manana")));
            String qr=(String)object(request("GET",path+"/qr",g,null),200).get("codigoQr");
            status(200,request("POST",path+"/confirm",r,Map.of("codigo",qr)));
        }
        String periodo="?desde="+LocalDate.now()+"&hasta="+LocalDate.now();
        var resumen=object(request("GET","/reports/summary"+periodo,g,null),200);
        assertEquals(5000.005,((Number)resumen.get("kgTotales")).doubleValue(),0.000001);
        assertEquals(3,((Number)resumen.get("totalRecojos")).intValue());
        var materiales=list(request("GET","/reports/materials"+periodo,g,null));
        assertEquals(3,materiales.size());
        assertNull(materiales.stream().filter(m -> "unidad".equals(m.get("unidadMedida"))).findFirst().orElseThrow().get("kilos"));
        var csv=request("GET","/reports/export"+periodo+"&formato=csv&tipo=ejecutadas",g,null);status(200,csv);
        assertTrue(csv.headers().firstValue("Content-Type").orElseThrow().startsWith("text/csv"));
        assertTrue(csv.headers().firstValue("Content-Disposition").orElseThrow().contains("historial.csv"));
        assertTrue(new String(csv.body(),StandardCharsets.UTF_8).contains("\"'=SUM(1,2) \"\"Prueba\"\"\""));
        assertTrue(list(request("GET","/history?desde="+LocalDate.now().plusDays(1),g,null)).isEmpty());
    }

    @Test void vinculacionOAuthExigeClaveYCorreoFallidoNoPersiste() throws Exception {
        user("vinculo@qhurinet.test",1);
        String token=googleToken("google-test","vinculo","vinculo@qhurinet.test",Instant.now().plusSeconds(3600));
        long antes=usuarios.count();
        status(401,request("POST","/auth/oauth/google",null,Map.of("idToken",token)));
        status(401,request("POST","/auth/oauth/google",null,Map.of("idToken",token,"passwordActual","incorrecta")));
        status(200,request("POST","/auth/oauth/google",null,Map.of("idToken",token,"passwordActual","Prueba2026!")));
        status(200,request("POST","/auth/oauth/google",null,Map.of("idToken",token)));
        assertEquals(antes,usuarios.count());
        Object modo=org.springframework.test.util.ReflectionTestUtils.getField(correoService,"modo");
        Object desde=org.springframework.test.util.ReflectionTestUtils.getField(correoService,"desde");
        try {
            org.springframework.test.util.ReflectionTestUtils.setField(correoService,"modo","smtp");
            org.springframework.test.util.ReflectionTestUtils.setField(correoService,"desde","");
            status(503,request("POST","/auth/register",null,Map.of("nombreCompleto","Fallo correo","email","rollback@qhurinet.test",
                    "password","Prueba2026!","telefono","987654321","rolId",1)));
            assertFalse(usuarios.existsByEmail("rollback@qhurinet.test"));
            assertEquals(antes,usuarios.count());
        } finally {
            org.springframework.test.util.ReflectionTestUtils.setField(correoService,"modo",modo);
            org.springframework.test.util.ReflectionTestUtils.setField(correoService,"desde",desde);
        }
    }

    @Test void pdfLargoMantieneContenidoYPaginacion() throws Exception {
        List<com.upc.qhurinet.dtos.HistorialDTO> registros=new ArrayList<>();
        for(int i=0;i<40;i++) registros.add(new com.upc.qhurinet.dtos.HistorialDTO(LocalDateTime.now(),"Cartón",new java.math.BigDecimal("12.50"),
                "Contraparte "+i+" "+"Nombre largo ".repeat(15),null,"ejecutada",null,(long)i,"kg"));
        byte[] bytes=pdfService.generar("María José Ñañez","2026-10-01 - 2026-10-31",registros);
        try(var doc=org.apache.pdfbox.Loader.loadPDF(bytes)) {
            assertTrue(doc.getNumberOfPages()>1);
            String text=new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
            assertTrue(text.contains("María José Ñañez"));assertTrue(text.contains("Contraparte 39"));
            java.nio.file.Files.write(java.nio.file.Path.of("target/reporte-qa.pdf"),bytes);
            javax.imageio.ImageIO.write(new org.apache.pdfbox.rendering.PDFRenderer(doc).renderImageWithDPI(0,110),"png",new File("target/reporte-qa.png"));
        }
    }

    @Test void correoSmtpEnServidorLocal() throws Exception {
        try(var servidor=new java.net.ServerSocket(0);var pool=Executors.newSingleThreadExecutor()) {
            var recibido=pool.submit(()->{
                try(var socket=servidor.accept();var reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
                    var writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8))) {
                    socket.setSoTimeout(10000);writer.write("220 localhost SMTP\r\n");writer.flush();
                    boolean data=false;StringBuilder texto=new StringBuilder();String linea;
                    while((linea=reader.readLine())!=null) {
                        if(data && !linea.equals(".")) { texto.append(linea).append("\n");continue; }
                        if(linea.equals(".")) data=false;
                        if(linea.equals("DATA")) {data=true;writer.write("354 Continue\r\n");}
                        else if(linea.equals("QUIT")) {writer.write("221 Bye\r\n");writer.flush();break;}
                        else writer.write("250 OK\r\n");
                        writer.flush();
                    }
                    return texto.toString();
                }
            });
            var sender=new org.springframework.mail.javamail.JavaMailSenderImpl();sender.setHost("localhost");sender.setPort(servidor.getLocalPort());
            var factory=new org.springframework.beans.factory.support.DefaultListableBeanFactory();factory.registerSingleton("mailSender",sender);
            var correo=new com.upc.qhurinet.serviceimpl.CorreoServiceImpl();
            org.springframework.test.util.ReflectionTestUtils.setField(correo,"remitente",factory.getBeanProvider(org.springframework.mail.javamail.JavaMailSender.class));
            org.springframework.test.util.ReflectionTestUtils.setField(correo,"modo","smtp");
            org.springframework.test.util.ReflectionTestUtils.setField(correo,"desde","prueba@qhurinet.test");
            org.springframework.test.util.ReflectionTestUtils.setField(correo,"url","http://localhost/verify-email");
            correo.enviarVerificacion("destino@qhurinet.test","token-prueba");
            assertTrue(recibido.get(15,TimeUnit.SECONDS).contains("token-prueba"));
        }
    }

    @Test
    @org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named="RUN_EXTERNAL_TESTS",matches="true")
    void osrmReal() throws Exception {
        String recolector=user("osrm-real@qhurinet.test",2);
        var ids=list(request("GET","/recycling-points",recolector,null)).stream().limit(3).map(p -> p.get("id")).toList();
        Object objetivo=org.springframework.test.util.AopTestUtils.getTargetObject(rutaService);
        Object anterior=org.springframework.test.util.ReflectionTestUtils.getField(objetivo,"rutasOsrmUrl");
        try {
            org.springframework.test.util.ReflectionTestUtils.setField(objetivo,"rutasOsrmUrl","https://router.project-osrm.org");
            var respuesta=request("POST","/routes/optimize",recolector,Map.of("latitudOrigen",-12.12,"longitudOrigen",-77.03,"puntosIds",ids));
            java.nio.file.Files.write(java.nio.file.Path.of("target/osrm-real-response.json"),respuesta.body());
            var datos=object(respuesta,200);assertEquals(3,((List<?>)datos.get("paradas")).size());
            assertTrue(((Number)datos.get("distanciaTotalKm")).doubleValue()>0);
        } finally {org.springframework.test.util.ReflectionTestUtils.setField(objetivo,"rutasOsrmUrl",anterior);}
    }
}
