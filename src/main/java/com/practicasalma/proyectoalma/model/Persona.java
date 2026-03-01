package com.practicasalma.proyectoalma.model;

//Aquí tendremos la clase base de la que heredaran el resto de clases, objetivo herencia y no duplicar codigo innecesario

import jakarta.persistence.*;
import java.time.LocalDate;

@MappedSuperclass
public abstract class Persona {

    // Hibernate autoincrementará este ID en cada tabla hija
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Obligatorios
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    // Opcional, pero si existe, no puede haber dos DNIs iguales en la misma tabla
    @Column(unique = true)
    private String dni;

    private String direccion;
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

    public Integer getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

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
}