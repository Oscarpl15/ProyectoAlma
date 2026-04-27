package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.service.SocioService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AgregarDonativoController {

    @FXML private TextField txtImporte;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> comboFormaPago;

    private Socio socio;
    private final SocioService socioService = new SocioService();

    @FXML
    public void initialize() {
        comboFormaPago.getItems().addAll("Domiciliación", "Transferencia", "Efectivo");
        dpFecha.setValue(LocalDate.now());
    }

    public void setSocio(Socio socio) {
        this.socio = socio;
        if (socio.getFormaPago() != null) {
            comboFormaPago.setValue(socio.getFormaPago());
        }
    }

    @FXML
    private void guardar(ActionEvent event) {
        String importeTexto = txtImporte.getText().trim();
        LocalDate fecha = dpFecha.getValue();

        if (importeTexto.isEmpty() || fecha == null) {
            mostrarError("El importe y la fecha son obligatorios.");
            return;
        }

        BigDecimal importe;
        try {
            importe = new BigDecimal(importeTexto.replace(",", "."));
            if (importe.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarError("El importe debe ser un número positivo.");
            return;
        }

        try {
            Donacion donacion = new Donacion(fecha, importe, "Puntual", socio);
            donacion.setFormaDonacion(comboFormaPago.getValue());
            socio.addDonacion(donacion);
            socioService.actualizarSocio(socio);
            cerrarVentana(event);
        } catch (Exception e) {
            mostrarError("No se pudo guardar el donativo: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar(ActionEvent event) {
        cerrarVentana(event);
    }

    private void cerrarVentana(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
