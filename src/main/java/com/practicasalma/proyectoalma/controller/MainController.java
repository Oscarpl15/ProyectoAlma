package com.practicasalma.proyectoalma.controller;


import com.practicasalma.proyectoalma.exception.ConfiguracionException;
import com.practicasalma.proyectoalma.service.GestorAsignaciones;
import com.practicasalma.proyectoalma.service.GestorCertificadosAnuales;
import com.practicasalma.proyectoalma.service.GestorCorreo;
import com.practicasalma.proyectoalma.service.GestorMatriculas;
import com.practicasalma.proyectoalma.util.config.GestorConfig;
import com.practicasalma.proyectoalma.util.doc.GestorDocumentos;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.practicasalma.proyectoalma.model.Socio;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador principal de la aplicación ({@code main-view.fxml}).
 * <p>
 * Gestiona la navegación por pestañas entre Alumnos, Docentes, Socios y Voluntarios,
 * y coordina los diálogos automáticos de inicio de curso (renovación de matrículas
 * y asignaciones de personal). También expone el menú de Ajustes para cambiar
 * el directorio de documentos y configurar credenciales de correo.
 * </p>
 */
public class MainController {

    @FXML private TabPane tabPrincipal;
    @FXML private StackPane rootPane;
    @FXML private ImageView imgFondo;
    @FXML private Button btnAjustes;

    // Sub-controladores inyectados automáticamente por JavaFX gracias al fx:id de fx:include
    @FXML private AlumnosController alumnosController;
    @FXML private DocentesController docentesController;
    @FXML private VoluntariosController voluntariosController;
    @FXML private GraficasController graficasController;

    @FXML private ContextMenu menuAjustes;

