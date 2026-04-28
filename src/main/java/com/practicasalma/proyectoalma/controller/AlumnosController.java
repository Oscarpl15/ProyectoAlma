package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.service.AlumnoService;
import com.practicasalma.proyectoalma.service.GeneradorPdfService;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.filtro.AlumnoFiltro;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador JavaFX de la pestaña de alumnos ({@code alumnos-view.fxml}).
 * <p>
 * Gestiona la tabla principal con filtrado en tiempo real, acceso a la ficha de detalle
 * y las operaciones de alta/baja de alumnos.
 * </p>
 */
public class AlumnosController {

    @FXML private TableView<Alumno> tablaAlumnos;
    @FXML private TableColumn<Alumno, String> colNombre;
    @FXML private TableColumn<Alumno, String> colApellidos;
    @FXML private TableColumn<Alumno, String> colTelefono;
    @FXML private TableColumn<Alumno, String> colGrupo;

    @FXML private TableColumn<Alumno, Boolean> colAutoDatos;
    @FXML private TableColumn<Alumno, Boolean> colAutoActividades;
    @FXML private TableColumn<Alumno, Boolean> colAutoComunicaciones;
    @FXML private TableColumn<Alumno, Boolean> colAutoImagen;
    @FXML private TableColumn<Alumno, Boolean> colAutoIrseSolo;
    @FXML private TableColumn<Alumno, String> colEstado;

    @FXML private ComboBox<String> comboCursoFiltro;
    @FXML private ComboBox<String> comboCursoAlumnoFiltro;
    @FXML private ComboBox<String> comboGrupoFiltro;
    @FXML private CheckBox chkSeguimientoSS;
    @FXML private CheckBox chkSeguimientoSAF;
    @FXML private ComboBox<String> comboEstadoFiltro;
    @FXML private TextField txtBuscar;

