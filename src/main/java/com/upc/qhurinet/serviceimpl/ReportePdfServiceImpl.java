package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.HistorialDTO;
import com.upc.qhurinet.services.ReportePdfService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportePdfServiceImpl implements ReportePdfService {

    @Override
    public byte[] generar(String usuario, String periodo, List<HistorialDTO> registros) {
        PDFont fuente = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        List<String> cabecera = new ArrayList<>();
        try (PDDocument documento = new PDDocument();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            agregar(cabecera, "QhuriNet - Historial de recolecciones", fuente);
            agregar(cabecera, "Usuario: " + usuario, fuente);
            agregar(cabecera, "Periodo: " + periodo, fuente);
            cabecera.add("");
            List<List<String>> paginas = new ArrayList<>();
            List<String> lineas = new ArrayList<>(cabecera);
            for (HistorialDTO registro : registros) {
                List<String> bloque = new ArrayList<>();
                agregar(
                        bloque,
                        registro.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                + " | "
                                + registro.getEstado(),
                        fuente);
                agregar(
                        bloque,
                        registro.getMaterial()
                                + " | "
                                + registro.getCantidad().toPlainString()
                                + " "
                                + registro.getUnidadMedida(),
                        fuente);
                agregar(bloque, "Contraparte: " + registro.getContraparte(), fuente);
                agregar(
                        bloque,
                        "Monto: "
                                + (registro.getMontoPago() == null
                                        ? "Sin pago registrado"
                                        : registro.getMontoPago().toPlainString()),
                        fuente);
                bloque.add("");
                if (lineas.size() + bloque.size() > 46) {
                    paginas.add(lineas);
                    lineas = new ArrayList<>(cabecera);
                }
                lineas.addAll(bloque);
            }
            paginas.add(lineas);
            int numeroPagina = 0;
            for (List<String> contenidoPagina : paginas) {
                numeroPagina++;
                PDPage pagina = new PDPage();
                documento.addPage(pagina);
                try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                    contenido.beginText();
                    contenido.setFont(fuente, 11);
                    contenido.setLeading(15);
                    contenido.newLineAtOffset(45, 745);
                    for (String linea : contenidoPagina) {
                        contenido.showText(linea);
                        contenido.newLine();
                    }
                    contenido.endText();
                    contenido.beginText();
                    contenido.setFont(fuente, 9);
                    contenido.newLineAtOffset(45, 30);
                    contenido.showText("QhuriNet - Página " + numeroPagina);
                    contenido.endText();
                }
            }
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el PDF", ex);
        }
    }

    private void agregar(List<String> lineas, String texto, PDFont fuente) throws IOException {
        StringBuilder linea = new StringBuilder();
        for (int cp : texto.codePoints().toArray()) {
            String caracter = Character.isISOControl(cp) ? " " : new String(Character.toChars(cp));
            try {
                fuente.encode(caracter);
            } catch (IllegalArgumentException ex) {
                caracter = "?";
            }
            if (fuente.getStringWidth(linea + caracter) * 11 / 1000 > 520) {
                int espacio = linea.lastIndexOf(" ");
                if (espacio > 0) {
                    lineas.add(linea.substring(0, espacio));
                    linea.delete(0, espacio + 1);
                } else {
                    lineas.add(linea.toString());
                    linea.setLength(0);
                }
            }
            linea.append(caracter);
        }
        lineas.add(linea.toString());
    }
}
