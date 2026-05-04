package com.practicasalma.proyectoalma.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controlador del diálogo de confirmación del envío anual de certificados.
 * <p>
 * Muestra la lista de socios que recibirán su certificado de donaciones,
 * con el importe total de cada uno. La directora confirma que es consciente
 * del envío antes de que se realice.
 * </p>
 */
public class ConfirmacionCertificadosController {

    @FXML private Label lblSubtitulo;
    @FXML private ListView<String> listaConfirmacion;

    private boolean confirmado = false;

    /**
     * Carga los datos a mostrar en el diálogo.
     *
     * @param subtitulo texto informativo bajo el título principal
     * @param lineas    lista de cadenas con formato "Nombre  —  XX.XX €"
     */
    public void setDatos(String subtitulo, List<String> lineas) {
        if (subtitulo != null) lblSubtitulo.setText(subtitulo);
        listaConfirmacion.getItems().setAll(lineas);
    }

    /** @return {@code true} si el usuario pulsó Confirmar */
    public boolean isConfirmado() {
        return confirmado;
    }

    @FXML
    private void confirmar() {
        confirmado = true;
        ((Stage) listaConfirmacion.getScene().getWindow()).close();
    }
}
