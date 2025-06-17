package clasesgestor;

import clasesmodelo.Cliente;
import clasesmodelo.Prestamo;
import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Gestiona todas las operaciones relacionadas con los clientes de la financiera.
 * Se encarga de la creación, consulta, edición, eliminación y persistencia
 * de los datos de los clientes en un archivo CSV.
 *
 * @author Tu Nombre
 * @version 1.2
 */
public class GestorCliente {
    private final Map<String, Cliente> clientes;
    private final GestorPrestamo gestorPrestamos;
    private final String archivoClientes = "data/clientes.csv";
    private static final String CSV_HEADER = "DNI;Nombre;Apellido;Direccion;Telefono;Correo";

    /**
     * Constructor para el GestorCliente.
     * Recibe una instancia de GestorPrestamo para poder consultar los préstamos de un cliente.
     *
     * @param gestorPrestamos una instancia de GestorPrestamo para acceder a los préstamos del cliente.
     */
    public GestorCliente(GestorPrestamo gestorPrestamos) {
        this.clientes = new HashMap<>();
        this.gestorPrestamos = gestorPrestamos;
    }

    /**
     * Verifica si ya existe un cliente con el DNI proporcionado.
     *
     * @param dni El DNI a verificar.
     * @return {@code true} si el cliente existe, {@code false} en caso contrario.
     */
    public boolean verificarExistenciaCliente(String dni) {
        return clientes.containsKey(dni);
    }

    /**
     * Crea un nuevo cliente y lo guarda en el sistema.
     * Valida que el DNI no exista previamente y que el teléfono y correo sean válidos.
     *
     * @param dni El Documento Nacional de Identidad del cliente.
     * @param nombre El nombre del cliente.
     * @param apellido El apellido del cliente.
     * @param direccion La dirección de residencia del cliente.
     * @param telefono El número de teléfono de contacto.
     * @param correo El correo electrónico de contacto.
     * @return {@code true} si el cliente se creó con éxito, {@code false} si ocurrió un error.
     */
    public boolean crearCliente(String dni, String nombre, String apellido, String direccion,
                                String telefono, String correo) {


        if (!validarTelefono(telefono) || !validarCorreo(correo)) {
            System.out.println("Datos no válidos. Registro Cancelado.");
            return false;
        }
        if (clientes.containsKey(dni)) {
            System.out.println("Error: Ya existe un cliente con este DNI.");
            return false;
        }

        Cliente nuevo = new Cliente(dni, nombre, apellido, direccion, telefono, correo);
        clientes.put(dni, nuevo);
        guardarEnArchivoCSV(archivoClientes);
        System.out.println("Cliente " + nombre + " " + apellido + " creado exitosamente.");
        return true;
    }

    /**
     * Obtiene un objeto Cliente a partir de su DNI.
     *
     * @param dni El DNI del cliente a buscar.
     * @return el objeto Cliente si se encuentra, o {@code null} si no existe.
     */
    public Cliente obtenerCliente(String dni) {
        return clientes.get(dni);
    }

    /**
     * Muestra por consola la información detallada de un cliente, incluyendo una tabla con sus préstamos.
     *
     * @param dni El DNI del cliente a consultar.
     */
    public void consultarCliente(String dni) {
        Cliente cliente = obtenerCliente(dni);
        if (cliente == null) {
            System.out.println("Cliente no encontrado con DNI: " + dni);
            return;
        }

        System.out.println("\n--- INFORMACIÓN DEL CLIENTE ---");
        System.out.println(cliente);

        if (gestorPrestamos != null) {
            List<Prestamo> prestamosDelCliente = gestorPrestamos.obtenerPrestamosPorCliente(dni);
            if (!prestamosDelCliente.isEmpty()) {
                System.out.println("\n--- PRÉSTAMOS ASOCIADOS ---");
                prestamosDelCliente.forEach(p -> p.verificarMoraDeCuotas(LocalDate.now()));
                dibujarTablaPrestamos(prestamosDelCliente);
            } else {
                System.out.println("  - Sin préstamos asociados");
            }
        } else {
            System.err.println("Error interno: GestorPrestamos no inicializado. No se pueden mostrar préstamos.");
        }
    }

