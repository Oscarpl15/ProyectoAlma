package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.model.PersonaAutorizada;
import com.practicasalma.proyectoalma.model.Tutor;
import com.practicasalma.proyectoalma.service.AlumnoService;
import com.practicasalma.proyectoalma.service.PersonaAutorizadaService;
import com.practicasalma.proyectoalma.service.TutorService;
import com.practicasalma.proyectoalma.util.ui.FxUtils;
import com.practicasalma.proyectoalma.util.doc.GestorDocumentos;
import com.practicasalma.proyectoalma.util.validacion.Validador;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controlador JavaFX de la ficha de detalle de un alumno ({@code fichaAlumno-view.fxml}).
 * <p>
 * Permite ver y editar todos los datos del alumno: datos personales, matrículas,
 * tutores legales, personas autorizadas a recogerle, familiares, autorizaciones
 * y documentación adjunta. Se abre como modal desde {@link AlumnosController}.
 * </p>
 */
public class FichaAlumnoController {

    @FXML private ImageView imgFoto;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDni;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtColegio;

    @FXML private CheckBox chkAutoDatos;
    @FXML private CheckBox chkAutoImagen;
    @FXML private CheckBox chkAutoActividades;
    @FXML private CheckBox chkAutoComunicaciones;
    @FXML private CheckBox chkAutoIrseSolo;
    @FXML private Label lblEstado;

    // Botones de acción
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnCerrar;
    @FXML private Button btnBajaAlta;

    // Curso y Grupo
    @FXML private TextField txtCurso;
    @FXML private ComboBox<String> comboCurso;
    @FXML private TextField txtGrupoLectura;
    @FXML private ComboBox<String> comboGrupo;

    // Nacionalidad, Género, Ciudad y Código Postal
    @FXML private TextField txtNacionalidad;
    @FXML private TextField txtGeneroLectura;
    @FXML private ComboBox<String> comboGenero;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;

    // Seguimiento y Derivación (Lectura)
    @FXML private VBox boxSeguimientoLectura;
    @FXML private Label lblSeguimientoValor;
    @FXML private Label lblDerivacionValor;

    // Seguimiento y Derivación (Edición)
    @FXML private VBox boxSeguimientoEdicion;
    @FXML private CheckBox chkSeguimientoSS;
    @FXML private CheckBox chkSeguimientoSAF;
    @FXML private CheckBox chkDerivacionSS;
    @FXML private CheckBox chkDerivacionSAF;
    @FXML private CheckBox chkDerivacionEOEP;
    @FXML private CheckBox chkDerivacionColegio;
    @FXML private CheckBox chkDerivacionOtro;
    @FXML private TextField txtDerivado;

    // Tabla Matrículas
    @FXML private TableView<Matricula> tablaMatriculas;
    @FXML private TableColumn<Matricula, String> colMatAnyo;
    @FXML private TableColumn<Matricula, String> colMatCurso;
    @FXML private TableColumn<Matricula, String> colMatGrupo;
    @FXML private TableColumn<Matricula, String> colMatRepeticion;

    // Tabla Tutores
    @FXML private TableView<Tutor> tablaTutores;
    @FXML private TableColumn<Tutor, String> colTutorNombre;
    @FXML private TableColumn<Tutor, String> colTutorApellidos;
    @FXML private TableColumn<Tutor, String> colTutorDni;
    @FXML private TableColumn<Tutor, String> colTutorTelefono;
    @FXML private TableColumn<Tutor, String> colTutorRelacion;
    @FXML private HBox boxBotonesTutores;

    // Tabla Personas Autorizadas
    @FXML private TableView<PersonaAutorizada> tablaAutorizados;
    @FXML private TableColumn<PersonaAutorizada, String> colAutoNombre;
    @FXML private TableColumn<PersonaAutorizada, String> colAutoApellidos;
    @FXML private TableColumn<PersonaAutorizada, String> colAutoDni;
    @FXML private TableColumn<PersonaAutorizada, String> colAutoTelefono;
    @FXML private TableColumn<PersonaAutorizada, String> colAutoRelacion;
    @FXML private HBox boxBotonesAutorizados;

    private Alumno alumnoActual;
    private String rutaFotoSeleccionada = null;

    private final AlumnoService alumnoService = new AlumnoService();
    private final TutorService tutorService = new TutorService();
    private final PersonaAutorizadaService paService = new PersonaAutorizadaService();

