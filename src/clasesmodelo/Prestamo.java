package clasesmodelo;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modela un préstamo otorgado a un cliente, incluyendo su monto, tasas,
 * y la lista de cuotas asociadas.
 *
 * @author Grupo4
 * @version 1.2
 */
public class Prestamo {

    private String idPrestamo;
    private String dniCliente;
    private double monto;
    private int numeroCuotas;
    private boolean esHipotecario;
    private LocalDate fechaInicio;
    private List<Cuota> cuotas;

    /** Tasa de interés fija para préstamos de tipo Personal. */
    public static final double TASA_PERSONAL = 15.5;
    /** Tasa de interés fija para préstamos de tipo Hipotecario. */
    public static final double TASA_HIPOTECARIO = 8.0;

    /**
     * Construye un nuevo Préstamo y genera automáticamente su plan de cuotas.
     *
     * @param idPrestamo El identificador único del préstamo.
     * @param dniCliente El DNI del cliente al que se le otorga el préstamo.
     * @param monto El monto total del capital prestado.
     * @param numeroCuotas El número de cuotas para devolver el préstamo.
     * @param esHipotecario {@code true} si es un préstamo hipotecario, {@code false} si es personal.
     * @param fechaInicio La fecha en que se otorga el préstamo.
     */
    public Prestamo(String idPrestamo, String dniCliente, double monto,
                    int numeroCuotas, boolean esHipotecario, LocalDate fechaInicio) {
        this.idPrestamo = idPrestamo;
        this.dniCliente = dniCliente;
        this.monto = monto;
        this.numeroCuotas = numeroCuotas;
        this.esHipotecario = esHipotecario;
        this.fechaInicio = fechaInicio;
        this.cuotas = new ArrayList<>();
        generarCuotas();
    }

    /**
     * Procesa todas las cuotas del préstamo para verificar si alguna ha entrado en mora
     * según la fecha actual y aplica las penalidades correspondientes.
     *
     * @param fechaActual La fecha contra la cual se compara el vencimiento de las cuotas.
     */
    public void verificarMoraDeCuotas(LocalDate fechaActual) {
        for (Cuota cuota : cuotas) {
            cuota.verificarYAplicarMora(fechaActual);
        }
    }

    /**
     * Calcula la deuda total pendiente del préstamo.
     * Suma el saldo pendiente de todas las cuotas no pagadas, incluyendo penalidades.
     *
     * @return El monto total del saldo pendiente.
     */
    public double calcularSaldoPendienteTotal() {
        return cuotas.stream()
                .filter(c -> c.getEstado() != Cuota.EstadoCuota.PAGADA)
                .mapToDouble(Cuota::getSaldoPendiente)
                .sum();
    }

    /**
     * Registra un monto de pago en una cuota específica.
     *
     * @param numeroCuota El número de la cuota a la que se aplicará el pago.
     * @param montoPagado El monto que se desea pagar.
     * @return {@code true} si el pago pudo ser aplicado, {@code false} en caso contrario.
     */
    public boolean registrarPagoEnCuota(int numeroCuota, double montoPagado) {
        if (numeroCuota <= 0 || numeroCuota > this.cuotas.size()) {
            System.err.println("Error: Número de cuota " + numeroCuota + " fuera de rango para el préstamo " + idPrestamo + ".");
            return false;
        }

        Cuota cuota = this.cuotas.get(numeroCuota - 1);
        return cuota.aplicarPago(montoPagado);
    }

    /**
     * Genera el plan de pagos completo, creando todas las instancias de Cuota
     * para este préstamo. Este método es privado y se llama desde el constructor.
     */
    private void generarCuotas() {
        double cuotaMensual = calcularCuotaMensual();
        LocalDate vencimientoActual = fechaInicio.plusMonths(1);

        for (int i = 1; i <= numeroCuotas; i++) {
            Cuota nuevaCuota = new Cuota(this.idPrestamo, i, cuotaMensual, vencimientoActual);
            this.cuotas.add(nuevaCuota);
            vencimientoActual = vencimientoActual.plusMonths(1);
        }
    }

