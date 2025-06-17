package clasesmodelo;

import java.time.LocalDate;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Modela una cuota individual de un préstamo. Contiene información sobre su monto,
 * estado (pendiente, pagada, etc.), fecha de vencimiento y penalidades por mora.
 *
 * @author Tu Nombre
 * @version 1.2
 */
public class Cuota {

    /**
     * Define los posibles estados de una cuota.
     */
    public enum EstadoCuota {
        /** La cuota está pendiente de pago y no ha vencido. */
        PENDIENTE,
        /** La cuota ha sido saldada en su totalidad. */
        PAGADA,
        /** La fecha de vencimiento ha pasado y la cuota no está pagada. */
        MORA,
        /** Se ha realizado un pago, pero no cubre el saldo total de la cuota. */
        PAGO_INCOMPLETO
    }

    /** Porcentaje de penalidad (5%) que se aplica sobre el monto original en caso de mora. */
    public static final double PORCENTAJE_PENALIDAD_MORA = 0.05;

    private String idPrestamo;
    private int numeroCuota;
    private double montoOriginal;
    private double montoPagado;
    private LocalDate fechaVencimiento;
    private EstadoCuota estado;
    private double montoPenalidadAcumulada;

    /**
     * Construye una nueva instancia de Cuota.
     *
     * @param idPrestamo El ID del préstamo al que pertenece.
     * @param numeroCuota El número secuencial de la cuota (ej. 1, 2, 3...).
     * @param montoOriginal El monto original a pagar para esta cuota.
     * @param fechaVencimiento La fecha límite para pagar sin incurrir en mora.
     */
    public Cuota(String idPrestamo, int numeroCuota, double montoOriginal, LocalDate fechaVencimiento) {
        this.idPrestamo = idPrestamo;
        this.numeroCuota = numeroCuota;
        this.montoOriginal = montoOriginal;
        this.fechaVencimiento = fechaVencimiento;
        this.montoPagado = 0.0;
        this.estado = EstadoCuota.PENDIENTE;
        this.montoPenalidadAcumulada = 0.0;
    }

    // --- Getters y Setters ---

    /** @return El ID del préstamo al que pertenece la cuota. */
    public String getIdPrestamo() { return idPrestamo; }
    /** @return El número de esta cuota dentro del plan de pagos. */
    public int getNumeroCuota() { return numeroCuota; }
    /** @return El monto original de la cuota, sin contar penalidades. */
    public double getMontoOriginal() { return montoOriginal; }
    /** @return El monto total que ha sido pagado para esta cuota. */
    public double getMontoPagado() { return montoPagado; }
    /** @return La fecha de vencimiento de la cuota. */
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    /** @return El estado actual de la cuota (PENDIENTE, PAGADA, etc.). */
    public EstadoCuota getEstado() { return estado; }
    /** @return El monto total de las penalidades acumuladas por mora. */
    public double getMontoPenalidadAcumulada() { return montoPenalidadAcumulada; }
    /** @param estado El nuevo estado para la cuota. */
    public void setEstado(EstadoCuota estado) { this.estado = estado; }
    /** @param montoPagado El nuevo valor para el monto pagado. */
    public void setMontoPagado(double montoPagado) { this.montoPagado = montoPagado; }

    /**
     * Calcula el saldo restante a pagar para esta cuota.
     * La fórmula es (monto original + penalidades) - monto pagado.
     *
     * @return El saldo pendiente de la cuota.
     */
    public double getSaldoPendiente() {
        return Math.max(0, montoOriginal + montoPenalidadAcumulada - montoPagado);
    }

    /**
     * Aplica un monto de pago a esta cuota.
     * Actualiza el monto pagado y el estado de la cuota (PAGO_INCOMPLETO o PAGADA).
     *
     * @param pago El monto a abonar.
     * @return {@code true} si el pago fue válido y se aplicó, {@code false} en caso contrario.
     */
    public boolean aplicarPago(double pago) {
        if (estado == EstadoCuota.PAGADA) {
            //System.out.println("La cuota #" + numeroCuota + " ya está completamente pagada.");
            return false;
        }

        double saldoAntes = getSaldoPendiente();
        if (pago <= 0) {
            //System.out.println("Error: El monto del pago debe ser positivo.");
            return false;
        }

        if (pago >= saldoAntes) {
            this.montoPagado += saldoAntes;
            this.estado = EstadoCuota.PAGADA;
            this.montoPenalidadAcumulada = 0.0; // Se salda la penalidad al pagar completamente
        } else {
            this.montoPagado += pago;
            // Solo cambia el estado a PAGO_INCOMPLETO si la cuota no estaba ya en MORA.
            // Si ya estaba en mora, debe permanecer en mora.
            if (this.estado != EstadoCuota.MORA) {
                this.estado = EstadoCuota.PAGO_INCOMPLETO;
            }
        }
        return true;
    }

    /**
     * Verifica si la cuota está vencida y, de ser así, cambia su estado a MORA
     * y aplica la penalidad correspondiente si es la primera vez que se detecta.
     *
     * @param fechaActual La fecha actual para comparar contra la fecha de vencimiento.
     */
    public void verificarYAplicarMora(LocalDate fechaActual) {
        if (this.estado != EstadoCuota.PAGADA && fechaActual.isAfter(this.fechaVencimiento)) {
            if (this.estado != EstadoCuota.MORA) {
                this.estado = EstadoCuota.MORA;
                double penalidadCalculada = montoOriginal * PORCENTAJE_PENALIDAD_MORA;
                this.montoPenalidadAcumulada += penalidadCalculada;
            }
        }
    }

    /** @return El monto original formateado como moneda local. */
    public String getMontoOriginalFormateado() {
        return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(montoOriginal);
    }

    /** @return El monto pagado formateado como moneda local. */
    public String getMontoPagadoFormateado() {
        return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(montoPagado);
    }

    /** @return El saldo pendiente formateado como moneda local. */
    public String getSaldoPendienteFormateado() {
        return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(getSaldoPendiente());
    }

    /** @return El monto de penalidad acumulada formateado como moneda local. */
    public String getMontoPenalidadAcumuladaFormateado() {
        return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(montoPenalidadAcumulada);
    }

    /**
     * Devuelve una representación en cadena del estado actual de la cuota.
     * @return un String con formato de los detalles de la cuota.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Cuota #").append(numeroCuota)
                .append(" | Monto Original: ").append(getMontoOriginalFormateado())
                .append(" | Monto Pagado: ").append(getMontoPagadoFormateado())
                .append(" | Saldo Pendiente: ").append(getSaldoPendienteFormateado());

        if (montoPenalidadAcumulada > 0) {
            sb.append(" | Penalidad Acumulada: ").append(getMontoPenalidadAcumuladaFormateado());
        }
        sb.append(" | Vencimiento: ").append(fechaVencimiento)
                .append(" | Estado: ").append(estado);
        return sb.toString();
    }
}