    @FXML
    public void initialize() {
        imgFondo.fitWidthProperty().bind(rootPane.widthProperty());
        imgFondo.fitHeightProperty().bind(rootPane.heightProperty());
        if (tabPrincipal != null) {
            tabPrincipal.getSelectionModel().select(0);
            tabPrincipal.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                if (newIdx.intValue() == 4) {
                    graficasController.refrescar();
                }
            });
        }

        Platform.runLater(() -> {
            // Aviso de credenciales no configuradas (a partir del 1 de diciembre)
            if (LocalDate.now().getMonthValue() == 12 && !GestorCorreo.estaConfigurado()) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING,
                        "Credenciales no configuradas",
                        "No hay credenciales de correo configuradas. Los certificados anuales se envían el 20 de enero. " +
                        "Configúralas en Ajustes → Gestor de credenciales de correo.");
            }

            // Envío automático de certificados anuales (≥ 20 enero, solo una vez por año)
            if (GestorCertificadosAnuales.esPeriodoEnvio() && !GestorCertificadosAnuales.yaEnviadoEsteAno()) {
                int anoAnterior = LocalDate.now().getYear() - 1;
                List<Socio> socios = GestorCertificadosAnuales.obtenerSociosConDonacionesEnAno(anoAnterior);
                if (!socios.isEmpty()) {
                    List<String> lineas = GestorCertificadosAnuales.construirLineasConfirmacion(socios, anoAnterior);
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                                "/com/practicasalma/proyectoalma/view/confirmacionCertificados-view.fxml"));
                        Parent root = loader.load();
                        ConfirmacionCertificadosController ctrl = loader.getController();
                        ctrl.setDatos("Donaciones del año " + anoAnterior + " — socios con donaciones registradas", lineas);
                        Stage stage = new Stage();
                        stage.setTitle("Certificados de donaciones " + anoAnterior);
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.initOwner(rootPane.getScene().getWindow());
                        stage.setScene(new Scene(root));
                        try { stage.getIcons().add(new Image(getClass().getResourceAsStream(
                                "/com/practicasalma/proyectoalma/assets/logo.png"))); } catch (Exception ignored) {}
                        stage.showAndWait();
                        if (ctrl.isConfirmado()) {
                            if (!GestorCorreo.estaConfigurado()) {
                                FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Sin credenciales",
                                        "No se han configurado las credenciales de correo. " +
                                        "Configúralas en Ajustes → Gestor de credenciales de correo.");
                                return;
                            }
                            GestorCertificadosAnuales.enviarCertificados(socios, anoAnterior);
                            GestorCertificadosAnuales.marcarEnviados();
                            FxUtils.mostrarAlerta(Alert.AlertType.INFORMATION, "Certificados enviados",
                                    "Se han enviado los certificados de donaciones del año " + anoAnterior + ".");
                        }
                    } catch (IOException e) {
                        FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error",
                                "No se pudo mostrar el diálogo de certificados: " + e.getMessage());
                    } catch (Exception e) {
                        FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error al enviar certificados",
                                "No se pudieron enviar todos los certificados: " + e.getMessage());
                    }
                }
            }
        });

        GestorMatriculas.DatosDialogo datosSexto = null;
        GestorAsignaciones.DatosPersonal datosPersonal = null;
        try {
            datosSexto = GestorMatriculas.ejecutar();
        } catch (ConfiguracionException e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error de configuración",
                    "No se pudo procesar las renovaciones de matrículas: " + e.getMessage());
        }
        try {
            datosPersonal = GestorAsignaciones.prepararDatos();
        } catch (ConfiguracionException e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error de configuración",
                    "No se pudo procesar las asignaciones del personal: " + e.getMessage());
        }
        if (datosSexto != null && !datosSexto.errores.isEmpty()) {
            FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Matrículas no renovadas",
                    "No se pudieron renovar automáticamente las matrículas de:\n" +
                    String.join("\n", datosSexto.errores));
        }
        if (datosSexto != null || datosPersonal != null) {
            RenovacionController.mostrar(datosSexto, datosPersonal);
            alumnosController.cargarAlumnosEnTabla();
            docentesController.cargarDocentesEnTabla();
            voluntariosController.cargarVoluntariosEnTabla();
        }
    }

    @FXML
    private void abrirMenuAjustes() {
        menuAjustes.show(btnAjustes, Side.BOTTOM, 0, 4);
        javafx.stage.Window window = btnAjustes.getScene().getWindow();
        menuAjustes.setX(window.getX() + window.getWidth() - menuAjustes.getWidth() - 15);
    }

    // --- MÉTODOS DE LOS BOTONES DEL MENÚ ---

    @FXML
    protected void mostrarAlumnos() {
        // Selecciona la primera pestaña (Índice 0)
        tabPrincipal.getSelectionModel().select(0);
    }

    @FXML
    protected void mostrarDocentes() {
        // Selecciona la segunda pestaña (Índice 1)
        tabPrincipal.getSelectionModel().select(1);
    }

    @FXML
    protected void mostrarVoluntarios() {
        // Selecciona la tercera pestaña (Índice 2)
        tabPrincipal.getSelectionModel().select(2);
    }

    @FXML
    protected void mostrarSocios() {
        // Selecciona la tercera pestaña (Índice 2)
        tabPrincipal.getSelectionModel().select(3);
    }

    @FXML
    protected void mostrarGraficas() {
        tabPrincipal.getSelectionModel().select(4);
    }

    @FXML
    private void cambiarDirectorioBBDD() {
        ConfiguracionInicialController.mostrarCambioBBDD();
        String nuevaRuta = GestorConfig.getRutaBBDD();
        if (nuevaRuta != null && !nuevaRuta.isBlank()) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Cambio pendiente");
            info.setHeaderText(null);
            info.setContentText("El cambio de base de datos se aplicará al reiniciar la aplicación.");
            info.showAndWait();
        }
    }

    @FXML
    private void cambiarDirectorioDocs() {
        String rutaActual = GestorConfig.getRutaDocumentos();
        File dirActual = (rutaActual != null && !rutaActual.isBlank()) ? new File(rutaActual) : null;

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar directorio de documentos");
        if (dirActual != null && dirActual.exists()) chooser.setInitialDirectory(dirActual);

        File dirNuevo = chooser.showDialog(btnAjustes.getScene().getWindow());
        if (dirNuevo == null) return;

        if (dirActual != null && GestorDocumentos.tieneContenido(dirActual)
                && !dirActual.getAbsolutePath().equals(dirNuevo.getAbsolutePath())) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Mover contenido");
            confirmacion.setHeaderText("Se encontró contenido en el directorio actual.");
            confirmacion.setContentText("¿Deseas mover todos los archivos al nuevo directorio?");
            confirmacion.getButtonTypes().setAll(
                    javafx.scene.control.ButtonType.YES,
                    javafx.scene.control.ButtonType.NO);
            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == javafx.scene.control.ButtonType.YES) {
                    GestorDocumentos.moverContenido(dirActual, dirNuevo);
                }
            });
        }

        GestorConfig.setRutaDocumentos(dirNuevo.getAbsolutePath());
        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("Directorio actualizado");
        ok.setHeaderText(null);
        ok.setContentText("Directorio de documentos actualizado correctamente.");
        ok.showAndWait();
    }

    @FXML
    private void gestionarGrupos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/practicasalma/proyectoalma/view/gestionarGrupos-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Gestionar Grupos");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            alumnosController.cargarAlumnosEnTabla();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la ventana de grupos: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void configurarCorreo() {
        try {
            FxUtils.abrirModal("gestorCredenciales-view.fxml", "Gestor de credenciales de correo");
        } catch (IOException e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir el gestor de credenciales: " + e.getMessage());
        }
    }

    @FXML
    private void abrirAjustesFechas() {
        try {
            FxUtils.abrirModal("ajustesFechas-view.fxml", "Fechas automáticas");
        } catch (IOException e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir el ajuste de fechas: " + e.getMessage());
        }
    }
}
