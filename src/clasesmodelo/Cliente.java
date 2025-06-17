package clasesmodelo;

/**
 * Representa a un cliente de la financiera.
 * Contiene todos los datos personales y de contacto del cliente.
 *
 * @author Grupo4
 * @version 1.1
 */
public class Cliente {
    private String dni;
    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;
    private String correoElectronico;

    /**
     * Construye una nueva instancia de Cliente.
     *
     * @param dni El Documento Nacional de Identidad del cliente.
     * @param nombre El nombre del cliente.
     * @param apellido El apellido del cliente.
     * @param direccion La dirección de residencia del cliente.
     * @param telefono El número de teléfono de contacto.
     * @param correoElectronico El correo electrónico de contacto.
     */
    public Cliente(String dni, String nombre, String apellido, String direccion, String telefono, String correoElectronico) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
    }

    // --- Getters ---

    /**
     * Obtiene el DNI del cliente.
     * @return el DNI del cliente.
     */
    public String getdni() { return dni; }

    /**
     * Obtiene el nombre del cliente.
     * @return el nombre del cliente.
     */
    public String getNombre() { return nombre; }

    /**
     * Obtiene el apellido del cliente.
     * @return el apellido del cliente.
     */
    public String getApellido() { return apellido; }

    /**
     * Obtiene la dirección del cliente.
     * @return la dirección del cliente.
     */
    public String getDireccion() { return direccion; }

    /**
     * Obtiene el teléfono del cliente.
     * @return el teléfono del cliente.
     */
    public String getTelefono() { return telefono; }

    /**
     * Obtiene el correo electrónico del cliente.
     * @return el correo electrónico del cliente.
     */
    public String getCorreoElectronico() { return correoElectronico; }

    // --- Setters para edición ---

    /**
     * Actualiza la dirección del cliente.
     * @param direccion la nueva dirección.
     */
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /**
     * Actualiza el teléfono del cliente.
     * @param telefono el nuevo teléfono.
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Actualiza el correo electrónico del cliente.
     * @param correoElectronico el nuevo correo electrónico.
     */
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    /**
     * Devuelve una representación en cadena de los datos del cliente.
     * @return un String con formato de los detalles del cliente.
     */
    @Override
    public String toString() {
        return "DNI: " + dni + "\n" +
                "Nombre: " + nombre + "\n" +
                "Apellido: " + apellido+ "\n" +
                "Dirección: " + direccion + "\n" +
                "Teléfono: " + telefono + "\n" +
                "Correo Electrónico: " + correoElectronico;
    }
}