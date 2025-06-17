package clasesgestor;

import clasesmodelo.Prestamo;
import clasesmodelo.Pago;
import clasesmodelo.Cuota;
import java.time.LocalDate;
import java.io.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Clase central para gestionar las operaciones de préstamos.
 * Maneja la creación, consulta, eliminación y persistencia de préstamos,
 * interactuando con GestorPago para registrar las transacciones.
 *
 * @author Tu Nombre
 * @version 1.2
 */
public class GestorPrestamo {
    private List<Prestamo> prestamos;
    private AtomicInteger ultimoId;
    private final String archivoPrestamos = "data/prestamos.csv";
    private static final String CSV_HEADER = "ID_Prestamo;ID_Cliente;Monto;Cuotas;Tipo;Fecha_Inicio";

    private GestorPago gestorPagos;

    /**
     * Constructor del GestorPrestamo.
     * Carga los préstamos existentes desde el archivo CSV al iniciar.
     *
     * @param gestorPagos Una instancia de GestorPago para registrar las transacciones de pago.
     */
    public GestorPrestamo(GestorPago gestorPagos) {
        this.prestamos = new ArrayList<>();
        this.ultimoId = new AtomicInteger(0);
        this.gestorPagos = gestorPagos;
        cargarDesdeArchivoCSV(archivoPrestamos);
    }

    /**
     * Crea un nuevo préstamo para un cliente, lo agrega a la lista y lo persiste en el archivo CSV.
     *
     * @param dniCliente El DNI del cliente que solicita el préstamo.
     * @param monto El capital del préstamo.
     * @param cuotas El número de cuotas para la devolución.
     * @param esHipotecario {@code true} si es hipotecario, {@code false} si es personal.
     * @param fechaInicio La fecha de otorgamiento del préstamo.
     * @return {@code true} siempre, indicando que la operación de creación se ha intentado.
     */
    public boolean crearPrestamo(String dniCliente, double monto,
                                 int cuotas, boolean esHipotecario, LocalDate fechaInicio) {
        String idPrestamo = String.format("%04d", ultimoId.incrementAndGet());
        Prestamo nuevo = new Prestamo(
                idPrestamo,
                dniCliente,
                monto,
                cuotas,
                esHipotecario,
                fechaInicio
        );
        prestamos.add(nuevo);
        guardarEnArchivoCSV(archivoPrestamos);
        System.out.println("Préstamo " + idPrestamo + " creado exitosamente.");
        return true;
    }


    /**
     * Busca y devuelve un objeto Prestamo basado en su ID.
     *
     * @param idPrestamo El ID del préstamo a buscar.
     * @return El objeto Prestamo si se encuentra, de lo contrario {@code null}.
     */
    public Prestamo obtenerPrestamo(String idPrestamo) {
        for (Prestamo p : prestamos) {
            if (p.getIdPrestamo().equals(idPrestamo)) {
                return p;
            }
        }
        return null;
    }


    /**
     * Elimina un préstamo del sistema, solo si su saldo pendiente es cero.
     *
     * @param idPrestamo El ID del préstamo a eliminar.
     * @return {@code true} si el préstamo fue eliminado, {@code false} si no se pudo eliminar.
     */
    /**
     * Elimina un préstamo del sistema, solo si su saldo pendiente es cero.
     * Al eliminar el préstamo, también elimina todas las transacciones de pago asociadas.
     *
     * @param idPrestamo El ID del préstamo a eliminar.
     * @return {@code true} si el préstamo fue eliminado, {@code false} si no se pudo eliminar.
     */
    public boolean eliminarPrestamo(String idPrestamo) {
        Prestamo prestamo = obtenerPrestamo(idPrestamo);
        if (prestamo != null) {
            double saldoPendiente = prestamo.calcularSaldoPendienteTotal();
            if (saldoPendiente > 0) {
                System.out.println("Error: No se puede eliminar el préstamo " + idPrestamo + " porque tiene un saldo pendiente de " + NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(saldoPendiente) + ".");
                return false;
            }
            if (gestorPagos != null) {
                gestorPagos.eliminarPagosDePrestamo(idPrestamo);
            }
            prestamos.remove(prestamo);
            guardarEnArchivoCSV(archivoPrestamos);
            System.out.println("Préstamo " + idPrestamo + " y sus pagos asociados han sido eliminados exitosamente.");
            return true;
        }
        System.out.println("Error: No se encontró un préstamo con el ID " + idPrestamo + ".");
        return false;
    }


    /**
     * Devuelve una copia de la lista de todos los préstamos registrados.
     *
     * @return una nueva lista conteniendo todos los préstamos.
     */
    public List<Prestamo> listarPrestamos() {
        return new ArrayList<>(this.prestamos);
    }


