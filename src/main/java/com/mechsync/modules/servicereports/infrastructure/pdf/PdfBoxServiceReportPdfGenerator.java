package com.mechsync.modules.servicereports.infrastructure.pdf;

import com.mechsync.modules.servicereports.application.port.out.ServiceReportPdfGeneratorPort;
import com.mechsync.modules.servicereports.domain.exception.ServiceReportPdfGenerationException;
import com.mechsync.modules.servicereports.domain.model.ServiceReportPdfData;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

@Component
public class PdfBoxServiceReportPdfGenerator implements ServiceReportPdfGeneratorPort {
    private static final PDFont REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX"));

    @Override
    public byte[] generate(ServiceReportPdfData data) {
        LocalDateTime generatedAt = LocalDateTime.now();
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Writer writer = new Writer(document, generatedAt);
            writer.title("Reporte de servicio");
            writer.text("MechSync", BOLD, 11, Color.DARK_GRAY, 15);
            writer.text("Identificador: REP-" + data.reportId(), REGULAR, 10,
                    Color.DARK_GRAY, 14);
            writer.text("Fecha de generación: " + date(generatedAt), REGULAR, 9,
                    Color.GRAY, 20);

            writer.section("Datos del reporte");
            writer.keyValue("Report ID", String.valueOf(data.reportId()));
            writer.keyValue("Job ID", String.valueOf(data.jobId()));
            writer.keyValue("Estado", data.reportStatus().name());
            writer.keyValue("Fecha de reporte", date(data.reportDate()));
            writer.keyValue("Fecha de entrega", date(data.deliveredAt()));
            writer.keyValue("Confirmación del cliente",
                    data.customerConfirmation() ? "Confirmada" : "No registrada");
            writer.label("Descripción final");
            writer.paragraph(value(data.finalDescription()));

            writer.section("Datos operativos");
            writer.keyValue("Work Order ID", value(data.workOrderId()));
            writer.keyValue("Vehicle Intake ID", value(data.vehicleIntakeId()));
            writer.keyValue("Técnico", value(data.technicianName()));
            writer.keyValue("Cliente", value(data.customerName()));
            writer.keyValue("Vehículo", vehicle(data));
            writer.keyValue("Placa", value(data.licensePlate()));
            writer.keyValue("VIN", value(data.vin()));
            writer.keyValue("Kilometraje", data.mileage() == null
                    ? "No disponible" : data.mileage() + " km");

            writer.section("Servicios reales");
            if (data.services().isEmpty()) {
                writer.paragraph("Sin servicios reales registrados.");
            } else {
                writer.table(new String[] {"Servicio", "Cantidad", "P. unitario", "Subtotal"},
                        data.services().stream().map(line -> new String[] {
                                value(line.name()), quantity(line.quantity()),
                                money(line.unitPrice()), money(line.subtotal())
                        }).toList(), new float[] {250, 70, 90, 90});
            }

            writer.section("Piezas reales");
            if (data.parts().isEmpty()) {
                writer.paragraph("Sin piezas reales registradas.");
            } else {
                writer.table(new String[] {"Pieza", "Cantidad / unidad", "P. unitario", "Subtotal"},
                        data.parts().stream().map(line -> new String[] {
                                value(line.name()), quantity(line.quantity()) + " "
                                        + value(line.measurementUnit()),
                                money(line.unitPrice()), money(line.subtotal())
                        }).toList(), new float[] {230, 90, 90, 90});
            }

            writer.section("Resumen financiero");
            writer.moneyRow("Subtotal", data.finalSubtotal());
            writer.moneyRow("IVA", data.finalIva());
            writer.moneyRow("Total", data.finalTotal());

            writer.section("Nota de cierre");
            writer.paragraph("Este documento resume el cierre operativo del trabajo registrado "
                    + "en MechSync.");
            writer.paragraph("La cotización aprobada y las líneas reales permanecen registradas "
                    + "en el sistema.");
            writer.finish();
            document.save(output);
            return output.toByteArray();
        } catch (IOException | IllegalArgumentException exception) {
            throw new ServiceReportPdfGenerationException(
                    "Could not generate Service Report PDF", exception);
        }
    }

    private static String vehicle(ServiceReportPdfData data) {
        List<String> values = new ArrayList<>();
        if (data.vehicleBrand() != null) values.add(data.vehicleBrand());
        if (data.vehicleModel() != null) values.add(data.vehicleModel());
        if (data.vehicleYear() != null) values.add(String.valueOf(data.vehicleYear()));
        return values.isEmpty() ? "No disponible" : String.join(" ", values);
    }

    private static String value(Object value) {
        return value == null || value.toString().isBlank() ? "No disponible" : value.toString();
    }

    private static String date(LocalDateTime value) {
        return value == null ? "No disponible" : DATE_TIME.format(value);
    }

    private static String quantity(BigDecimal value) {
        return value == null ? "No disponible" : value.stripTrailingZeros().toPlainString();
    }

    private static String money(BigDecimal value) {
        if (value == null) return "No disponible";
        return String.format(Locale.US, "MXN $%,.2f", value.setScale(2, RoundingMode.HALF_UP));
    }

    private static final class Writer {
        private static final float MARGIN = 50;
        private static final float CONTENT_WIDTH = PDRectangle.LETTER.getWidth() - (MARGIN * 2);
        private static final float BOTTOM = 58;
        private final PDDocument document;
        private final LocalDateTime generatedAt;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        private Writer(PDDocument document, LocalDateTime generatedAt) throws IOException {
            this.document = document;
            this.generatedAt = generatedAt;
            newPage();
        }

        private void title(String value) throws IOException {
            text(value, BOLD, 20, new Color(142, 0, 30), 25);
        }

        private void section(String value) throws IOException {
            ensure(32);
            y -= 8;
            stream.setNonStrokingColor(new Color(238, 241, 245));
            stream.addRect(MARGIN, y - 18, CONTENT_WIDTH, 23);
            stream.fill();
            drawText(value, BOLD, 11, new Color(35, 43, 54), MARGIN + 7, y - 11);
            y -= 30;
        }

        private void keyValue(String key, String value) throws IOException {
            ensure(17);
            drawText(key + ":", BOLD, 9, Color.DARK_GRAY, MARGIN, y);
            drawText(safe(value), REGULAR, 9, Color.DARK_GRAY, MARGIN + 130, y);
            y -= 15;
        }

        private void label(String value) throws IOException {
            ensure(16);
            drawText(value + ":", BOLD, 9, Color.DARK_GRAY, MARGIN, y);
            y -= 14;
        }

        private void paragraph(String value) throws IOException {
            List<String> lines = wrap(safe(value), REGULAR, 9, CONTENT_WIDTH);
            ensure(lines.size() * 12 + 5);
            for (String line : lines) {
                drawText(line, REGULAR, 9, Color.DARK_GRAY, MARGIN, y);
                y -= 12;
            }
            y -= 5;
        }

        private void text(String value, PDFont font, float size, Color color, float after)
                throws IOException {
            ensure(after + 5);
            drawText(safe(value), font, size, color, MARGIN, y);
            y -= after;
        }

        private void moneyRow(String label, BigDecimal amount) throws IOException {
            ensure(19);
            drawText(label, BOLD, 10, Color.DARK_GRAY, MARGIN + 285, y);
            drawText(money(amount), BOLD, 10, Color.DARK_GRAY, MARGIN + 390, y);
            y -= 17;
        }

        private void table(String[] headers, List<String[]> rows, float[] widths)
                throws IOException {
            drawTableHeader(headers, widths);
            for (String[] row : rows) {
                List<String> firstColumn = wrap(safe(row[0]), REGULAR, 8, widths[0] - 8);
                float rowHeight = Math.max(22, firstColumn.size() * 10 + 8);
                if (y - rowHeight < BOTTOM) {
                    newPage();
                    drawTableHeader(headers, widths);
                }
                float x = MARGIN;
                for (int column = 0; column < widths.length; column++) {
                    stream.setStrokingColor(new Color(205, 210, 217));
                    stream.addRect(x, y - rowHeight, widths[column], rowHeight);
                    stream.stroke();
                    List<String> values = column == 0 ? firstColumn
                            : List.of(safe(row[column]));
                    float textY = y - 13;
                    for (String value : values) {
                        drawText(value, REGULAR, 8, Color.DARK_GRAY, x + 4, textY);
                        textY -= 10;
                    }
                    x += widths[column];
                }
                y -= rowHeight;
            }
            y -= 8;
        }

        private void drawTableHeader(String[] headers, float[] widths) throws IOException {
            ensure(24);
            float x = MARGIN;
            for (int i = 0; i < widths.length; i++) {
                stream.setNonStrokingColor(new Color(70, 78, 90));
                stream.addRect(x, y - 22, widths[i], 22);
                stream.fill();
                drawText(headers[i], BOLD, 8, Color.WHITE, x + 4, y - 14);
                x += widths[i];
            }
            y -= 22;
        }

        private void ensure(float height) throws IOException {
            if (y - height < BOTTOM) newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) stream.close();
            page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        private void finish() throws IOException {
            if (stream != null) stream.close();
            int pages = document.getNumberOfPages();
            for (int index = 0; index < pages; index++) {
                PDPage target = document.getPage(index);
                try (PDPageContentStream footer = new PDPageContentStream(document, target,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    String left = "Generado: " + date(generatedAt);
                    String right = "Página " + (index + 1) + " de " + pages;
                    drawText(footer, left, REGULAR, 8, Color.GRAY, MARGIN, 30);
                    float rightWidth = width(right, REGULAR, 8);
                    drawText(footer, right, REGULAR, 8, Color.GRAY,
                            PDRectangle.LETTER.getWidth() - MARGIN - rightWidth, 30);
                }
            }
        }

        private void drawText(String value, PDFont font, float size, Color color, float x, float y)
                throws IOException {
            drawText(stream, value, font, size, color, x, y);
        }

        private static void drawText(PDPageContentStream content, String value, PDFont font,
                float size, Color color, float x, float y) throws IOException {
            content.beginText();
            content.setFont(font, size);
            content.setNonStrokingColor(color);
            content.newLineAtOffset(x, y);
            content.showText(safe(value));
            content.endText();
        }

        private static List<String> wrap(String value, PDFont font, float size, float maxWidth)
                throws IOException {
            if (value.isBlank()) return List.of("No disponible");
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : value.split("\\s+")) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (width(candidate, font, size) <= maxWidth || line.isEmpty()) {
                    line.setLength(0);
                    line.append(candidate);
                } else {
                    lines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                }
            }
            if (!line.isEmpty()) lines.add(line.toString());
            return lines;
        }

        private static float width(String value, PDFont font, float size) throws IOException {
            return font.getStringWidth(safe(value)) / 1000 * size;
        }

        private static String safe(String value) {
            if (value == null || value.isBlank()) return "No disponible";
            String normalized = value.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", " ")
                    .replaceAll("\\s+", " ").trim();
            StringBuilder result = new StringBuilder(normalized.length());
            normalized.codePoints().forEach(codePoint -> {
                String character = new String(Character.toChars(codePoint));
                try {
                    REGULAR.encode(character);
                    result.append(character);
                } catch (IllegalArgumentException | IOException exception) {
                    result.append('?');
                }
            });
            return result.toString();
        }
    }
}