    /**
     * Edita los datos de contacto de un cliente existente.
     *
     * @param dni El DNI del cliente a editar.
     * @param nuevaDireccion La nueva dirección. Si es null o vacía, no se modifica.
     * @param nuevoTelefono El nuevo teléfono. Si es null o vacío, no se modifica.
     * @param nuevoCorreo El nuevo correo electrónico. Si es null o vacío, no se modifica.
     * @return {@code true} si se realizó al menos un cambio, {@code false} en caso contrario.
     */
    public boolean editarCliente(String dni, String nuevaDireccion,
                                 String nuevoTelefono, String nuevoCorreo) {
        Cliente cliente = obtenerCliente(dni);
        if (cliente == null) {
            System.out.println("Error: Cliente no encontrado.");
            return false;
        }

        boolean cambios = false;
        if (nuevaDireccion != null && !nuevaDireccion.isEmpty()) {
            cliente.setDireccion(nuevaDireccion);
            cambios = true;
        }

        if (nuevoTelefono != null && !nuevoTelefono.isEmpty()) {
            if (validarTelefono(nuevoTelefono)) {
                cliente.setTelefono(nuevoTelefono);
                cambios = true;
            } else {
                System.out.println("Advertencia: El nuevo teléfono '" + nuevoTelefono + "' no es válido. No se cambió.");
            }
        }

        if (nuevoCorreo != null && !nuevoCorreo.isEmpty()) {
            if (validarCorreo(nuevoCorreo)) {
                cliente.setCorreoElectronico(nuevoCorreo);
                cambios = true;
            } else {
                System.out.println("Advertencia: El nuevo correo electrónico '" + nuevoCorreo + "' no es válido. No se cambió.");
            }
        }

        if (cambios) {
            guardarEnArchivoCSV(archivoClientes);
            System.out.println("Cliente actualizado exitosamente.");
            return true;
        }

        System.out.println("No se realizaron cambios.");
        return false;
    }


    /**
     * Devuelve una lista con todos los clientes registrados en el sistema.
     *
     * @return una lista de objetos Cliente.
     */
    public List<Cliente> listarClientes() {
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return new ArrayList<>();
        }