    @FXML
    public void initialize() {
        comboGrupo.getItems().addAll(
                "g1 lunes/miercoles",
                "g2 lunes/miercoles",
                "g1 martes/jueves",
                "g2 martes/jueves"
        );
        comboCurso.getItems().addAll(
                "1º Infantil", "2º Infantil", "3º Infantil",
                "1º Primaria", "2º Primaria", "3º Primaria",
                "4º Primaria", "5º Primaria", "6º Primaria"
        );
        comboGenero.getItems().addAll(
                "Masculino", "Femenino", "No binario", "Prefiero no especificar"
        );

        // Configurar columnas de Matrículas
        colMatAnyo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAnyoAcademico()));
        colMatCurso.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCurso()));
        colMatGrupo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getGrupoAsignado() != null ? c.getValue().getGrupoAsignado() : "—"));
        colMatRepeticion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                Boolean.TRUE.equals(c.getValue().getEsRepeticion()) ? "Sí" : "No"));

        // Doble clic en matrícula → abrir ficha de matrícula
        tablaMatriculas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Matricula seleccionada = tablaMatriculas.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    abrirFichaMatricula(seleccionada);
                }
            }
        });

        // Configurar columnas de Tutores
        colTutorNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        colTutorApellidos.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getApellidos()));
        colTutorDni.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDocumentoIdentidad()));
        colTutorTelefono.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTelefono()));
        colTutorRelacion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRelacion()));

        ContextMenu menuTutores = new ContextMenu();
        MenuItem itemEliminarTutor = new MenuItem("Eliminar Tutor");
        itemEliminarTutor.setOnAction(e -> eliminarTutor());
        menuTutores.getItems().add(itemEliminarTutor);
        tablaTutores.setContextMenu(menuTutores);

        // Configurar columnas de Autorizados
        colAutoNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        colAutoApellidos.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getApellidos()));
        colAutoDni.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDocumentoIdentidad()));
        colAutoTelefono.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTelefono()));
        colAutoRelacion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRelacion()));

        // Menú contextual (Clic derecho)
        ContextMenu menuContextual = new ContextMenu();
        MenuItem itemEliminar = new MenuItem("Eliminar Autorizado");
        itemEliminar.setOnAction(e -> eliminarAutorizado());
        menuContextual.getItems().add(itemEliminar);
        tablaAutorizados.setContextMenu(menuContextual);
    }

    public void setAlumno(Alumno alumno) {
        this.alumnoActual = alumno;
        cargarDatosEnVista();

        // Forzamos el modo lectura al abrir la ficha
        cambiarModoEdicion(false);

    }

    private void cargarDatosEnVista() {
        if (alumnoActual != null) {
            txtNombre.setText(alumnoActual.getNombre() != null ? alumnoActual.getNombre() : "");
            txtApellidos.setText(alumnoActual.getApellidos() != null ? alumnoActual.getApellidos() : "");
            txtDni.setText(alumnoActual.getDocumentoIdentidad() != null ? alumnoActual.getDocumentoIdentidad() : "");
            txtDireccion.setText(alumnoActual.getDireccion() != null ? alumnoActual.getDireccion() : "");
            txtTelefono.setText(alumnoActual.getTelefono() != null ? alumnoActual.getTelefono() : "");
            txtColegio.setText(alumnoActual.getColegio() != null ? alumnoActual.getColegio() : "");
            txtDerivado.setText(alumnoActual.getDerivadoPor() != null ? alumnoActual.getDerivadoPor() : "");
            txtNacionalidad.setText(alumnoActual.getNacionalidad() != null ? alumnoActual.getNacionalidad() : "");
            txtGeneroLectura.setText(alumnoActual.getGenero() != null ? alumnoActual.getGenero() : "");
            comboGenero.setValue(alumnoActual.getGenero());
            txtCiudad.setText(alumnoActual.getCiudad() != null ? alumnoActual.getCiudad() : "");
            txtCodigoPostal.setText(alumnoActual.getCodigoPostal() != null ? alumnoActual.getCodigoPostal() : "");
            dpFechaNacimiento.setValue(alumnoActual.getFechaNacimiento());

            chkAutoDatos.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaUsoDatos()));
            chkAutoImagen.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaImagen()));
            chkAutoActividades.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaActividades()));
            chkAutoComunicaciones.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaComunicaciones()));
            chkAutoIrseSolo.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaIrseSolo()));
            actualizarLabelEstado(Boolean.TRUE.equals(alumnoActual.getActivo()));

            // --- CARGAR CURSO Y GRUPO DESDE MATRÍCULA ---
            // Buscamos la última matrícula activa (asumimos la última de la lista)
            if (alumnoActual.getMatriculas() != null && !alumnoActual.getMatriculas().isEmpty()) {
                var ultimaMatricula = alumnoActual.getMatriculas().get(alumnoActual.getMatriculas().size() - 1);
                txtCurso.setText(ultimaMatricula.getCurso() != null ? ultimaMatricula.getCurso() : "Sin curso");
                comboCurso.setValue(ultimaMatricula.getCurso());
                txtGrupoLectura.setText(ultimaMatricula.getGrupoAsignado() != null ? ultimaMatricula.getGrupoAsignado() : "Sin grupo");
                comboGrupo.setValue(ultimaMatricula.getGrupoAsignado());
            } else {
                txtCurso.setText("Sin matricular");
                comboCurso.setValue(null);
                txtGrupoLectura.setText("Sin grupo");
                comboGrupo.setValue(null);
            }

            // --- CARGAR SEGUIMIENTO Y DERIVACIÓN (Checkboxes) ---
            chkSeguimientoSS.setSelected(Boolean.TRUE.equals(alumnoActual.getSeguimientoServiciosSociales()));
            chkSeguimientoSAF.setSelected(Boolean.TRUE.equals(alumnoActual.getSeguimientoSaf()));
            chkDerivacionSS.setSelected(Boolean.TRUE.equals(alumnoActual.getDerivacionSS()));
            chkDerivacionSAF.setSelected(Boolean.TRUE.equals(alumnoActual.getDerivacionSaf()));
            chkDerivacionEOEP.setSelected(Boolean.TRUE.equals(alumnoActual.getDerivacionEoep()));
            chkDerivacionColegio.setSelected(Boolean.TRUE.equals(alumnoActual.getDerivacionColegio()));
            chkDerivacionOtro.setSelected(Boolean.TRUE.equals(alumnoActual.getDerivacionOtro()));
            txtDerivado.setText(alumnoActual.getDerivadoPor() != null ? alumnoActual.getDerivadoPor() : "");

            tablaMatriculas.setItems(FXCollections.observableArrayList(alumnoActual.getMatriculas()));
            tablaTutores.setItems(FXCollections.observableArrayList(alumnoActual.getTutores()));
            tablaAutorizados.setItems(FXCollections.observableArrayList(alumnoActual.getAutorizadaRecoger()));

            // Generar el texto bonito para el Modo Lectura
            actualizarTextosSeguimiento();

            String rutaAlumno = alumnoActual.getRutaFotoPerfil();
            if (rutaAlumno != null && !rutaAlumno.trim().isEmpty()) {
                try {
                    if (rutaAlumno.startsWith("/")) {
                        imgFoto.setImage(new Image(getClass().getResource(rutaAlumno).toExternalForm()));
                    } else if (rutaAlumno.startsWith("file:") || rutaAlumno.startsWith("http")) {
                        imgFoto.setImage(new Image(rutaAlumno));
                    } else {
                        imgFoto.setImage(new Image(new File(rutaAlumno).toURI().toString()));
                    }
                } catch (Exception e) {
                    System.out.println("Error al cargar la imagen: " + rutaAlumno);
                }
            } else {
                cargarFotoPorDefecto();
            }
            actualizarBotonBajaAlta();
        }
    }

    private void cargarFotoPorDefecto() {
        try {
            imgFoto.setImage(new Image(getClass().getResource("/com/practicasalma/proyectoalma/assets/default.png").toExternalForm()));
        } catch (Exception e) {
            System.out.println("No se encontró imagen por defecto.");
        }
    }

    // --- CONTROL DE LECTURA/EDICIÓN ---

    private void cambiarModoEdicion(boolean editable) {
        // TextFields y DatePicker
        txtNombre.setEditable(editable);
        txtApellidos.setEditable(editable);
        txtDni.setEditable(editable);
        txtDireccion.setEditable(editable);
        txtTelefono.setEditable(editable);
        txtColegio.setEditable(editable);
        txtDerivado.setEditable(editable);
        dpFechaNacimiento.setDisable(!editable);

        // MAGIA 2: Ocultar y comprimir el botón de Cambiar Foto si no estamos editando
        btnCambiarFoto.setVisible(editable);
        btnCambiarFoto.setManaged(editable);

        // Visibilidad de Botones Inferiores
        btnEditar.setVisible(!editable);
        btnEditar.setManaged(!editable);
        btnCerrar.setVisible(!editable);
        btnCerrar.setManaged(!editable);

        btnGuardar.setVisible(editable);
        btnGuardar.setManaged(editable);
        btnCancelarEdicion.setVisible(editable);
        btnCancelarEdicion.setManaged(editable);

        // MAGIA 3: Transformar el diseño de los CheckBoxes delegando la lógica visual al CSS
        actualizarVistaCheckbox(chkAutoDatos, "Protección Datos", editable);
        actualizarVistaCheckbox(chkAutoImagen, "Uso Imagen", editable);
        actualizarVistaCheckbox(chkAutoActividades, "Actividades", editable);
        actualizarVistaCheckbox(chkAutoComunicaciones, "Comunicaciones", editable);
        actualizarVistaCheckbox(chkAutoIrseSolo, "Autoriza Irse Solo", editable);

        // Truco visual para el Curso Actual
        boolean tieneMatriculas = alumnoActual != null
                && alumnoActual.getMatriculas() != null
                && !alumnoActual.getMatriculas().isEmpty();
        txtCurso.setVisible(!editable || !tieneMatriculas);
        txtCurso.setManaged(!editable || !tieneMatriculas);
        comboCurso.setVisible(editable && tieneMatriculas);
        comboCurso.setManaged(editable && tieneMatriculas);

        // Truco visual para el Grupo (Intercambiar Textfield por Combo)
        txtGrupoLectura.setVisible(!editable);
        txtGrupoLectura.setManaged(!editable);
        comboGrupo.setVisible(editable);
        comboGrupo.setManaged(editable);

        // Truco visual para el Género
        txtGeneroLectura.setVisible(!editable);
        txtGeneroLectura.setManaged(!editable);
        comboGenero.setVisible(editable);
        comboGenero.setManaged(editable);

        // Nacionalidad, Ciudad y Código Postal
        txtNacionalidad.setEditable(editable);
        txtCiudad.setEditable(editable);
        txtCodigoPostal.setEditable(editable);

        // Truco visual para Seguimiento/Derivación
        boxSeguimientoLectura.setVisible(!editable);
        boxSeguimientoLectura.setManaged(!editable);
        boxSeguimientoEdicion.setVisible(editable);
        boxSeguimientoEdicion.setManaged(editable);

        // Tutores
        boxBotonesTutores.setVisible(editable);
        boxBotonesTutores.setManaged(editable);
        if (tablaTutores.getContextMenu() != null) {
            tablaTutores.getContextMenu().getItems().get(0).setVisible(editable);
        }

        // Autorizados
        boxBotonesAutorizados.setVisible(editable);
        boxBotonesAutorizados.setManaged(editable);
        if (tablaAutorizados.getContextMenu() != null) {
            tablaAutorizados.getContextMenu().getItems().get(0).setVisible(editable);
        }

        if (!editable) {
            // Al volver a modo lectura, actualizamos los textos resumen por si ha cambiado algún check
            actualizarTextosSeguimiento();

            // Sincronizamos el texto de lectura del curso
            if (comboCurso.getValue() != null) {
                txtCurso.setText(comboCurso.getValue());
            }
            // Sincronizamos el texto de lectura del grupo con lo que se haya elegido en el desplegable
            if (comboGrupo.getValue() != null) {
                txtGrupoLectura.setText(comboGrupo.getValue());
            }
            // Sincronizamos el texto de lectura del género
            txtGeneroLectura.setText(comboGenero.getValue() != null ? comboGenero.getValue() : "");

            Platform.runLater(() -> btnEditar.requestFocus());
        }
    }

    private void actualizarVistaCheckbox(CheckBox chk, String textoBase, boolean editable) {
        chk.setDisable(!editable);

        // Limpiamos las clases CSS previamente añadidas
        chk.getStyleClass().removeAll("check-lectura-true", "check-lectura-false");

        if (!editable) {
            // Modo Lectura: Añadimos iconos de texto y aplicamos la clase CSS correspondiente
            if (chk.isSelected()) {
                chk.setText("✔  " + textoBase);
                chk.getStyleClass().add("check-lectura-true");
            } else {
                chk.setText("✘  " + textoBase);
                chk.getStyleClass().add("check-lectura-false");
            }
        } else {
            // Modo Edición: Restauramos el texto original y la caja aparecerá sola al quitarle el disabled
            chk.setText(textoBase);
        }
    }

    @FXML
    private void activarEdicion(ActionEvent event) {
        cambiarModoEdicion(true);
    }

    @FXML
    private void cancelarEdicion(ActionEvent event) {
        cargarDatosEnVista(); // Restaura los datos
        cambiarModoEdicion(false); // Vuelve a bloquear
    }

    @FXML
    private void guardarDatos() {
        try {
            // 1. Recoger datos de los TextFields y DatePicker
            alumnoActual.setNombre(txtNombre.getText());
            alumnoActual.setApellidos(txtApellidos.getText());
            alumnoActual.setDocumentoIdentidad(txtDni.getText());
            alumnoActual.setDireccion(txtDireccion.getText());
            alumnoActual.setTelefono(txtTelefono.getText());
            alumnoActual.setColegio(txtColegio.getText());
            alumnoActual.setFechaNacimiento(dpFechaNacimiento.getValue());
            alumnoActual.setDerivadoPor(txtDerivado.getText());
            alumnoActual.setNacionalidad(txtNacionalidad.getText().isBlank() ? null : txtNacionalidad.getText().trim());
            alumnoActual.setGenero(comboGenero.getValue());
            alumnoActual.setCiudad(txtCiudad.getText().isBlank() ? null : txtCiudad.getText().trim());
            String cp = txtCodigoPostal.getText().trim();
            if (!cp.isBlank() && !Validador.esCodigoPostalValido(cp)) {
                FxUtils.mostrarAlerta(Alert.AlertType.WARNING, "Código postal inválido", "El código postal debe tener exactamente 5 dígitos.");
                return;
            }
            alumnoActual.setCodigoPostal(cp.isBlank() ? null : cp);

            // 2. Recoger Autorizaciones
            alumnoActual.setAutorizaUsoDatos(chkAutoDatos.isSelected());
            alumnoActual.setAutorizaImagen(chkAutoImagen.isSelected());
            alumnoActual.setAutorizaActividades(chkAutoActividades.isSelected());
            alumnoActual.setAutorizaComunicaciones(chkAutoComunicaciones.isSelected());
            alumnoActual.setAutorizaIrseSolo(chkAutoIrseSolo.isSelected());

            // 3. Recoger Seguimiento y Derivación
            alumnoActual.setSeguimientoServiciosSociales(chkSeguimientoSS.isSelected());
            alumnoActual.setSeguimientoSaf(chkSeguimientoSAF.isSelected());

            alumnoActual.setDerivacionSS(chkDerivacionSS.isSelected());
            alumnoActual.setDerivacionSaf(chkDerivacionSAF.isSelected());
            alumnoActual.setDerivacionEoep(chkDerivacionEOEP.isSelected());
            alumnoActual.setDerivacionColegio(chkDerivacionColegio.isSelected());
            alumnoActual.setDerivacionOtro(chkDerivacionOtro.isSelected());

            // 4. Si se cambió la foto, actualizamos la ruta
            if (rutaFotoSeleccionada != null) {
                alumnoActual.setRutaFotoPerfil(rutaFotoSeleccionada);
            }

            // 5. Actualizar la matrícula (Curso y Grupo)
            if (alumnoActual.getMatriculas() != null && !alumnoActual.getMatriculas().isEmpty()) {
                var ultimaMatricula = alumnoActual.getMatriculas().get(alumnoActual.getMatriculas().size() - 1);
                if (comboCurso.getValue() != null) {
                    ultimaMatricula.setCurso(comboCurso.getValue());
                }
                if (comboGrupo.getValue() != null) {
                    ultimaMatricula.setGrupoAsignado(comboGrupo.getValue());
                }
            }

            // --- ¡GUARDAMOS EN BASE DE DATOS! ---
            alumnoService.actualizarAlumno(alumnoActual);

            // Volvemos al modo lectura
            cambiarModoEdicion(false);

            // Avisamos al usuario
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Los datos de " + alumnoActual.getNombre() + " se han actualizado correctamente.");
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se han podido guardar los cambios");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void seleccionarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona foto del alumno");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            File dir = GestorDocumentos.getDirectorio("Alumnos", txtNombre.getText(), txtApellidos.getText());
            if (dir != null) {
                File destino = new File(dir, "foto" + obtenerExtension(archivoSeleccionado.getName()));
                try {
                    java.nio.file.Files.copy(archivoSeleccionado.toPath(), destino.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    rutaFotoSeleccionada = destino.getAbsolutePath();
                } catch (java.io.IOException e) {
                    rutaFotoSeleccionada = archivoSeleccionado.getAbsolutePath();
                }
            } else {
                rutaFotoSeleccionada = archivoSeleccionado.getAbsolutePath();
            }
            imgFoto.setImage(new Image(new File(rutaFotoSeleccionada).toURI().toString()));
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int punto = nombreArchivo.lastIndexOf('.');
        return punto >= 0 ? nombreArchivo.substring(punto) : "";
    }

    private void actualizarLabelEstado(boolean activo) {
        lblEstado.getStyleClass().removeAll("celda-activo", "celda-baja");
        if (activo) {
            lblEstado.setText("✔  Activo");
            lblEstado.getStyleClass().add("celda-activo");
        } else {
            lblEstado.setText("✘  Inactivo");
            lblEstado.getStyleClass().add("celda-baja");
        }
    }

    private void actualizarBotonBajaAlta() {
        boolean activo = Boolean.TRUE.equals(alumnoActual.getActivo());
        btnBajaAlta.getStyleClass().removeAll("boton-baja", "boton-guardar-exito");
        if (activo) {
            btnBajaAlta.setText("Dar de baja");
            btnBajaAlta.getStyleClass().add("boton-baja");
        } else {
            btnBajaAlta.setText("Dar de alta");
            btnBajaAlta.getStyleClass().add("boton-guardar-exito");
        }
    }

    @FXML
    private void toggleBajaAlta() {
        boolean activo = Boolean.TRUE.equals(alumnoActual.getActivo());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(activo ? "Dar de baja" : "Dar de alta");
        confirm.setHeaderText((activo ? "¿Dar de baja a " : "¿Dar de alta a ") + alumnoActual.getNombre() + "?");
        if (!activo) {
            confirm.setContentText("Se generará una nueva matrícula para el curso actual.");
        }
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    if (activo) {
                        alumnoService.darDeBaja(alumnoActual.getId());
                    } else {
                        alumnoService.darDeAlta(alumnoActual.getId());
                    }
                    alumnoActual = alumnoService.obtenerCompleto(alumnoActual.getId());
                    cargarDatosEnVista();
                    cambiarModoEdicion(false);
                } catch (Exception e) {
                    FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    private void actualizarTextosSeguimiento() {
        // Texto de Seguimiento
        java.util.List<String> seg = new java.util.ArrayList<>();
        if (chkSeguimientoSS.isSelected()) seg.add("Servicios Sociales");
        if (chkSeguimientoSAF.isSelected()) seg.add("SAF");
        lblSeguimientoValor.setText( (seg.isEmpty() ? "Ninguno" : String.join(" y ", seg)));

        // Texto de Derivación
        java.util.List<String> der = new java.util.ArrayList<>();
        if (chkDerivacionSS.isSelected()) der.add("Servicios Sociales");
        if (chkDerivacionSAF.isSelected()) der.add("SAF");
        if (chkDerivacionEOEP.isSelected()) der.add("EOEP");
        if (chkDerivacionColegio.isSelected()) der.add("Colegio");
        if (chkDerivacionOtro.isSelected()) der.add("Otro");

        String entidades = String.join(", ", der);
        String persona = txtDerivado.getText() != null ? txtDerivado.getText().trim() : "";

        String textoDerivacion = "Ninguna";
        if (!entidades.isEmpty() || !persona.isEmpty()) {
            if (!entidades.isEmpty() && !persona.isEmpty()) {
                textoDerivacion = entidades + " (" + persona + ")";
            } else if (!entidades.isEmpty()) {
                textoDerivacion = entidades;
            } else {
                textoDerivacion = persona;
            }
        }
        lblDerivacionValor.setText(textoDerivacion);
    }

    @FXML
    private void agregarAutorizado() {
        Dialog<PersonaAutorizada> dialog = new Dialog<>();
        dialog.setTitle("Nueva Persona Autorizada");
        dialog.setHeaderText("Añadir autorización de recogida");

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/practicasalma/proyectoalma/css/estilos.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("fondo-blanco");

        ButtonType btnGuardarType = new ButtonType("Añadir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField nombre = new TextField(); nombre.setPromptText("Nombre");
        TextField apellidos = new TextField(); apellidos.setPromptText("Apellidos");
        TextField dni = new TextField(); dni.setPromptText("Documento (DNI/NIE/Pasaporte)");
        TextField telefono = new TextField(); telefono.setPromptText("Teléfono");
        TextField relacion = new TextField(); relacion.setPromptText("Ej: Abuela, Tío...");

        grid.add(new Label("Nombre:"), 0, 0); grid.add(nombre, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1); grid.add(apellidos, 1, 1);
        grid.add(new Label("Documento:"), 0, 2); grid.add(dni, 1, 2);
        grid.add(new Label("Teléfono:"), 0, 3); grid.add(telefono, 1, 3);
        grid.add(new Label("Relación:"), 0, 4); grid.add(relacion, 1, 4);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nombre::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType && !nombre.getText().trim().isEmpty() && !dni.getText().trim().isEmpty()) {
                return new PersonaAutorizada(
                        nombre.getText().trim(), apellidos.getText().trim(),
                        dni.getText().trim(), telefono.getText().trim(), relacion.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pa -> {
            alumnoActual.addPersonaAutorizada(pa);
            tablaAutorizados.getItems().add(pa);
        });
    }

    @FXML
    private void eliminarAutorizado() {
        PersonaAutorizada seleccion = tablaAutorizados.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            alumnoActual.removePersonaAutorizada(seleccion);
            tablaAutorizados.getItems().remove(seleccion);
        }
    }

    @FXML
    private void anadirDocumento() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar documento");
        File archivo = chooser.showOpenDialog(btnCerrar.getScene().getWindow());
        if (archivo == null) return;
        GestorDocumentos.copiarDocumento(archivo, "Alumnos", txtNombre.getText(), txtApellidos.getText());
    }

    @FXML
    private void verDocumentos() {
        GestorDocumentos.abrirDirectorio("Alumnos", txtNombre.getText(), txtApellidos.getText());
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    private void abrirFichaMatricula(Matricula matricula) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/practicasalma/proyectoalma/view/ficha-matricula.fxml"));
            Parent root = loader.load();

            FichaMatriculaController controller = loader.getController();
            String nombreAlumno = (alumnoActual.getNombre() != null ? alumnoActual.getNombre() : "") +
                    " " + (alumnoActual.getApellidos() != null ? alumnoActual.getApellidos() : "");
            controller.setMatricula(matricula, nombreAlumno.trim());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ficha de Matrícula");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refrescar tabla tras posibles cambios
            tablaMatriculas.refresh();
            // Actualizar el campo curso visible si la última matrícula cambió
            if (alumnoActual.getMatriculas() != null && !alumnoActual.getMatriculas().isEmpty()) {
                var ultima = alumnoActual.getMatriculas().get(alumnoActual.getMatriculas().size() - 1);
                txtCurso.setText(ultima.getCurso() != null ? ultima.getCurso() : "Sin curso");
                txtGrupoLectura.setText(ultima.getGrupoAsignado() != null ? ultima.getGrupoAsignado() : "Sin grupo");
                comboGrupo.setValue(ultima.getGrupoAsignado());
            }
        } catch (Exception e) {
            FxUtils.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ficha de matrícula: " + e.getMessage());
        }
    }

    // ── TUTORES ──────────────────────────────────────────────────────────────

    @FXML
    private void agregarNuevoTutor() {
        Dialog<Tutor> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Tutor / Padre");
        dialog.setHeaderText("Crear nueva persona tutora");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/practicasalma/proyectoalma/css/estilos.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("fondo-blanco");

        ButtonType btnOk = new ButtonType("Añadir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField nombre    = new TextField(); nombre.setPromptText("Nombre");
        TextField apellidos = new TextField(); apellidos.setPromptText("Apellidos");
        TextField dni       = new TextField(); dni.setPromptText("Documento (DNI/NIE/Pasaporte)");
        TextField telefono  = new TextField(); telefono.setPromptText("Teléfono");
        TextField relacion  = new TextField(); relacion.setPromptText("Ej: Madre, Padre...");
        relacion.setText("Tutor/a Legal");

        grid.add(new Label("Nombre:"),    0, 0); grid.add(nombre,    1, 0);
        grid.add(new Label("Apellidos:"), 0, 1); grid.add(apellidos, 1, 1);
        grid.add(new Label("Documento:"),   0, 2); grid.add(dni,       1, 2);
        grid.add(new Label("Teléfono:"),  0, 3); grid.add(telefono,  1, 3);
        grid.add(new Label("Relación:"),  0, 4); grid.add(relacion,  1, 4);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nombre::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn == btnOk && !nombre.getText().trim().isEmpty()) {
                Tutor t = new Tutor();
                t.setNombre(nombre.getText().trim());
                t.setApellidos(apellidos.getText().trim());
                t.setDocumentoIdentidad(dni.getText().trim().isEmpty() ? null : dni.getText().trim());
                t.setTelefono(telefono.getText().trim().isEmpty() ? null : telefono.getText().trim());
                t.setRelacion(relacion.getText().trim().isEmpty() ? "Tutor/a Legal" : relacion.getText().trim());
                return t;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(tutor -> {
            alumnoActual.addTutor(tutor);
            tablaTutores.getItems().add(tutor);
        });
    }

    @FXML
    private void agregarTutorExistente() {
        java.util.List<Tutor> todos = tutorService.obtenerTodos();
        // Filtramos los que ya están vinculados
        java.util.List<Tutor> disponibles = todos.stream()
                .filter(t -> !alumnoActual.getTutores().stream()
                        .anyMatch(existing -> existing.getId().equals(t.getId())))
                .toList();

        Tutor seleccionado = mostrarDialogoSeleccionPersona(
                disponibles.stream().map(t -> (com.practicasalma.proyectoalma.model.Persona) t).toList(),
                "Buscar Tutor Existente", "Selecciona un tutor ya registrado:");

        if (seleccionado != null) {
            alumnoActual.addTutor(seleccionado);
            tablaTutores.getItems().add(seleccionado);
        }
    }

    @FXML
    private void eliminarTutor() {
        Tutor seleccion = tablaTutores.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            alumnoActual.removeTutor(seleccion);
            tablaTutores.getItems().remove(seleccion);
        }
    }

    // ── PERSONAS AUTORIZADAS (BUSCAR EXISTENTE) ───────────────────────────────

    @FXML
    private void agregarAutorizadoExistente() {
        java.util.List<PersonaAutorizada> todos = paService.obtenerTodos();
        java.util.List<PersonaAutorizada> disponibles = todos.stream()
                .filter(p -> !alumnoActual.getAutorizadaRecoger().stream()
                        .anyMatch(existing -> existing.getId().equals(p.getId())))
                .toList();

        PersonaAutorizada seleccionada = mostrarDialogoSeleccionPersona(
                disponibles.stream().map(p -> (com.practicasalma.proyectoalma.model.Persona) p).toList(),
                "Buscar Persona Autorizada", "Selecciona una persona ya registrada:");

        if (seleccionada != null) {
            alumnoActual.addPersonaAutorizada(seleccionada);
            tablaAutorizados.getItems().add(seleccionada);
        }
    }

    // ── HELPER: DIÁLOGO DE SELECCIÓN GENÉRICO ────────────────────────────────

    @SuppressWarnings("unchecked")
    private <T extends com.practicasalma.proyectoalma.model.Persona> T mostrarDialogoSeleccionPersona(
            java.util.List<? extends com.practicasalma.proyectoalma.model.Persona> lista,
            String titulo, String cabecera) {

        if (lista.isEmpty()) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle(titulo);
            info.setHeaderText(null);
            info.setContentText("No hay personas disponibles para añadir.");
            info.showAndWait();
            return null;
        }

        Dialog<com.practicasalma.proyectoalma.model.Persona> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(cabecera);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/practicasalma/proyectoalma/css/estilos.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("fondo-blanco");

        ButtonType btnOk = new ButtonType("Seleccionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        TextField txtFiltro = new TextField();
        txtFiltro.setPromptText("Buscar por nombre o apellidos...");

        javafx.collections.ObservableList<com.practicasalma.proyectoalma.model.Persona> items =
                javafx.collections.FXCollections.observableArrayList(lista);
        javafx.collections.transformation.FilteredList<com.practicasalma.proyectoalma.model.Persona> filtrada =
                new javafx.collections.transformation.FilteredList<>(items, p -> true);

        txtFiltro.textProperty().addListener((obs, old, val) -> filtrada.setPredicate(p -> {
            if (val == null || val.isBlank()) return true;
            String f = val.toLowerCase();
            return p.getNombre().toLowerCase().contains(f) || p.getApellidos().toLowerCase().contains(f);
        }));

        ListView<com.practicasalma.proyectoalma.model.Persona> listView = new ListView<>(filtrada);
        listView.setPrefHeight(250);
        listView.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(com.practicasalma.proyectoalma.model.Persona item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getApellidos() + ", " + item.getNombre()
                        + (item.getDocumentoIdentidad() != null ? "  (" + item.getDocumentoIdentidad() + ")" : ""));
            }
        });

        VBox content = new VBox(8, txtFiltro, listView);
        content.setPrefWidth(380);
        dialog.getDialogPane().setContent(content);
        Platform.runLater(txtFiltro::requestFocus);

        dialog.setResultConverter(btn -> btn == btnOk ? listView.getSelectionModel().getSelectedItem() : null);

        return (T) dialog.showAndWait().orElse(null);
    }
}
