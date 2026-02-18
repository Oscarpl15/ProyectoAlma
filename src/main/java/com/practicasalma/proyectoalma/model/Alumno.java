package com.practicasalma.proyectoalma.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Alumno {

    //Esto sirve para que se genere una conexion y cuando se modifique el dato, se actualice en la tabla automaticamente.
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty apellidos = new SimpleStringProperty();
    // Añade DNI, Curso, etc.

    public Alumno(String nombre, String apellidos) {
        this.nombre.set(nombre);
        this.apellidos.set(apellidos);
    }

    public StringProperty nombreProperty() { return nombre; }
    public StringProperty apellidosProperty() { return apellidos; }
    // Getters normales si quieres...


    public String getNombre() {
        return nombre.get();
    }

    public String getApellidos() {
        return apellidos.get();
    }
}