    private final AlumnoService alumnoService = new AlumnoService();
    private final GeneradorPdfService generadorPdfService = new GeneradorPdfService();
    private final ObservableList<Alumno> listaMaestra = FXCollections.observableArrayList();
    private FilteredList<Alumno> listaFiltrada;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellidos()));
        colTelefono.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getTelefono() != null ? cellData.getValue().getTelefono() : ""));
        colGrupo.setCellValueFactory(cellData -> {
            Alumno alumno = cellData.getValue();
            if (alumno.getMatriculas() != null && !alumno.getMatriculas().isEmpty()) {
                String grupo = alumno.getMatriculas().get(0).getGrupoAsignado();
                return new SimpleStringProperty(grupo != null ? grupo : "Sin grupo");
            }
            return new SimpleStringProperty("Sin matricular");
        });
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(Boolean.TRUE.equals(cellData.getValue().getActivo()) ? "Activo" : "Inactivo"));
        colEstado.setCellFactory(FxUtils.celdaEstado());

        configurarColumnaBooleana(colAutoDatos, alumno -> alumno.getAutorizaUsoDatos());
        configurarColumnaBooleana(colAutoActividades, alumno -> alumno.getAutorizaActividades());
        configurarColumnaBooleana(colAutoComunicaciones, alumno -> alumno.getAutorizaComunicaciones());
        configurarColumnaBooleana(colAutoImagen, alumno -> alumno.getAutorizaImagen());
        configurarColumnaBooleana(colAutoIrseSolo, alumno -> alumno.getAutorizaIrseSolo());

        poblarCursoEscolar();
        poblarCursoAlumno();
        poblarGrupo();
        poblarEstado();

        listaFiltrada = new FilteredList<>(listaMaestra, a -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboCursoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboCursoAlumnoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboGrupoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        chkSeguimientoSS.selectedProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        chkSeguimientoSAF.selectedProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        comboEstadoFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        SortedList<Alumno> listaSortable = new SortedList<>(listaFiltrada);
        listaSortable.comparatorProperty().bind(tablaAlumnos.comparatorProperty());
        tablaAlumnos.setItems(listaSortable);

        tablaAlumnos.setRowFactory(tv -> {
            TableRow<Alumno> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirFichaAlumno(row.getItem());
                }
            });
            return row;
        });

        cargarAlumnosEnTabla();
    }

    private void poblarCursoEscolar() {
        LocalDate hoy = LocalDate.now();
        int anyoInicio = 2022;
        int anyoActual = (hoy.getMonthValue() >= 9) ? hoy.getYear() : hoy.getYear() - 1;

        ObservableList<String> cursos = FXCollections.observableArrayList();
        cursos.add("Todos");
        for (int anyo = anyoInicio; anyo <= anyoActual; anyo++) {
            cursos.add(anyo + "/" + (anyo + 1));
        }
        comboCursoFiltro.setItems(cursos);
        comboCursoFiltro.setValue(anyoActual + "/" + (anyoActual + 1));
    }

    private void poblarCursoAlumno() {
        comboCursoAlumnoFiltro.setItems(FXCollections.observableArrayList(
                "Todos",
                "1º Infantil", "2º Infantil", "3º Infantil",
                "1º Primaria", "2º Primaria", "3º Primaria",
                "4º Primaria", "5º Primaria", "6º Primaria"
        ));
        comboCursoAlumnoFiltro.setValue("Todos");
    }

    private void poblarGrupo() {
        comboGrupoFiltro.setItems(FXCollections.observableArrayList(
                "Todos",
                "g1 lunes/miercoles", "g2 lunes/miercoles",
                "g1 martes/jueves", "g2 martes/jueves"
        ));
        comboGrupoFiltro.setValue("Todos");
    }

    private void poblarEstado() {
        comboEstadoFiltro.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        comboEstadoFiltro.setValue("Todos");
    }

    private void aplicarFiltros() {
        listaFiltrada.setPredicate(AlumnoFiltro.construir(
                txtBuscar.getText(),
                comboCursoFiltro.getValue(),
                comboCursoAlumnoFiltro.getValue(),
                comboGrupoFiltro.getValue(),
                chkSeguimientoSS.isSelected(),
                chkSeguimientoSAF.isSelected(),
                comboEstadoFiltro.getValue()
        ));
    }

    private void configurarColumnaBooleana(TableColumn<Alumno, Boolean> columna,
                                           java.util.function.Function<Alumno, Boolean> extractor) {
        columna.setCellValueFactory(cellData -> {
            Boolean valor = extractor.apply(cellData.getValue());
            return new SimpleObjectProperty<>(valor != null ? valor : false);
        });
        columna.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("celda-booleana-true", "celda-booleana-false");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "✓" : "✗");
                    getStyleClass().add(item ? "celda-booleana-true" : "celda-booleana-false");
                }
            }
        });
    }

    public void cargarAlumnosEnTabla() {
        listaMaestra.setAll(alumnoService.obtenerTodos());
    }

    @FXML
    private void nuevoAlumno() {
        try {
            FxUtils.abrirModal("agregarAlumno-view.fxml", "Agregar alumno");
            cargarAlumnosEnTabla();
        } catch (IOException e) {
            FxUtils.mostrarAlerta(javafx.scene.control.Alert.AlertType.ERROR, "Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    private void exportarPdfGrupos() {
        List<Alumno> alumnosFiltrados = new ArrayList<>(listaFiltrada);
        if (alumnosFiltrados.isEmpty()) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", "No hay alumnos para generar el PDF.");
            return;
        }

        String cursoEscolar = comboCursoFiltro.getValue();
        if (cursoEscolar == null || "Todos".equalsIgnoreCase(cursoEscolar)) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING,
                    "Curso requerido",
                    "Selecciona un curso escolar para generar el PDF de grupos.");
            return;
        }

        alumnosFiltrados.removeIf(alumno -> !Boolean.TRUE.equals(alumno.getActivo()));
        if (alumnosFiltrados.isEmpty()) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", "No hay alumnos activos para generar el PDF.");
            return;
        }

        List<Alumno> alumnosCompletos = new ArrayList<>();
        for (Alumno alumno : alumnosFiltrados) {
            if (alumno == null || alumno.getId() == null) {
                continue;
            }
            alumnosCompletos.add(alumnoService.obtenerCompleto(alumno.getId()));
        }

        String rutaDocumentos = GestorConfig.getRutaDocumentos();
        if (rutaDocumentos == null || rutaDocumentos.isBlank()) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING,
                    "Directorio no configurado",
                    "Configura el directorio de documentos en Ajustes antes de generar PDFs.");
            return;
        }

        try {
            String marcaTiempo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            Path rutaSalida = Paths.get(rutaDocumentos, "Informes", "alumnos", "grupos_alumnos_" + marcaTiempo + ".pdf");
            generadorPdfService.generarPdfGruposAlumnos(rutaSalida.toString(), alumnosCompletos, "Listado de alumnos por grupos", cursoEscolar);

            FxUtils.mostrarAlerta(Alert.AlertType.INFORMATION,
                    "PDF generado",
                    "Se ha generado el PDF en: " + rutaSalida);
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo generar el PDF de grupos: " + e.getMessage());
        }
    }

    private void abrirFichaAlumno(Alumno alumnoTabla) {
        try {
            Alumno alumnoCompleto = alumnoService.obtenerCompleto(alumnoTabla.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/practicasalma/proyectoalma/view/ficha-alumno.fxml"));
            Parent root = loader.load();
            FichaAlumnoController controller = loader.getController();
            controller.setAlumno(alumnoCompleto);

            Stage stage = new Stage();
            stage.setTitle("Ficha del Alumno: " + alumnoCompleto.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaAlumnos.getScene().getWindow());
            stage.showAndWait();

            cargarAlumnosEnTabla();
        } catch (Exception e) {
            FxUtils.mostrarAlerta(javafx.scene.control.Alert.AlertType.ERROR, "Error", "No se pudo abrir la ficha: " + e.getMessage());
        }
    }
}
