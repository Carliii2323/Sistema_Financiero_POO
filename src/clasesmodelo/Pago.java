package clasesmodelo;

import java.time.LocalDate;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Representa una transacción de pago única y atómica.
 * Se utiliza para registrar cada operación de pago en el sistema y persistirla en un archivo.
 * A diferencia de Cuota, que representa el estado de una deuda, Pago representa el evento del pago.
 *
 * @author Tu Nombre
 * @version 1.1
 */
public class Pago {
    private String idPrestamo;
    private int numeroCuota;
    private double montoPagado;
    private LocalDate fechaPago;

    /**
     * Construye una nueva transacción de Pago.
     *
     * @param idPrestamo El ID del préstamo al que corresponde el pago.
     * @param numeroCuota El número de la cuota a la que se imputa el pago.
     * @param montoPagado El monto de dinero transferido en esta transacción.
     * @param fechaPago La fecha en que se realizó el pago.
     */
    public Pago(String idPrestamo, int numeroCuota, double montoPagado, LocalDate fechaPago) {
        this.idPrestamo = idPrestamo;
        this.numeroCuota = numeroCuota;
        this.montoPagado = montoPagado;
        this.fechaPago = fechaPago;
    }

    // --- Getters ---

    /**
     * Obtiene el ID del préstamo asociado a este pago.
     * @return el ID del préstamo.
     */
    public String getIdPrestamo() {
        return idPrestamo;
    }

    /**
     * Obtiene el número de la cuota asociada a este pago.
     * @return el número de la cuota.
     */
    public int getNumeroCuota() {
        return numeroCuota;
    }

    /**
     * Obtiene el monto exacto de esta transacción de pago.
     * @return el monto pagado.
     */
    public double getMontoPagado() {
        return montoPagado;
    }

    /**
     * Obtiene la fecha en que se realizó el pago.
     * @return la fecha del pago.
     */
    public LocalDate getFechaPago() {
        return fechaPago;
    }

    /**
     * Formatea el monto pagado a un formato de moneda local (Argentina).
     * @return El monto pagado formateado como un String con el símbolo de moneda.
     */
    public String getMontoPagadoFormateado() {
        // Usa Locale para Argentina para formato de moneda (ej. $1.234,56)
        return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(montoPagado);
    }

    /**
     * Devuelve una representación en cadena de la transacción de pago.
     * @return un String con los detalles del pago.
     */
    @Override
    public String toString() {
        return "Pago {" +
                "ID Préstamo: '" + idPrestamo + '\'' +
                ", Cuota #: " + numeroCuota +
                ", Monto Pagado: " + getMontoPagadoFormateado() +
                ", Fecha Pago: " + fechaPago +
                '}';
    }
}