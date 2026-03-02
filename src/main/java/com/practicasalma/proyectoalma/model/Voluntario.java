package com.practicasalma.proyectoalma.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "voluntarios")
public class Voluntario extends Persona {

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    // Certificado de Delitos Sexuales
    @Column(name = "cert_delitos_sexuales", nullable = false)
    private Boolean autoDelitosSexuales = false;

    @Column(name = "ruta_doc_delitos")
    private String rutaDocDelitosSexuales;

    // Ley de Protección de Datos
    @Column(name = "cert_proteccion_datos", nullable = false)
    private Boolean autoProteccionDatos = false;

    @Column(name = "ruta_doc_leypd")
    private String rutaDocProteccionDatos;


    // Constructor vacío obligatorio para Hibernate
    public Voluntario() {
        super();
    }

    // Constructor para instanciar desde el controlador JavaFX
    public Voluntario(String nombre, String apellidos, String direccion, String dni, String telefono, String correo, LocalDate fechaNacimiento) {
        super(nombre, apellidos, direccion);
        this.setDni(dni);
        this.setTelefono(telefono);
        this.setCorreo(correo);
        this.fechaNacimiento = fechaNacimiento;
    }

    // Getters y Setters
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Boolean getAutoDelitosSexuales() {
        return autoDelitosSexuales;
    }

    public void setAutoDelitosSexuales(Boolean autoDelitosSexuales) {
        this.autoDelitosSexuales = autoDelitosSexuales;
    }

    public String getRutaDocDelitosSexuales() {
        return rutaDocDelitosSexuales;
    }

    public void setRutaDocDelitosSexuales(String rutaDocDelitosSexuales) {
        this.rutaDocDelitosSexuales = rutaDocDelitosSexuales;
    }

    public Boolean getAutoProteccionDatos() {
        return autoProteccionDatos;
    }

    public void setAutoProteccionDatos(Boolean autoProteccionDatos) {
        this.autoProteccionDatos = autoProteccionDatos;
    }

    public String getRutaDocProteccionDatos() {
        return rutaDocProteccionDatos;
    }

    public void setRutaDocProteccionDatos(String rutaDocProteccionDatos) {
        this.rutaDocProteccionDatos = rutaDocProteccionDatos;
    }
}