        System.out.println("\n--- LISTADO DE CLIENTES ---");
        return new ArrayList<>(clientes.values());
    }


    /**
     * Elimina un cliente del sistema. La operación falla si el cliente tiene préstamos asociados.
     *
     * @param dni El DNI del cliente a eliminar.
     * @return {@code true} si el cliente fue eliminado con éxito, {@code false} si no se pudo eliminar.
     */
    public boolean eliminarCliente(String dni) {
        if ("00000000".equals(dni)) {
            System.out.println("Error: El cliente de prueba con DNI 00000000 no puede ser eliminado.");
            return false;
        }

        if (!clientes.containsKey(dni)) {
            System.out.println("Cliente con DNI " + dni + " no encontrado.");
            return false;
        }

        if (gestorPrestamos != null && !gestorPrestamos.obtenerPrestamosPorCliente(dni).isEmpty()) {
            System.out.println("Error: El cliente con DNI " + dni + " tiene préstamos asociados y no puede ser eliminado.");
            return false;
        }

        clientes.remove(dni);
        guardarEnArchivoCSV(archivoClientes);
        System.out.println("Cliente con DNI " + dni + " eliminado exitosamente.");
        return true;
    }


    /**
     * Carga los datos de los clientes desde un archivo CSV al mapa en memoria.
     *
     * @param nombreArchivo La ruta del archivo CSV de clientes.
     */
    public void cargarDesdeArchivoCSV(String nombreArchivo) {
        File archivo = new File(nombreArchivo);
        if (!archivo.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            br.readLine(); // Saltar la cabecera

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                if (datos.length == 6) {
                    clientes.put(datos[0], new Cliente(datos[0], datos[1], datos[2], datos[3], datos[4], datos[5]));
                } else {
                    System.err.println("Advertencia: Línea de cliente con formato incorrecto en CSV, se omite: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar clientes desde CSV: " + e.getMessage());
        }
    }

    /**
     * Guarda el estado actual de todos los clientes del mapa en memoria a un archivo CSV.
     *
     * @param nombreArchivo La ruta del archivo CSV donde se guardarán los datos.
     */
    public void guardarEnArchivoCSV(String nombreArchivo) {
        crearDirectorioSiNoExiste(nombreArchivo);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {
            bw.write(CSV_HEADER);
            bw.newLine();

            clientes.values().forEach(cliente -> {
                try {
                    bw.write(String.join(";",
                            cliente.getdni(),
                            cliente.getNombre(),
                            cliente.getApellido(),
                            cliente.getDireccion(),
                            cliente.getTelefono(),
                            cliente.getCorreoElectronico()
                    ));
                    bw.newLine();
                } catch (IOException e) {
                    System.err.println("Error al escribir cliente en CSV: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("Error al guardar CSV de clientes: " + e.getMessage());
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
     * Valida el formato de un número de teléfono usando una expresión regular.
     * @param telefono El teléfono a validar.
     * @return {@code true} si el formato es válido.
     */
    private boolean validarTelefono(String telefono) {
        return telefono != null && telefono.matches("[+]?\\d{7,15}");
    }

    /**
     * Valida el formato de un correo electrónico usando una expresión regular.
     * @param correo El correo a validar.
     * @return {@code true} si el formato es válido.
     */
    private boolean validarCorreo(String correo) {
        return correo != null &&
                Pattern.compile("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,6}$").matcher(correo).matches();
    }


    // --- Métodos de Ayuda para Dibujar Tablas ---

    /**
     * Dibuja una tabla en la consola con la información de los préstamos de un cliente.
     * @param prestamos La lista de préstamos a mostrar.
     */
    private void dibujarTablaPrestamos(List<Prestamo> prestamos) {
        String[] headers = {"ID Préstamo", "Monto", "Cuotas", "Tipo", "Fecha Inicio", "Saldo Pendiente", "Cuotas Mora", "Penalidades"};
        int[] widths = {13, 15, 8, 12, 14, 18, 12, 15};

        imprimirLineaSeparadora(widths);
        imprimirFila(headers, widths, false, new boolean[headers.length]);
        imprimirLineaSeparadora(widths);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        boolean[] alignRight = {false, true, true, false, false, true, true, true};
        for (Prestamo p : prestamos) {
            String[] rowData = {
                    p.getIdPrestamo(),
                    currencyFormat.format(p.getMonto()),
                    String.valueOf(p.getNumeroCuotas()),
                    p.getTipoPrestamoString(),
                    p.getFechaInicio().toString(),
                    currencyFormat.format(p.calcularSaldoPendienteTotal()),
                    String.valueOf(p.getCuotasEnMora().size()),
                    currencyFormat.format(p.getTotalPenalidadesAcumuladas())
            };
            imprimirFila(rowData, widths, true, alignRight);
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
    private void imprimirFila(String[] data, int[] widths, boolean alignAllRight, boolean[] alignRightSpecific) {
        StringBuilder row = new StringBuilder("|");
        for (int i = 0; i < data.length; i++) {
            String cell = data[i];
            int width = widths[i];
            boolean shouldAlignRight = alignAllRight || (alignRightSpecific != null && i < alignRightSpecific.length && alignRightSpecific[i]);

            if (shouldAlignRight) {
                row.append(String.format(" %" + (width - 1) + "s |", cell));
            } else {
                row.append(String.format(" %-" + (width - 1) + "s |", cell));
            }
        }
        System.out.println(row.toString());
    }

    /**
     * Imprime una línea separadora para una tabla en la consola (ej: +-----+------+).
     * @param widths Array de enteros con el ancho de cada columna para dibujar la línea.
     */
    private void imprimirLineaSeparadora(int[] widths) {
        StringBuilder line = new StringBuilder("+");
        for (int width : widths) {
            line.append("-".repeat(width));
            line.append("+");
        }
        System.out.println(line.toString());
    }
}