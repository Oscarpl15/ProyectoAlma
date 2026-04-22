package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.service.GraficasService;
import com.practicasalma.proyectoalma.service.GeneradorPdfService;
import com.practicasalma.proyectoalma.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraficasController {

    @FXML private StackPane rootGraficas;
    @FXML private ImageView imgFondoGraficas;

    @FXML private ComboBox<String> comboEntidad;
    @FXML private ComboBox<String> comboTipoDato;
    @FXML private ComboBox<String> comboAno;
    @FXML private ComboBox<String> comboTipoSocio;
    @FXML private ComboBox<String> comboPeriodicidad;
    @FXML private ComboBox<String> comboVisual;

    @FXML private VBox boxTipoSocio;
    @FXML private VBox boxPeriodicidad;

    @FXML private Label lblActualizacion;
    @FXML private Label lblResumen1;
    @FXML private Label lblResumen2;
    @FXML private Label lblResumen3;

    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final GraficasService graficasService = new GraficasService();
    private final GeneradorPdfService generadorPdfService = new GeneradorPdfService();

    @FXML
    public void initialize() {
        imgFondoGraficas.fitWidthProperty().bind(rootGraficas.widthProperty());
        imgFondoGraficas.fitHeightProperty().bind(rootGraficas.heightProperty());

        comboEntidad.setItems(FXCollections.observableArrayList("ALUMNOS", "SOCIOS"));
        comboVisual.setItems(FXCollections.observableArrayList("BARRAS", "CIRCULAR"));

        comboTipoSocio.setItems(FXCollections.observableArrayList("TODOS", "Física", "Empresa", "Asociación"));
        comboPeriodicidad.setItems(FXCollections.observableArrayList("TODAS", "Mensual", "Trimestral", "Anual", "Puntual"));
        comboVisual.setDisable(true);

        comboEntidad.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            configurarFiltrosSegunEntidad(newV);
            refrescarGraficas();
        });
        comboTipoDato.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> refrescarGraficas());
        comboAno.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> refrescarGraficas());
        comboTipoSocio.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> refrescarGraficas());
        comboPeriodicidad.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> refrescarGraficas());

        comboEntidad.getSelectionModel().select("ALUMNOS");
        lblActualizacion.setText("Alumnos: circular | Socios: barras");
    }

    private void configurarFiltrosSegunEntidad(String entidad) {
        boolean esSocios = "SOCIOS".equals(entidad);

        boxTipoSocio.setManaged(esSocios);
        boxTipoSocio.setVisible(esSocios);
        boxPeriodicidad.setManaged(esSocios);
        boxPeriodicidad.setVisible(esSocios);

        if (esSocios) {
            comboVisual.getSelectionModel().select("BARRAS");
            comboTipoDato.setItems(FXCollections.observableArrayList("INGRESOS_MES"));
            comboTipoDato.getSelectionModel().select("INGRESOS_MES");

            List<String> anos = graficasService.obtenerAnosSocios();
            comboAno.setItems(FXCollections.observableArrayList(anos));
            if (!anos.isEmpty()) {
                comboAno.getSelectionModel().select(0);
            }

            comboTipoSocio.getSelectionModel().select("TODOS");
            comboPeriodicidad.getSelectionModel().select("TODAS");
        } else {
            comboVisual.getSelectionModel().select("CIRCULAR");
            comboTipoDato.setItems(FXCollections.observableArrayList("NACIONALIDAD", "CURSO", "GENERO"));
            comboTipoDato.getSelectionModel().select("NACIONALIDAD");

            List<String> anos = graficasService.obtenerAnosAcademicosAlumnos();
            comboAno.setItems(FXCollections.observableArrayList(anos));
            if (!anos.isEmpty()) {
                comboAno.getSelectionModel().select(0);
            }
        }
    }

    private void refrescarGraficas() {
        String entidad = comboEntidad.getValue();
        String visual = "SOCIOS".equals(entidad) ? "BARRAS" : "CIRCULAR";

        GraficasService.ResultadoGrafica resultado;

        if ("SOCIOS".equals(entidad)) {
            resultado = graficasService.generarGraficaSocios(
                    comboAno.getValue(),
                    comboTipoSocio.getValue(),
                    comboPeriodicidad.getValue()
            );
        } else {
            resultado = graficasService.generarGraficaAlumnos(
                    comboTipoDato.getValue(),
                    comboAno.getValue()
            );
        }

        renderizar(resultado, visual);
        actualizarResumen(resultado.getResumen());
    }

    private void renderizar(GraficasService.ResultadoGrafica resultado, String visual) {
        if ("CIRCULAR".equals(visual)) {
            barChart.setVisible(false);
            barChart.setManaged(false);
            pieChart.setVisible(true);
            pieChart.setManaged(true);

            List<PieChart.Data> data = new ArrayList<>();
            for (Map.Entry<String, Number> e : resultado.getValores().entrySet()) {
                data.add(new PieChart.Data(e.getKey(), e.getValue().doubleValue()));
            }
            pieChart.setTitle(resultado.getTitulo());
            pieChart.setData(FXCollections.observableArrayList(data));
            return;
        }

        pieChart.setVisible(false);
        pieChart.setManaged(false);
        barChart.setVisible(true);
        barChart.setManaged(true);

        xAxis.setLabel("Categoria");
        yAxis.setLabel("Valor");
        barChart.setTitle(resultado.getTitulo());
        barChart.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Map.Entry<String, Number> e : resultado.getValores().entrySet()) {
            serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        barChart.getData().add(serie);
    }

    private void actualizarResumen(List<String> resumen) {
        lblResumen1.setText(resumen.size() > 0 ? resumen.get(0) : "-");
        lblResumen2.setText(resumen.size() > 1 ? resumen.get(1) : "-");
        lblResumen3.setText(resumen.size() > 2 ? resumen.get(2) : "-");
    }

    @FXML
    private void exportarGraficaPdf() {
        try {
            Chart graficaActiva = pieChart.isVisible() ? pieChart : barChart;
            if (graficaActiva == null || !graficaActiva.isVisible()) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Sin grafica", "No hay ninguna grafica visible para exportar.");
                return;
            }

            graficaActiva.applyCss();
            graficaActiva.layout();

            SnapshotParameters snapshotParameters = new SnapshotParameters();
            snapshotParameters.setFill(Color.WHITE);
            WritableImage graficaImagen = graficaActiva.snapshot(snapshotParameters, null);

            String titulo = obtenerTituloGrafica();
            List<String> resumen = new ArrayList<>();
            if (lblResumen1.getText() != null && !lblResumen1.getText().isBlank() && !"-".equals(lblResumen1.getText())) {
                resumen.add(lblResumen1.getText());
            }
            if (lblResumen2.getText() != null && !lblResumen2.getText().isBlank() && !"-".equals(lblResumen2.getText())) {
                resumen.add(lblResumen2.getText());
            }
            if (lblResumen3.getText() != null && !lblResumen3.getText().isBlank() && !"-".equals(lblResumen3.getText())) {
                resumen.add(lblResumen3.getText());
            }

            String marcaTiempo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            Path rutaSalida = Paths.get(System.getProperty("user.home"), "Desktop", "grafica_" + marcaTiempo + ".pdf");

            generadorPdfService.generarPdfGrafica(rutaSalida.toString(), titulo, graficaImagen, resumen);

            FxUtils.mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "PDF generado",
                    "Se ha generado el PDF de la grafica en: " + rutaSalida
            );
        } catch (Exception e) {
            FxUtils.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error al exportar",
                    "No se pudo exportar la grafica a PDF: " + e.getMessage()
            );
        }
    }

    private String obtenerTituloGrafica() {
        String titulo = pieChart.isVisible() ? pieChart.getTitle() : barChart.getTitle();
        if (titulo == null || titulo.isBlank()) {
            return "Grafica";
        }
        return titulo;
    }
}
