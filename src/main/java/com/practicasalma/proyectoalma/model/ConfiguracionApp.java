package com.practicasalma.proyectoalma.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que almacena la configuración de la aplicación en la base de datos.
 * <p>
 * Usa un modelo clave/valor simple. La clave es la clave primaria, por lo que
 * cada parámetro tiene exactamente un registro. Hibernate crea la tabla
 * automáticamente gracias a {@code hbm2ddl.auto=update}.
 * </p>
 * <p>
 * Parámetros que se guardan aquí (no en {@code ~/.alma/config.properties}):
 * grupos, correo.remitente, correo.password.app.
 * </p>
 */
@Entity
@Table(name = "configuracion")
public class ConfiguracionApp {

    @Id
    @Column(name = "clave", nullable = false)
    private String clave;

    @Column(name = "valor")
    private String valor;

    public ConfiguracionApp() {}

    public ConfiguracionApp(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public String getClave() { return clave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
