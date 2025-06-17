package clasesgestor;

import clasesmodelo.Pago;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la colección de todas las transacciones de pago individuales.
 * Se encarga de la persistencia de los pagos en el archivo pagos.csv,
 * guardando cada pago que se realiza en el sistema.
 *
 * @author Tu Nombre
 * @version 1.2
 */
public class GestorPago {
    private List<Pago> pagos;
    private final String archivoPagos = "data/pagos.csv";
    private static final String CSV_HEADER = "ID_Prestamo;Numero_Cuota;Monto_Pagado;Fecha_Pago";

    /**
     * Constructor del GestorPago. Inicializa la lista de pagos
     * y carga los registros existentes desde el archivo CSV.
     */
    public GestorPago() {
        this.pagos = new ArrayList<>();
        cargarDesdeArchivoCSV(archivoPagos);
    }

    /**
     * Agrega una nueva transacción de pago a la lista y la guarda inmediatamente en el archivo CSV.
     * @param pago El objeto Pago a agregar.
     */
    public void agregarPago(Pago pago) {
        this.pagos.add(pago);
        guardarEnArchivoCSV(archivoPagos);
    }

    /**
     * Devuelve una copia de la lista de todas las transacciones de pago registradas.
     * Se devuelve una copia para proteger la lista original de modificaciones externas.
     *
     * @return una nueva lista conteniendo todos los objetos Pago.
     */
    public List<Pago> getAllPagos() {
        return new ArrayList<>(this.pagos);
    }

    /**
     * Elimina todas las transacciones de pago asociadas a un ID de préstamo específico.
     * Después de la eliminación, guarda los cambios en el archivo CSV.
     *
     * @param idPrestamo El ID del préstamo cuyos pagos se deben eliminar.
     */
    public void eliminarPagosDePrestamo(String idPrestamo) {
        // Usa removeIf para eliminar de la lista todos los pagos que coincidan con el idPrestamo.
        boolean seEliminaronPagos = this.pagos.removeIf(pago -> pago.getIdPrestamo().equals(idPrestamo));

        // Si se realizó alguna eliminación, guarda el estado actualizado del archivo de pagos.
        if (seEliminaronPagos) {
            guardarEnArchivoCSV(archivoPagos);
        }
    }
    /**
     * Carga las transacciones de pago desde un archivo CSV al iniciar el sistema.
     * Lee el archivo línea por línea y las convierte en objetos Pago.
     *
     * @param nombreArchivo La ruta del archivo CSV de pagos.
     */
    public void cargarDesdeArchivoCSV(String nombreArchivo) {
        File archivo = new File(nombreArchivo);
        if (!archivo.exists()) {
            // No se imprime nada para una carga silenciosa al inicio
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            br.readLine(); // Saltar el encabezado

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");

                if (datos.length == 4) {
                    try {
                        String idPrestamo = datos[0];
                        int numeroCuota = Integer.parseInt(datos[1]);
                        double montoPagado = Double.parseDouble(datos[2]);
                        LocalDate fechaPago = LocalDate.parse(datos[3]);

                        Pago nuevoPago = new Pago(idPrestamo, numeroCuota, montoPagado, fechaPago);
                        pagos.add(nuevoPago);
                    } catch (NumberFormatException e) {
                        System.err.println("Advertencia: Error al parsear número o monto de pago en CSV, línea omitida: " + linea + " (" + e.getMessage() + ")");
                    } catch (DateTimeParseException e) {
                        System.err.println("Advertencia: Error al parsear fecha de pago en CSV, línea omitida: " + linea + " (" + e.getMessage() + ")");
                    }
                } else {
                    System.err.println("Advertencia: Línea de pago con formato incorrecto en CSV, se omite: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar pagos desde CSV: " + e.getMessage());
        }
    }

    /**
     * Guarda todas las transacciones de pago de la lista en memoria a un archivo CSV,
     * sobrescribiendo el contenido anterior.
     *
     * @param nombreArchivo La ruta del archivo CSV donde se guardarán los datos.
     */
    public void guardarEnArchivoCSV(String nombreArchivo) {
        crearDirectorioSiNoExiste(nombreArchivo);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {
            bw.write(CSV_HEADER);
            bw.newLine();

            for (Pago p : pagos) {
                String linea = String.join(";",
                        p.getIdPrestamo(),
                        String.valueOf(p.getNumeroCuota()),
                        String.valueOf(p.getMontoPagado()),
                        p.getFechaPago().toString()
                );
                bw.write(linea);
                bw.newLine();
            }
            // Opcional: Descomentar para depuración
            // System.out.println(pagos.size() + " pagos guardados en CSV.");
        } catch (IOException e) {
            System.err.println("Error al guardar pagos en CSV: " + e.getMessage());
        }
    }

    /**
     * Helper privado para asegurar que el directorio de un archivo exista antes de escribirlo.
     * @param rutaArchivo La ruta completa del archivo.
     */
    private void crearDirectorioSiNoExiste(String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        File directorio = archivo.getParentFile();

        if (directorio != null && !directorio.exists()) {
            directorio.mkdirs();
        }
    }
}