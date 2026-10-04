package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.DocumentoVerificacionRepositorio;
import com.upc.qhurinet.repositories.PublicacionMaterialRepositorio;
import com.upc.qhurinet.repositories.TicketSoporteRepositorio;
import com.upc.qhurinet.repositories.UsuarioRepositorio;
import com.upc.qhurinet.services.AlmacenamientoService;

import org.apache.pdfbox.Loader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.imageio.ImageIO;

@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {

    private static final List<String> CARPETAS =
            List.of(
                    "fotos_perfil",
                    "fotos_publicaciones",
                    "documentos_verificacion",
                    "evidencias_tickets");

    @Value("${almacenamiento.ruta}")
    private String rutaBase;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PublicacionMaterialRepositorio publicacionMaterialRepositorio;

    @Autowired
    private DocumentoVerificacionRepositorio documentoVerificacionRepositorio;

    @Autowired
    private TicketSoporteRepositorio ticketSoporteRepositorio;

    @Override
    public String validar(MultipartFile archivo, List<String> extensiones) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("archivo: es obligatorio");
        }
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("archivo: máximo 5 MB");
        }
        String extension = extension(archivo.getOriginalFilename());
        if (!extensiones.contains(extension)) {
            throw new IllegalArgumentException("archivo: formato no admitido");
        }
        try {
            byte[] b = archivo.getBytes();
            boolean valido =
                    switch (extension) {
                        case "jpg", "jpeg", "png" -> imagenValida(b, extension);
                        case "webp" ->
                                b.length >= 20
                                        && new String(b, 0, 4, StandardCharsets.US_ASCII)
                                                .equals("RIFF")
                                        && new String(b, 8, 4, StandardCharsets.US_ASCII)
                                                .equals("WEBP")
                                        && Integer.toUnsignedLong(
                                                                ByteBuffer.wrap(b, 4, 4)
                                                                        .order(
                                                                                ByteOrder
                                                                                        .LITTLE_ENDIAN)
                                                                        .getInt())
                                                        + 8
                                                == b.length;
                        case "pdf" -> pdfValido(b);
                        default -> false;
                    };
            if (!valido) {
                throw new IllegalArgumentException("archivo: contenido no corresponde al formato");
            }
            return tipo(extension);
        } catch (IOException ex) {
            throw new IllegalArgumentException("archivo: no se pudo leer");
        }
    }

    @Override
    public String guardar(MultipartFile archivo, String carpeta, List<String> extensiones) {
        validar(archivo, extensiones);
        if (!CARPETAS.contains(carpeta)) {
            throw new IllegalArgumentException("carpeta: no admitida");
        }
        String nombre = UUID.randomUUID() + "." + extension(archivo.getOriginalFilename());
        Path raiz = Paths.get(rutaBase).toAbsolutePath().normalize();
        Path destino = raiz.resolve(carpeta).resolve(nombre).normalize();
        try {
            Files.createDirectories(destino.getParent());
            if (!destino.getParent().toRealPath().startsWith(raiz.toRealPath())) {
                throw new AccessDeniedException("Ruta no admitida");
            }
            Files.write(destino, archivo.getBytes(), StandardOpenOption.CREATE_NEW);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCompletion(int estado) {
                                if (estado != STATUS_COMMITTED) {
                                    try {
                                        Files.deleteIfExists(destino);
                                    } catch (IOException ignored) {
                                    }
                                }
                            }
                        });
            }
            return "/api/v1/files/" + carpeta + "--" + nombre;
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar el archivo", ex);
        }
    }

    @Override
    public Resource obtener(String email, String id) {
        if (id == null || !id.matches("[a-z_]+--[0-9a-fA-F-]{36}\\.(png|jpg|jpeg|webp|pdf)")) {
            throw new NoSuchElementException("Archivo no encontrado");
        }
        String[] partes = id.split("--", 2);
        if (!CARPETAS.contains(partes[0])) {
            throw new NoSuchElementException("Archivo no encontrado");
        }
        String url = "/api/v1/files/" + id;
        boolean encontrado;
        switch (partes[0]) {
            case "fotos_perfil" ->
                    encontrado = !usuarioRepositorio.findByFotoPerfilUrl(url).isEmpty();
            case "fotos_publicaciones" ->
                    encontrado = !publicacionMaterialRepositorio.findByFotoUrl(url).isEmpty();
            case "documentos_verificacion" -> {
                var doc =
                        documentoVerificacionRepositorio
                                .findByUrlArchivo(url)
                                .orElseThrow(
                                        () -> new NoSuchElementException("Archivo no encontrado"));
                if (!doc.getUsuario().getEmail().equals(email)) {
                    throw new AccessDeniedException("Documento privado");
                }
                encontrado = true;
            }
            default -> {
                var ticket =
                        ticketSoporteRepositorio
                                .findByEvidenciaUrl(url)
                                .orElseThrow(
                                        () -> new NoSuchElementException("Archivo no encontrado"));
                if (!ticket.getUsuario().getEmail().equals(email)) {
                    throw new AccessDeniedException("Evidencia privada");
                }
                encontrado = true;
            }
        }
        if (!encontrado) {
            throw new NoSuchElementException("Archivo no encontrado");
        }
        Path raiz = Paths.get(rutaBase).toAbsolutePath().normalize();
        Path archivo = raiz.resolve(partes[0]).resolve(partes[1]).normalize();
        try {
            if (!Files.isRegularFile(archivo)
                    || !archivo.toRealPath().startsWith(raiz.toRealPath())) {
                throw new NoSuchElementException("Archivo no encontrado");
            }
            return new FileSystemResource(archivo);
        } catch (IOException ex) {
            throw new NoSuchElementException("Archivo no encontrado");
        }
    }

    @Override
    public String tipo(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    private boolean imagenValida(byte[] bytes, String extension) throws IOException {
        try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                return false;
            }
            var reader = readers.next();
            try {
                reader.setInput(stream);
                String formato = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!("png".equals(extension)
                        ? "png".equals(formato)
                        : "jpeg".equals(formato) || "jpg".equals(formato))) {
                    return false;
                }
                if ((long) reader.getWidth(0) * reader.getHeight(0) > 25000000L) {
                    return false;
                }
                return reader.read(0) != null;
            } finally {
                reader.dispose();
            }
        }
    }

    private boolean pdfValido(byte[] bytes) {
        try (var pdf = Loader.loadPDF(bytes)) {
            return pdf.getNumberOfPages() > 0 && !pdf.isEncrypted();
        } catch (IOException ex) {
            return false;
        }
    }

    private String extension(String nombre) {
        return nombre == null || !nombre.contains(".")
                ? ""
                : nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
