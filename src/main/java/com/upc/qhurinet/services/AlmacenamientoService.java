package com.upc.qhurinet.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
 Patron de carga de archivos del proyecto.
 El archivo se guarda en una carpeta local (propiedad almacenamiento.ruta, por defecto "uploads")
 y la base de datos guarda unicamente la ruta que devuelve este servicio.

 Como reutilizarlo desde otro servicio (ejemplo END-07, UsuarioServiceImpl.actualizarFotoPerfil):
   1. El controlador recibe el archivo como multipart:
        @PostMapping(value = "/...", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        ... @RequestParam(value = "archivo", required = false) MultipartFile archivo
   2. El servicio llama a guardar(...) con su propia subcarpeta y sus extensiones admitidas:
        String ruta = almacenamientoService.guardar(archivo, "fotos_publicaciones", List.of("jpg", "jpeg", "png", "webp"));
   3. El servicio guarda la ruta en la columna *_url y la devuelve en el DTO.

 Subcarpetas previstas: fotos_perfil (END-07), fotos_publicaciones (END-20),
 documentos_verificacion (END-10), evidencias_tickets (END-48).
 Si falta el archivo o su extension no esta admitida, lanza IllegalArgumentException (400).
 El tamaño maximo lo controla spring.servlet.multipart.max-file-size.
*/
public interface AlmacenamientoService {
    public String guardar(MultipartFile archivo, String carpeta, List<String> extensionesPermitidas);
    String validar(MultipartFile archivo, List<String> extensiones);
    org.springframework.core.io.Resource obtener(String email, String id);
    String tipo(String extension);

}
