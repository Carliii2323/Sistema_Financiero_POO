import clasesgestor.GestorCliente;
import clasesgestor.GestorPrestamo;
import clasesgestor.GestorPago;
import clasesgestor.GestorReportes;
import clasesmodelo.Cliente;
import clasesmodelo.Prestamo;
import clasesmodelo.Cuota;
import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Clase principal que contiene el punto de entrada de la aplicación (`main`).
 * Es responsable de inicializar todos los gestores del sistema y de manejar
 * la interfaz de usuario a través de la consola.
 *
 * @author Tu Nombre
 * @version 1.2
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static GestorCliente CLIENTES;
    private static GestorPrestamo GESTOR_PRESTAMO;
    private static GestorPago GESTOR_PAGO;
    private static GestorReportes GESTOR_REPORTES;

    /**
     * Punto de entrada principal del programa.
     * Llama a la inicialización de los gestores y luego muestra el menú principal.
     * @param args Argumentos de la línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        inicializarGestores();
        mostrarMenuPrincipal();
    }

    /**
     * Configura e inicializa todas las clases gestoras en el orden de dependencia correcto.
     * Carga los datos existentes desde los archivos CSV para restaurar el estado del sistema.
     */
    private static void inicializarGestores() {
        GESTOR_PAGO = new GestorPago();
        GESTOR_PRESTAMO = new GestorPrestamo(GESTOR_PAGO);
        CLIENTES = new GestorCliente(GESTOR_PRESTAMO);
        GESTOR_REPORTES = new GestorReportes();

        CLIENTES.cargarDesdeArchivoCSV("data/clientes.csv");
        GESTOR_PRESTAMO.aplicarPagosCargadosACuotas();
        GESTOR_PRESTAMO.verificarYAplicarMoraATodosLosPrestamos(LocalDate.now());

        System.out.println("Sistema inicializado. ¡Bienvenido!");
    }

    /**
     * Muestra el menú principal de la aplicación y gestiona la navegación
     * a los diferentes submenús según la elección del usuario.
     */
    private static void mostrarMenuPrincipal() {
        int opcion;
        do {
            System.out.println("\n--- Menú Principal ---");
            System.out.println("1. Gestión de Clientes");
            System.out.println("2. Gestión de Préstamos");
            System.out.println("3. Registrar Pago de Cuota");
            System.out.println("4. Ver Detalles de Préstamos (con Cuotas)");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = obtenerOpcion();

            switch (opcion) {
                case 1: menuClientes(); break;
                case 2: menuPrestamos(); break;
                case 3: registrarPago(); break;
                case 4: listarPrestamosConCuotas(); break;
                case 0: guardarYSalir(); break;
                default: System.out.println("Opción inválida. Intente de nuevo.");
            }
        } while (opcion != 0);
    }

    /**
     * Muestra el submenú de gestión de clientes y maneja sus opciones.
     */
    private static void menuClientes() {
        int opcion;
        do {
            System.out.println("\n--- Gestión de Clientes ---");
            System.out.println("1. Crear Cliente");
            System.out.println("2. Buscar Cliente (Ver Detalle)");
            System.out.println("3. Editar Cliente");
            System.out.println("4. Eliminar Cliente");
            System.out.println("5. Listar Todos los Clientes");
            System.out.println("6. Generar Reporte PDF de Cliente");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");
            opcion = obtenerOpcion();
            switch (opcion) {
                case 1: crearCliente(); break;
                case 2: consultarClienteDetalle(); break;
                case 3: editarCliente(); break;
                case 4: eliminarCliente(); break;
                case 5: listarClientes(); break;
                case 6: generarReporteCliente(); break;
                case 0: System.out.println("Volviendo al Menú Principal..."); break;
                default: System.out.println("Opción inválida. Intente de nuevo.");
            }
        } while (opcion != 0);
    }

    /**
     * Gestiona el flujo de la consola para la creación de un nuevo cliente.
     */
    private static void crearCliente() {
        System.out.print("DNI: ");
        String dni = scanner.nextLine();
        if (CLIENTES.verificarExistenciaCliente(dni)) {
            System.out.println("Error: Ya existe un cliente con este DNI.");
            return;
        } if (!validarDNI(dni)) {
            System.out.println("Dato no válido. El DNI debe contener 7 u 8 dígitos numéricos. Registro Cancelado.");
            return;
        }
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Apellido: ");
        String apellido = scanner.nextLine();
        System.out.print("Dirección: ");
        String direccion = scanner.nextLine();
        System.out.print("Teléfono: ");
        String telefono = scanner.nextLine();
        System.out.print("Correo Electrónico: ");
        String correo = scanner.nextLine();

        CLIENTES.crearCliente(dni, nombre, apellido, direccion, telefono, correo);
    }

    /**
     * Gestiona la consulta de un cliente por DNI, mostrando sus detalles.
     */
    private static void consultarClienteDetalle() {
        System.out.print("DNI del cliente a buscar/consultar: ");
        String dni = scanner.nextLine();
        CLIENTES.consultarCliente(dni);
    }

    /**
     * Gestiona la edición de los datos de contacto de un cliente.
     */
    private static void editarCliente() {
        System.out.print("DNI del cliente a editar: ");
        String dni = scanner.nextLine();
        if (!CLIENTES.verificarExistenciaCliente(dni)) {
            System.out.println("Error: Cliente no encontrado.");
            return;
        }

        System.out.print("Nueva dirección (dejar vacío para no cambiar): ");
        String direccion = scanner.nextLine();
        System.out.print("Nuevo teléfono (dejar vacío para no cambiar): ");
        String telefono = scanner.nextLine();
        System.out.print("Nuevo correo Electrónico (dejar vacío para no cambiar): ");
        String correo = scanner.nextLine();
        CLIENTES.editarCliente(dni, direccion, telefono, correo);
    }

    /**
     * Gestiona la eliminación de un cliente.
     */
    private static void eliminarCliente() {
        System.out.print("DNI del cliente a eliminar: ");
        String dni = scanner.nextLine();
        CLIENTES.eliminarCliente(dni);
    }

    /**
     * Muestra una lista de todos los clientes registrados.
     */
    private static void listarClientes() {
        List<Cliente> clientes = CLIENTES.listarClientes();
        if (clientes.isEmpty()) {
            return;
        }
        for (int i = 0; i < clientes.size(); i++) {
            System.out.println(clientes.get(i));
            GESTOR_PRESTAMO.obtenerPrestamosPorCliente(clientes.get(i).getdni())
                    .forEach(p -> p.verificarMoraDeCuotas(LocalDate.now()));
            if (i < clientes.size() - 1) {
                System.out.println("----------------------------------------");
            }
        }
        System.out.println("----------------------------------------");
    }

    /**
     * Muestra el submenú de gestión de préstamos y maneja sus opciones.
     */
    private static void menuPrestamos() {
        int opcion;
        do {
            System.out.println("\n--- Gestión de Préstamos ---");
            System.out.println("1. Crear Préstamo");
            System.out.println("2. Listar Préstamos (resumen)");
            System.out.println("3. Eliminar Préstamo");
            System.out.println("4. Buscar Préstamo por ID");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");
            opcion = obtenerOpcion();

            switch (opcion) {
                case 1: crearPrestamo(); break;
                case 2: listarPrestamos(); break;
                case 3: eliminarPrestamo(); break;
                case 4: consultarPrestamoPorId(); break;
                case 0: System.out.println("Volviendo al Menú Principal..."); break;
                default: System.out.println("Opción inválida. Intente de nuevo.");
            }
        } while (opcion != 0);
    }

    /**
     * Gestiona el flujo de la consola para la creación de un nuevo préstamo.
     */
    private static void crearPrestamo() {
        System.out.print("DNI del cliente: ");
        String dni = scanner.nextLine();

        if (!CLIENTES.verificarExistenciaCliente(dni)) {
            System.out.println("Error: Cliente con DNI " + dni + " no registrado.");
            return;
        }

        double monto = 0;
        try {
            System.out.print("Monto: $");
            monto = Double.parseDouble(scanner.nextLine());
            if (monto <= 0) {
                System.out.println("Error: El monto debe ser un número positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para el monto. Por favor, ingrese un número.");
            return;
        }

        int cuotas = 0;
        try {
            System.out.print("Número de cuotas: ");
            cuotas = Integer.parseInt(scanner.nextLine());
            if (cuotas <= 0) {
                System.out.println("Error: El número de cuotas debe ser un entero positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para el número de cuotas. Por favor, ingrese un entero.");
            return;
        }

        System.out.print("¿Es hipotecario? (s/n): ");
        boolean esHipotecario = scanner.nextLine().equalsIgnoreCase("s");
        LocalDate fechaInicio = LocalDate.now();

        if (GESTOR_PRESTAMO.crearPrestamo(dni, monto, cuotas, esHipotecario, fechaInicio)) {
            Prestamo ultimoPrestamo = GESTOR_PRESTAMO.obtenerUltimoPrestamo();
            if (ultimoPrestamo != null) {
                System.out.println("Detalles del préstamo recién creado:");
                dibujarTablaPrestamos(List.of(ultimoPrestamo));
            }
        } else {
            System.out.println("Error: No se pudo crear el préstamo.");
        }
    }

    /**
     * Muestra un resumen de todos los préstamos registrados en formato de tabla.
     */
    private static void listarPrestamos() {
        GESTOR_PRESTAMO.verificarYAplicarMoraATodosLosPrestamos(LocalDate.now());
        List<Prestamo> prestamos = GESTOR_PRESTAMO.listarPrestamos();
        if (prestamos.isEmpty()) {
            System.out.println("No hay préstamos registrados.");
            return;
        }
        System.out.println("\n--- Listado de Préstamos (Resumen) ---");
        dibujarTablaPrestamos(prestamos);
    }

    /**
     * Busca un préstamo por su ID y muestra sus detalles y su plan de cuotas.
     */
    private static void consultarPrestamoPorId() {
        System.out.print("Ingrese el ID del préstamo a buscar: ");
        String idPrestamo = scanner.nextLine();

        Prestamo prestamo = GESTOR_PRESTAMO.obtenerPrestamo(idPrestamo);
        if (prestamo == null) {
            System.out.println("Error: Préstamo con ID '" + idPrestamo + "' no encontrado.");
            return;
        }
        prestamo.verificarMoraDeCuotas(LocalDate.now());

        System.out.println("\n--- Detalles del Préstamo: " + prestamo.getIdPrestamo() + " ---");
        dibujarTablaPrestamos(List.of(prestamo));
        System.out.println("\n--- Cuotas del Préstamo: " + prestamo.getIdPrestamo() + " ---");
        dibujarTablaCuotas(prestamo.getCuotas());
    }

    /**
     * Muestra un listado completo de todos los préstamos con el detalle de cada una de sus cuotas.
     */
    private static void listarPrestamosConCuotas() {
        GESTOR_PRESTAMO.verificarYAplicarMoraATodosLosPrestamos(LocalDate.now());
        List<Prestamo> prestamos = GESTOR_PRESTAMO.listarPrestamos();
        if (prestamos.isEmpty()) {
            System.out.println("No hay préstamos registrados para mostrar con detalles de cuotas.");
            return;
        }
        System.out.println("\n--- Listado Completo de Préstamos con Cuotas ---");
        for (Prestamo prestamo : prestamos) {
            System.out.println("\n" + prestamo.getIdPrestamo() + " - DNI: " + prestamo.getDniCliente() + " (Monto: " + prestamo.getMontoFormateado() + ")");
            System.out.println("  Saldo Pendiente Total: " + NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(prestamo.calcularSaldoPendienteTotal()));
            System.out.println("  Cuotas en Mora: " + prestamo.getCuotasEnMora().size() + (prestamo.getTotalPenalidadesAcumuladas() > 0 ? " (Penalidades Acumuladas: " + NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(prestamo.getTotalPenalidadesAcumuladas()) + ")" : ""));
            System.out.println("  --------------------------------------------------");
            dibujarTablaCuotas(prestamo.getCuotas());
            System.out.println("======================================================================");
        }
    }

    /**
     * Gestiona la eliminación de un préstamo por su ID.
     */
    private static void eliminarPrestamo() {
        System.out.print("ID del préstamo a eliminar: ");
        String idPrestamo = scanner.nextLine();
        GESTOR_PRESTAMO.eliminarPrestamo(idPrestamo);
    }

    /**
     * Gestiona el flujo de la consola para registrar un pago en una cuota.
     * Incluye una lógica para manejar pagos excedentes y aplicarlos a cuotas futuras.
     */
    private static void registrarPago() {
        System.out.print("ID del préstamo a registrar pago: ");
        String idPrestamoPago = scanner.nextLine();

        Prestamo prestamo = GESTOR_PRESTAMO.obtenerPrestamo(idPrestamoPago);
        if (prestamo == null) {
            System.out.println("Error: Préstamo con ID " + idPrestamoPago + " no encontrado.");
            return;
        }
        prestamo.verificarMoraDeCuotas(LocalDate.now());

        System.out.println("\n--- Cuotas del Préstamo " + idPrestamoPago + " ---");
        dibujarTablaCuotas(prestamo.getCuotas());
        System.out.println("----------------------------------------");


        int numCuota = 0;
        try {
            System.out.print("Número de cuota a pagar: ");
            numCuota = Integer.parseInt(scanner.nextLine());
            if (numCuota <= 0 || numCuota > prestamo.getNumeroCuotas()) {
                System.out.println("Error: El número de cuota debe estar dentro del rango válido (1 a " + prestamo.getNumeroCuotas() + ").");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para el número de cuota. Por favor, ingrese un entero.");
            return;
        }

        Cuota cuotaActual = prestamo.getCuotas().get(numCuota - 1);
        if (cuotaActual.getEstado() == Cuota.EstadoCuota.PAGADA) {
            System.out.println("Error: La cuota #" + numCuota + " ya está completamente pagada.");
            return;
        }

        double montoAPagarDeCuota = cuotaActual.getSaldoPendiente();
        double montoPagadoInput = 0;
        try {
            System.out.print("Monto a pagar para la cuota #" + numCuota + " (Saldo pendiente: " + cuotaActual.getSaldoPendienteFormateado() + "): $");
            montoPagadoInput = Double.parseDouble(scanner.nextLine());
            if (montoPagadoInput <= 0) {
                System.out.println("Error: El monto a pagar debe ser un número positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para el monto pagado. Por favor, ingrese un número.");
            return;
        }

        double excedente = 0;
        if (montoPagadoInput > montoAPagarDeCuota) {
            excedente = montoPagadoInput - montoAPagarDeCuota;
            System.out.println("Se detectó un excedente de pago de: " + NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(excedente));
            if (GESTOR_PRESTAMO.registrarPago(idPrestamoPago, numCuota, montoAPagarDeCuota)) {
                System.out.println("Cuota #" + numCuota + " saldada.");
            } else {
                System.out.println("Error al saldar la cuota #" + numCuota + " con el monto exacto.");
                return;
            }
        } else {
            if (!GESTOR_PRESTAMO.registrarPago(idPrestamoPago, numCuota, montoPagadoInput)) {
                System.out.println("No se pudo registrar el pago. Verifique los datos o el estado de la cuota.");
                return;
            }
        }

        if (excedente > 0) {
            System.out.println("Aplicando excedente a cuotas futuras...");
            for (int i = numCuota; i < prestamo.getCuotas().size() && excedente > 0; i++) {
                Cuota siguienteCuota = prestamo.getCuotas().get(i);
                if (siguienteCuota.getEstado() != Cuota.EstadoCuota.PAGADA) {
                    double saldoPendienteSiguiente = siguienteCuota.getSaldoPendiente();
                    if (excedente >= saldoPendienteSiguiente) {
                        GESTOR_PRESTAMO.registrarPago(idPrestamoPago, siguienteCuota.getNumeroCuota(), saldoPendienteSiguiente);
                        excedente -= saldoPendienteSiguiente;
                        System.out.println("  Cuota #" + siguienteCuota.getNumeroCuota() + " pagada con excedente. Restante del excedente: " + NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(excedente));
                    } else {
                        GESTOR_PRESTAMO.registrarPago(idPrestamoPago, siguienteCuota.getNumeroCuota(), excedente);
                        System.out.println("  Se aplicó " + NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(excedente) + " a la cuota #" + siguienteCuota.getNumeroCuota() + ". Cuota ahora en estado: " + siguienteCuota.getEstado());
                        excedente = 0;
                    }
                }
            }
            if (excedente > 0) {
                System.out.println("Advertencia: Quedó un excedente de " + NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(excedente) + " que no pudo aplicarse a ninguna cuota futura.");
            }
        }
    }

    /**
     * Guarda el estado de todos los gestores en sus respectivos archivos CSV y cierra la aplicación.
     */
    private static void guardarYSalir() {
        System.out.println("\nGuardando datos...");
        CLIENTES.guardarEnArchivoCSV("data/clientes.csv");
        GESTOR_PRESTAMO.guardarEnArchivoCSV("data/prestamos.csv");
        System.out.println("Datos guardados exitosamente. ¡Hasta pronto!");
        scanner.close();
    }

    /**
     * Helper para capturar y validar la entrada numérica del usuario para los menús.
     * @return la opción numérica ingresada por el usuario, o -1 si la entrada no es válida.
     */
    private static int obtenerOpcion() {
        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Entrada inválida. Por favor, ingrese un número.");
            return -1;
        } finally {
            scanner.nextLine(); // Limpiar el buffer del scanner
        }
    }

    /**
     * Valida que el formato del DNI sea correcto (7 u 8 dígitos numéricos).
     * @param dni El DNI a validar.
     * @return {@code true} si el formato es válido, {@code false} en caso contrario.
     */
    private static boolean validarDNI(String dni) {
        // La expresión regular \\d{7,8} verifica que el String contenga exactamente 7 u 8 dígitos.
        return dni != null && dni.matches("\\d{7,8}");
    }

    // --- Métodos de Ayuda para Dibujar Tablas ---

    /**
     * Dibuja en la consola una tabla formateada con los datos de los préstamos.
     * @param prestamos La lista de préstamos a mostrar.
     */
    private static void dibujarTablaPrestamos(List<Prestamo> prestamos) {
        String[] headers = {"ID Préstamo", "DNI Cliente", "Monto", "Cuotas", "Tipo", "Fecha Inicio", "Saldo Pendiente", "Cuotas Mora", "Penalidades"};
        int[] widths = {13, 13, 15, 8, 12, 14, 18, 12, 15};

        imprimirLineaSeparadora(widths);
        imprimirFila(headers, widths, false, new boolean[headers.length]);
        imprimirLineaSeparadora(widths);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        boolean[] alignRight = {false, false, true, true, false, false, true, true, true};
        for (Prestamo p : prestamos) {
            String[] rowData = {
                    p.getIdPrestamo(),
                    p.getDniCliente(),
                    currencyFormat.format(p.getMonto()),
                    String.valueOf(p.getNumeroCuotas()),
                    p.getTipoPrestamoString(),
                    p.getFechaInicio().toString(),
                    currencyFormat.format(p.calcularSaldoPendienteTotal()),
                    String.valueOf(p.getCuotasEnMora().size()),
                    currencyFormat.format(p.getTotalPenalidadesAcumuladas())
            };
            imprimirFila(rowData, widths, false, alignRight);
        }
        imprimirLineaSeparadora(widths);
    }

    /**
     * Dibuja en la consola una tabla formateada con los datos de las cuotas de un préstamo.
     * @param cuotas La lista de cuotas a mostrar.
     */
    private static void dibujarTablaCuotas(List<Cuota> cuotas) {
        String[] headers = {"Num", "Monto Original", "Monto Pagado", "Saldo Pendiente", "Penalidad", "Vencimiento", "Estado"};
        int[] widths = {5, 17, 17, 18, 13, 14, 15};

        imprimirLineaSeparadora(widths);
        imprimirFila(headers, widths, false, new boolean[headers.length]);
        imprimirLineaSeparadora(widths);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        boolean[] alignRight = {true, true, true, true, true, false, false};
        for (Cuota c : cuotas) {
            String penalidadStr = (c.getMontoPenalidadAcumulada() > 0) ? currencyFormat.format(c.getMontoPenalidadAcumulada()) : "-";
            String[] rowData = {
                    String.valueOf(c.getNumeroCuota()),
                    currencyFormat.format(c.getMontoOriginal()),
                    currencyFormat.format(c.getMontoPagado()),
                    currencyFormat.format(c.getSaldoPendiente()),
                    penalidadStr,
                    c.getFechaVencimiento().toString(),
                    c.getEstado().name()
            };
            imprimirFila(rowData, widths, false, alignRight);
        }
        imprimirLineaSeparadora(widths);
    }

    /**
     * Imprime una fila de una tabla en la consola con el formato y alineación especificados.
     * @param data Array de Strings con los datos de cada celda.
     * @param widths Array de enteros con el ancho de cada columna.
     * @param alignAllRight Booleano obsoleto para alinear todo a la derecha.
     * @param alignRightSpecific Array booleano que indica qué columnas específicas alinear a la derecha.
     */
    private static void imprimirFila(String[] data, int[] widths, boolean alignAllRight, boolean[] alignRightSpecific) {
        StringBuilder row = new StringBuilder("|");
        for (int i = 0; i < data.length; i++) {
            String cell = data[i];
            int width = widths[i];
            boolean shouldAlignRight = (alignRightSpecific != null && i < alignRightSpecific.length && alignRightSpecific[i]);

            if (shouldAlignRight) {
                row.append(String.format(" %" + (width - 1) + "s |", cell));
            } else {
                row.append(String.format(" %-" + (width - 1) + "s |", cell));
            }
        }
        System.out.println(row.toString());
    }

    /**
     * Gestiona el flujo para generar un reporte PDF para un cliente específico.
     */
    private static void generarReporteCliente() {
        System.out.print("DNI del cliente para generar el reporte: ");
        String dni = scanner.nextLine();

        Cliente cliente = CLIENTES.obtenerCliente(dni);
        if (cliente == null) {
            System.out.println("Error: Cliente con DNI " + dni + " no encontrado.");
            return;
        }

        List<Prestamo> prestamos = GESTOR_PRESTAMO.obtenerPrestamosPorCliente(dni);
        prestamos.forEach(p -> p.verificarMoraDeCuotas(LocalDate.now()));
        GESTOR_REPORTES.generarReporteCliente(cliente, prestamos);
    }

    /**
     * Imprime una línea separadora para una tabla en la consola (ej: +-----+------+).
     * @param widths Array de enteros con el ancho de cada columna para dibujar la línea.
     */
    private static void imprimirLineaSeparadora(int[] widths) {
        StringBuilder line = new StringBuilder("+");
        for (int width : widths) {
            line.append("-".repeat(width));
            line.append("+");
        }
        System.out.println(line.toString());
    }
}