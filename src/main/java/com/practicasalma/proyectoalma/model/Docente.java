package com.practicasalma.proyectoalma.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Docente {

    //Esto sirve para que se genere una conexion y cuando se modifique el dato, se actualice en la tabla automaticamente.
    private String nombre;
    private String apellidos;
    private String direccion;
    private String telefono;
    private String dni;
    private String correo;

    public Docente() {}

    public Docente(String nombre, String apellidos, String direccion, String telefono, String dni, String correo) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.telefono = telefono;
        this.dni = dni;
        this.correo = correo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDni() {
        return dni;
    }

    public String getCorreo() {
        return correo;
    }

    // Getters normales si quieres...
}