    /**
     * Carga los datos de los préstamos desde un archivo CSV. Este método se llama
     * al iniciar el sistema para restaurar el estado anterior.
     *
     * @param nombreArchivo La ruta del archivo CSV de préstamos.
     */
    public void cargarDesdeArchivoCSV(String nombreArchivo) {
        File archivo = new File(nombreArchivo);
        if (!archivo.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            br.readLine(); // Saltar cabecera
            int maxId = 0;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                if (datos.length == 6) {
                    try {
                        String idPrestamo = datos[0];
                        // ... (código interno de parseo)
                        Prestamo nuevoPrestamo = new Prestamo(idPrestamo, datos[1], Double.parseDouble(datos[2]), Integer.parseInt(datos[3]), datos[4].equalsIgnoreCase("hipotecario"), LocalDate.parse(datos[5]));
                        this.prestamos.add(nuevoPrestamo);

                        // Lógica para mantener el contador de ID actualizado
                        try {
                            int numId = Integer.parseInt(idPrestamo.replaceAll("^P-", ""));
                            if (numId > maxId) {
                                maxId = numId;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Advertencia: ID de préstamo con formato no numérico, se omite para el contador: " + idPrestamo);
                        }

                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.err.println("Advertencia: Error al parsear datos de préstamo en CSV, línea omitida: " + linea);
                    }
                } else {
                    System.err.println("Advertencia: Línea de préstamo con formato incorrecto en CSV, se omite: " + linea);
                }
            }
            this.ultimoId.set(maxId);
        } catch (IOException e) {
            System.err.println("Error al cargar préstamos desde CSV: " + e.getMessage());
        }
    }


    /**
     * Guarda la lista completa de préstamos en el archivo CSV, sobrescribiendo el contenido.
     *
     * @param nombreArchivo La ruta del archivo CSV.
     */
    public void guardarEnArchivoCSV(String nombreArchivo) {
        crearDirectorioSiNoExiste(nombreArchivo);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {
            bw.write(CSV_HEADER);
            bw.newLine();

            for (Prestamo p : prestamos) {
                if ("00000000".equals(p.getDniCliente())) {
                    continue;
                }
                String tipo = p.esHipotecario() ? "hipotecario" : "personal";
                String linea = String.join(";",
                        p.getIdPrestamo(),
                        p.getDniCliente(),
                        String.valueOf(p.getMonto()),
                        String.valueOf(p.getNumeroCuotas()),
                        tipo,
                        p.getFechaInicio().toString()
                );
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar préstamos en CSV: " + e.getMessage());
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


    /**
     * Obtiene una lista de todos los préstamos asociados a un DNI de cliente específico.
     *
     * @param dniCliente El DNI del cliente.
     * @return una lista de objetos Prestamo.
     */
    public List<Prestamo> obtenerPrestamosPorCliente(String dniCliente) {
        return prestamos.stream()
                .filter(p -> p.getDniCliente().equals(dniCliente))
                .collect(Collectors.toList());
    }


    /**
     * Obtiene el último préstamo que fue añadido a la lista.
     *
     * @return el último objeto Prestamo añadido, o {@code null} si la lista está vacía.
     */
    public Prestamo obtenerUltimoPrestamo() {
        if (prestamos.isEmpty()) {
            return null;
        }
        return prestamos.get(prestamos.size() - 1);
    }

    /**
     * Orquesta el proceso de registrar un pago. Encuentra el préstamo, aplica el pago
     * a la cuota correspondiente y registra la transacción a través de GestorPago.
     *
     * @param idPrestamo El ID del préstamo sobre el cual se realiza el pago.
     * @param numeroCuota El número de la cuota a pagar.
     * @param montoPagado El monto a abonar.
     * @return {@code true} si el pago se registró correctamente, {@code false} en caso contrario.
     */
    public boolean registrarPago(String idPrestamo, int numeroCuota, double montoPagado) {
        Prestamo prestamo = obtenerPrestamo(idPrestamo);
        if (prestamo == null) {
            System.out.println("Error: Préstamo con ID " + idPrestamo + " no encontrado.");
            return false;
        }

        boolean pagoAplicadoEnCuota = prestamo.registrarPagoEnCuota(numeroCuota, montoPagado);
        if (pagoAplicadoEnCuota) {
            Pago nuevoPagoTransaccion = new Pago(idPrestamo, numeroCuota, montoPagado, LocalDate.now());
            if (gestorPagos != null) {
                gestorPagos.agregarPago(nuevoPagoTransaccion);
                System.out.println("Transacción de pago de " + NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(montoPagado) +
                        " para Préstamo " + idPrestamo + ", Cuota #" + numeroCuota + " registrada.");
            } else {
                System.err.println("Advertencia: GestorPagos no inicializado. La transacción de pago no se guardará persistentemente.");
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Reconcilia los pagos históricos (cargados del CSV) con los préstamos en memoria.
     * Este método es crucial para restaurar el estado correcto del sistema al iniciar.
     */
    public void aplicarPagosCargadosACuotas() {
        if (gestorPagos == null) {
            System.err.println("Advertencia: No se pueden aplicar pagos, GestorPagos no inicializado.");
            return;
        }

        List<Pago> todosLosPagosHistoricos = gestorPagos.getAllPagos();
        if (todosLosPagosHistoricos.isEmpty()) {
            // No imprimir nada en la carga silenciosa
            return;
        }

        for (Pago pago : todosLosPagosHistoricos) {
            Prestamo prestamo = obtenerPrestamo(pago.getIdPrestamo());
            if (prestamo != null) {
                prestamo.registrarPagoEnCuota(pago.getNumeroCuota(), pago.getMontoPagado());
            }
        }
    }

    /**
     * Itera sobre todos los préstamos para verificar y aplicar el estado de mora
     * a las cuotas que estén vencidas.
     * @param fechaActual La fecha actual para la verificación.
     */
    public void verificarYAplicarMoraATodosLosPrestamos(LocalDate fechaActual) {
        for (Prestamo prestamo : prestamos) {
            prestamo.verificarMoraDeCuotas(fechaActual);
        }
        guardarEnArchivoCSV(archivoPrestamos);
    }
}