    /**
     * Calcula el valor de la cuota mensual utilizando la fórmula del sistema de amortización francés.
     *
     * @return el valor de la cuota mensual calculada.
     */
    public double calcularCuotaMensual() {
        double tasaMensual = (getTasaInteres() / 100.0);
        int n = numeroCuotas;

        if (tasaMensual == 0) return monto / numeroCuotas;

        // Fórmula del sistema francés: C = (V * i) / (1 - (1 + i)^-n)
        // La implementacion usa una variante algebráicamente equivalente.
        double factor = Math.pow(1 + tasaMensual, n);
        return (monto * tasaMensual * factor) / (factor - 1);
    }

    // --- Getters ---

    /** @return el ID único del préstamo. */
    public String getIdPrestamo() { return idPrestamo; }
    /** @return el DNI del cliente asociado. */
    public String getDniCliente() { return dniCliente; }
    /** @return el monto original del préstamo. */
    public double getMonto() { return monto; }
    /** @return el número total de cuotas. */
    public int getNumeroCuotas() { return numeroCuotas; }
    /** @return {@code true} si el préstamo es hipotecario. */
    public boolean esHipotecario() { return esHipotecario; }
    /** @return la fecha de inicio del préstamo. */
    public LocalDate getFechaInicio() { return fechaInicio; }
    /** @return la tasa de interés aplicable según el tipo de préstamo. */
    public double getTasaInteres() { return esHipotecario ? TASA_HIPOTECARIO : TASA_PERSONAL; }
    /** @return una copia de la lista de cuotas para evitar modificaciones externas. */
    public List<Cuota> getCuotas() { return new ArrayList<>(this.cuotas); }
    /** @return una lista de las cuotas que se encuentran en estado de mora. */
    public List<Cuota> getCuotasEnMora() { return cuotas.stream().filter(c -> c.getEstado() == Cuota.EstadoCuota.MORA).collect(Collectors.toList()); }
    /** @return la suma de todas las penalidades acumuladas en las cuotas. */
    public double getTotalPenalidadesAcumuladas() { return cuotas.stream().mapToDouble(Cuota::getMontoPenalidadAcumulada).sum(); }
    /** @return el tipo de préstamo como un String ("Hipotecario" o "Personal"). */
    public String getTipoPrestamoString() { return esHipotecario ? "Hipotecario" : "Personal"; }
    /** @return el monto original del préstamo formateado como moneda. */
    public String getMontoFormateado() { return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(monto); }
    /** @return el valor de la cuota mensual formateado como moneda. */
    public String getCuotaMensualFormateada() { return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(calcularCuotaMensual()); }

    /**
     * Devuelve una representación textual detallada del préstamo y sus cuotas.
     * @return un String con formato de los detalles del préstamo.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Préstamo {\n");
        sb.append("  ID: '").append(idPrestamo).append("'\n");
        sb.append("  DNI Cliente: '").append(dniCliente).append("'\n");
        sb.append("  Monto Original: ").append(getMontoFormateado()).append("\n");
        sb.append("  Cuotas Totales: ").append(numeroCuotas).append("\n");
        sb.append("  Tipo: ").append(esHipotecario ? "Hipotecario" : "Personal").append("\n");
        sb.append("  Fecha Inicio: ").append(fechaInicio).append("\n");
        sb.append("  Cuota Mensual Teórica: ").append(getCuotaMensualFormateada()).append("\n");

        double saldoPendienteTotal = calcularSaldoPendienteTotal();
        List<Cuota> cuotasEnMora = getCuotasEnMora();
        double totalPenalidades = getTotalPenalidadesAcumuladas();


        sb.append("  --- Estado del Préstamo ---\n");
        sb.append("  Saldo Pendiente Total (incl. penalidades): ").append(NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(saldoPendienteTotal)).append("\n");
        sb.append("  Cuotas en Mora: ").append(cuotasEnMora.size()).append("\n");
        if (totalPenalidades > 0) {
            sb.append("  Penalidades Acumuladas: ").append(NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(totalPenalidades)).append("\n");
        }

        sb.append("  --- Cuotas Detalle ---\n");
        if (cuotas.isEmpty()) {
            sb.append("    No hay cuotas generadas aún.\n");
        } else {
            for (Cuota c : cuotas) {
                sb.append(c.toString()).append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}