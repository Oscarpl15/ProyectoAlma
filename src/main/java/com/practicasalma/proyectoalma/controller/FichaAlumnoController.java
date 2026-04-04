package com.practicasalma.proyectoalma.controller;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.PersonaAutorizada;
import com.practicasalma.proyectoalma.service.AlumnoService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

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

    // Botones de acción
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnCerrar;

    // Curso y Grupo
    @FXML private TextField txtCurso;
    @FXML private TextField txtGrupoLectura;
    @FXML private ComboBox<String> comboGrupo;

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

    // Servicio para conectar con la BBDD
    private final AlumnoService alumnoService = new AlumnoService();

    @FXML
    public void initialize() {
        comboGrupo.getItems().addAll(
                "g1 lunes/miercoles",
                "g2 lunes/miercoles",
                "g1 martes/jueves",
                "g2 martes/jueves"
        );

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
            dpFechaNacimiento.setValue(alumnoActual.getFechaNacimiento());

            chkAutoDatos.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaUsoDatos()));
            chkAutoImagen.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaImagen()));
            chkAutoActividades.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaActividades()));
            chkAutoComunicaciones.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaComunicaciones()));
            chkAutoIrseSolo.setSelected(Boolean.TRUE.equals(alumnoActual.getAutorizaIrseSolo()));


            // --- CARGAR CURSO Y GRUPO DESDE MATRÍCULA ---
            // Buscamos la última matrícula activa (asumimos la última de la lista)
            if (alumnoActual.getMatriculas() != null && !alumnoActual.getMatriculas().isEmpty()) {
                var ultimaMatricula = alumnoActual.getMatriculas().get(alumnoActual.getMatriculas().size() - 1);
                txtCurso.setText(ultimaMatricula.getCurso() != null ? ultimaMatricula.getCurso() : "Sin curso");
                txtGrupoLectura.setText(ultimaMatricula.getGrupoAsignado() != null ? ultimaMatricula.getGrupoAsignado() : "Sin grupo");
                comboGrupo.setValue(ultimaMatricula.getGrupoAsignado());
            } else {
                txtCurso.setText("Sin matricular");
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

            tablaAutorizados.setItems(javafx.collections.FXCollections.observableArrayList(alumnoActual.getAutorizadaRecoger()));

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

        // Truco visual para el Grupo (Intercambiar Textfield por Combo)
        txtGrupoLectura.setVisible(!editable);
        txtGrupoLectura.setManaged(!editable);
        comboGrupo.setVisible(editable);
        comboGrupo.setManaged(editable);

        // Truco visual para Seguimiento/Derivación
        boxSeguimientoLectura.setVisible(!editable);
        boxSeguimientoLectura.setManaged(!editable);
        boxSeguimientoEdicion.setVisible(editable);
        boxSeguimientoEdicion.setManaged(editable);

        // Autorizados
        boxBotonesAutorizados.setVisible(editable);
        boxBotonesAutorizados.setManaged(editable);
        if (tablaAutorizados.getContextMenu() != null) {
            tablaAutorizados.getContextMenu().getItems().get(0).setVisible(editable);
        }

        if (!editable) {
            // Al volver a modo lectura, actualizamos los textos resumen por si ha cambiado algún check
            actualizarTextosSeguimiento();

            // Sincronizamos el texto de lectura del grupo con lo que se haya elegido en el desplegable
            if (comboGrupo.getValue() != null) {
                txtGrupoLectura.setText(comboGrupo.getValue());
            }

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

            // 5. Actualizar la matrícula (Grupo)
            if (alumnoActual.getMatriculas() != null && !alumnoActual.getMatriculas().isEmpty()) {
                var ultimaMatricula = alumnoActual.getMatriculas().get(alumnoActual.getMatriculas().size() - 1);
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
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            rutaFotoSeleccionada = archivoSeleccionado.getAbsolutePath();
            imgFoto.setImage(new Image(archivoSeleccionado.toURI().toString()));
        }
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
        TextField dni = new TextField(); dni.setPromptText("DNI/NIE");
        TextField telefono = new TextField(); telefono.setPromptText("Teléfono");
        TextField relacion = new TextField(); relacion.setPromptText("Ej: Abuela, Tío...");

        grid.add(new Label("Nombre:"), 0, 0); grid.add(nombre, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1); grid.add(apellidos, 1, 1);
        grid.add(new Label("DNI/NIE:"), 0, 2); grid.add(dni, 1, 2);
        grid.add(new Label("Teléfono:"), 0, 3); grid.add(telefono, 1, 3);
        grid.add(new Label("Relación:"), 0, 4); grid.add(relacion, 1, 4);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nombre::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType && !nombre.getText().trim().isEmpty() && !dni.getText().trim().isEmpty()) {
                PersonaAutorizada pa = new PersonaAutorizada(
                        nombre.getText(), apellidos.getText(), dni.getText(), telefono.getText(), relacion.getText()
                );
                pa.setAlumno(alumnoActual);
                return pa;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pa -> {
            alumnoActual.getAutorizadaRecoger().add(pa);
            tablaAutorizados.getItems().add(pa);
        });
    }

    @FXML
    private void eliminarAutorizado() {
        PersonaAutorizada seleccion = tablaAutorizados.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            alumnoActual.getAutorizadaRecoger().remove(seleccion);
            tablaAutorizados.getItems().remove(seleccion);
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}
