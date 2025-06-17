package clasesgestor;

import clasesmodelo.Cliente;
import clasesmodelo.Prestamo;
import clasesmodelo.Cuota;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Clase responsable de generar reportes en formato PDF para el sistema financiero.
 * Utiliza la biblioteca Apache PDFBox para crear documentos con tablas y texto formateado.
 *
 * @author Tu Nombre
 * @version 1.2
 */
public class GestorReportes {

    private PDDocument document;
    private PDPageContentStream contentStream;
    private float yPosition;

    private static final float MARGIN_X = 50;
    private static final float START_Y = 750;
    private static final float BOTTOM_MARGIN = 50;
    private static final float TABLE_ROW_HEIGHT = 20f;
    private static final float CELL_MARGIN = 5f;

    /**
     * Genera un reporte PDF completo para un cliente específico.
     * El reporte incluye datos personales, y tablas detalladas de sus préstamos y cuotas.
     *
     * @param cliente El objeto Cliente del cual se generará el reporte.
     * @param prestamos La lista de préstamos asociados a ese cliente.
     */
    public void generarReporteCliente(Cliente cliente, List<Prestamo> prestamos) {
        this.document = new PDDocument();

        try {
            File directorio = new File("reportes");
            if (!directorio.exists()) {
                directorio.mkdirs();
            }
            String nombreArchivo = "reportes/reporte_cliente_" + cliente.getdni() + ".pdf";

            startNewPage();

            // Título y datos del cliente
            writeLine(cliente.getNombre() + " " + cliente.getApellido(), 22, true);
            yPosition -= 15;
            writeLine("Reporte Financiero - Generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 14, false);
            yPosition -= 25;

            writeSectionTitle("Datos Personales");
            writeLine("DNI: " + cliente.getdni(), 12, false);
            writeLine("Dirección: " + cliente.getDireccion(), 12, false);
            writeLine("Teléfono: " + cliente.getTelefono(), 12, false);
            writeLine("Correo Electrónico: " + cliente.getCorreoElectronico(), 12, false);
            yPosition -= 25;

            // Sección de Préstamos y Cuotas en tablas
            writeSectionTitle("Resumen de Préstamos y Detalle de Cuotas");
            if (prestamos.isEmpty()) {
                checkPageBreak();
                writeLine("El cliente no tiene préstamos asociados.", 12, false);
            } else {
                for (Prestamo p : prestamos) {
                    checkPageBreak();

                    String[] prestamoHeaders = {"ID Préstamo", "Tipo", "Monto Original", "Fecha Inicio", "Saldo Pendiente"};
                    List<String[]> prestamoData = new ArrayList<>();
                    prestamoData.add(new String[]{
                            p.getIdPrestamo(), p.getTipoPrestamoString(), p.getMontoFormateado(),
                            p.getFechaInicio().toString(),
                            NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(p.calcularSaldoPendienteTotal())
                    });
                    drawTable(prestamoHeaders, prestamoData, new float[]{0.15f, 0.15f, 0.25f, 0.2f, 0.25f});
                    yPosition -= 10;

                    String[] cuotasHeaders = {"#", "Vencimiento", "Estado", "Saldo Pendiente", "Penalidad"};
                    List<String[]> cuotasData = new ArrayList<>();
                    for (Cuota c : p.getCuotas()) {
                        String penalidad = c.getMontoPenalidadAcumulada() > 0 ? c.getMontoPenalidadAcumuladaFormateado() : "-";
                        cuotasData.add(new String[]{
                                String.valueOf(c.getNumeroCuota()),
                                c.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                c.getEstado().toString(), c.getSaldoPendienteFormateado(), penalidad
                        });
                    }
                    drawTable(cuotasHeaders, cuotasData, new float[]{0.05f, 0.25f, 0.25f, 0.25f, 0.20f});
                    yPosition -= 25;
                }
            }

            contentStream.close();
            document.save(nombreArchivo);

            System.out.println("**************************************************");
            System.out.println("PDF generado:" + nombreArchivo);
            System.out.println("**************************************************");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (document != null) {
                    document.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * [PRIVADO] Escribe una línea de texto simple y mueve el cursor verticalmente.
     * @param text El texto a escribir.
     * @param fontSize El tamaño de la fuente.
     * @param isBold Si el texto debe estar en negrita.
     * @throws IOException Si ocurre un error al escribir en el stream.
     */
    private void writeLine(String text, int fontSize, boolean isBold) throws IOException {
        contentStream.beginText();
        contentStream.setFont(isBold ? new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD) : new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
        contentStream.newLineAtOffset(MARGIN_X, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        yPosition -= (fontSize * 1.5f);
    }

    /**
     * [PRIVADO] Escribe texto dentro de una celda de tabla, sin mover el cursor vertical principal.
     * @param text El texto a escribir.
     * @param fontSize El tamaño de la fuente.
     * @param isBold Si el texto debe estar en negrita.
     * @param xPosition La coordenada X donde comenzará el texto.
     * @throws IOException Si ocurre un error al escribir en el stream.
     */
    private void writeCellText(String text, int fontSize, boolean isBold, float xPosition) throws IOException {
        float y = yPosition + (TABLE_ROW_HEIGHT - fontSize) / 2;
        contentStream.beginText();
        contentStream.setFont(isBold ? new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD) : new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
        contentStream.newLineAtOffset(xPosition, y);
        contentStream.showText(text != null ? text : "");
        contentStream.endText();
    }

    /**
     * [PRIVADO] Dibuja una tabla completa con cabecera y datos.
     * @param headers Los títulos de las columnas.
     * @param data Una lista, donde cada elemento es un array de Strings representando una fila.
     * @param colWidthsPorcentajes Array con el porcentaje de ancho para cada columna (la suma debe ser 1).
     * @throws IOException Si ocurre un error al dibujar.
     */
    private void drawTable(String[] headers, List<String[]> data, float[] colWidthsPorcentajes) throws IOException {
        float tableWidth = PDRectangle.A4.getWidth() - (MARGIN_X * 2);
        float[] colWidths = new float[colWidthsPorcentajes.length];
        for (int i = 0; i < colWidthsPorcentajes.length; i++) {
            colWidths[i] = tableWidth * colWidthsPorcentajes[i];
        }
        drawRow(headers, colWidths, true);
        for (String[] rowData : data) {
            drawRow(rowData, colWidths, false);
        }
    }

    /**
     * [PRIVADO] Dibuja una única fila de una tabla, incluyendo bordes y texto.
     * @param rowData Array con los datos de cada celda de la fila.
     * @param colWidths Array con los anchos de cada columna.
     * @param isHeader Si la fila es una cabecera (para aplicar estilo).
     * @throws IOException Si ocurre un error al dibujar.
     */
    private void drawRow(String[] rowData, float[] colWidths, boolean isHeader) throws IOException {
        checkPageBreak();
        float x = MARGIN_X;
        if (isHeader) {
            contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
            contentStream.addRect(x, yPosition, PDRectangle.A4.getWidth() - (MARGIN_X * 2), TABLE_ROW_HEIGHT);
            contentStream.fill();
            contentStream.setNonStrokingColor(Color.BLACK);
        }
        for (int i = 0; i < rowData.length; i++) {
            contentStream.addRect(x, yPosition, colWidths[i], TABLE_ROW_HEIGHT);
            writeCellText(rowData[i], 10, isHeader, x + CELL_MARGIN);
            x += colWidths[i];
        }
        contentStream.stroke();
        yPosition -= TABLE_ROW_HEIGHT;
    }

    /**
     * [PRIVADO] Dibuja un título de sección formateado con una línea debajo.
     * @param title El texto del título.
     * @throws IOException Si ocurre un error al dibujar.
     */
    private void writeSectionTitle(String title) throws IOException {
        checkPageBreak();
        writeLine(title, 16, true);
        yPosition -= 5;
        contentStream.setStrokingColor(0, 0, 0);
        contentStream.setLineWidth(1);
        contentStream.moveTo(MARGIN_X, yPosition);
        contentStream.lineTo(MARGIN_X + 500, yPosition);
        contentStream.stroke();
        yPosition -= 15;
    }

    /**
     * [PRIVADO] Verifica si se necesita una nueva página y la crea si es necesario.
     * @throws IOException Si ocurre un error al crear la página.
     */
    private void checkPageBreak() throws IOException {
        if (yPosition <= BOTTOM_MARGIN) {
            contentStream.close();
            startNewPage();
        }
    }

    /**
     * [PRIVADO] Crea una nueva página en el documento y reinicia el cursor de escritura.
     * @throws IOException Si ocurre un error al crear el stream de contenido.
     */
    private void startNewPage() throws IOException {
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);
        contentStream = new PDPageContentStream(document, newPage);
        yPosition = START_Y;
    }
}