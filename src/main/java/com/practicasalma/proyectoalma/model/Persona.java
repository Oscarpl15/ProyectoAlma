package com.practicasalma.proyectoalma.model;

//Aquí tendremos la clase base de la que heredaran el resto de clases, objetivo herencia y no duplicar codigo innecesario

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
public abstract class Persona {

    // Hibernate autoincrementará este ID en cada tabla hija
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Obligatorios
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(name = "documento_identidad", unique = true)
    private String documentoIdentidad; // Sirve para DNI, NIE o Pasaporte

    @Column(name = "tipo_documento")
    private String tipoDocumento; // "DNI", "NIE", "Pasaporte"

    private String direccion;
    private String ciudad;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    private String telefono;
    private String correo;

    // Usamos LocalDate para las fechas. Le damos nombre a la columna para que quede más limpio en SQLite
    @Column(name = "fecha_alta")
    private LocalDate fechaAlta;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    // Por defecto, al crear a alguien, está activo
    private Boolean activo = true;

    // Constructor vacío (vacio en argumentos de entrada) necesario 100% para que hibernate funcione
    public Persona() {
        this.fechaAlta = LocalDate.now(); // Autocompleta la fecha de hoy al instanciar
    }

    // Constructor con los campos obligatorios
    public Persona(String nombre, String apellidos, String direccion) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.fechaAlta = LocalDate.now();
    }

    // Getters y Setters

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public LocalDate getFechaAlta() { return fechaAlta; }

    public LocalDate getFechaBaja() { return fechaBaja; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getDocumentoIdentidad() {return documentoIdentidad;}
    public void setDocumentoIdentidad(String documentoIdentidad) {this.documentoIdentidad = documentoIdentidad;}

    public String getTipoDocumento() {return tipoDocumento;}
    public void setTipoDocumento(String tipoDocumento) {this.tipoDocumento = tipoDocumento;}

    public String getCiudad() {return ciudad;}
    public void setCiudad(String ciudad) {this.ciudad = ciudad;}

    public String getCodigoPostal() {return codigoPostal;}
    public void setCodigoPostal(String codigoPostal) {this.codigoPostal = codigoPostal;}

    @Column
    private String nacionalidad;

    @Column
    private String genero;

    public String getNacionalidad() {return nacionalidad;}
    public void setNacionalidad(String nacionalidad) {this.nacionalidad = nacionalidad;}

    public String getGenero() {return genero;}
    public void setGenero(String genero) {this.genero = genero;}
}