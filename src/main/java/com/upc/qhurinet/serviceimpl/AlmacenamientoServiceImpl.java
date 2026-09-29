package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.services.AlmacenamientoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {

    @Value("${almacenamiento.ruta}")
    private String rutaBase;

    @Override
    public String guardar(MultipartFile archivo, String carpeta, List<String> extensionesPermitidas) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("archivo: es obligatorio");
        }
        String extension = obtenerExtension(archivo.getOriginalFilename());
        if (!extensionesPermitidas.contains(extension)) {
            throw new IllegalArgumentException("archivo: formato no admitido. Formatos permitidos: "
                    + String.join(", ", extensionesPermitidas));
        }
        // Nombre generado por el servidor: evita colisiones y rutas manipuladas por el cliente
        String nombreArchivo = UUID.randomUUID() + "." + extension;
        Path directorio = Paths.get(rutaBase, carpeta);
        try (InputStream contenido = archivo.getInputStream()) {
            Files.createDirectories(directorio);
            Files.copy(contenido, directorio.resolve(nombreArchivo), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo", e);
        }
        return rutaBase + "/" + carpeta + "/" + nombreArchivo;
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return "";
        }
        return nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase();
    }
}
