package com.example.demo.controller;

import com.example.demo.database.DatabaseConnection;
import com.example.demo.model.Empleado;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoController {

    // ComboBoxes y TextFields de Búsqueda
    @FXML private ComboBox<String> cbFiltro1, cbFiltro2, cbFiltro3;
    @FXML private TextField txtFiltro1, txtFiltro2, txtFiltro3;

    // TextFields del Formulario
    @FXML private TextField txtCodigo, txtNombre, txtCorreo, txtCargo, txtSalario;

    // TableView y Columnas
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, String> colCodigo, colNombre, colCorreo, colCargo;
    @FXML private TableColumn<Empleado, Double> colSalario;

    // Variable para almacenar el ID del empleado seleccionado (necesario para UPDATE y DELETE)
    private int idEmpleadoSeleccionado = -1;

    @FXML
    public void initialize() {
        // Enlazar las columnas de la tabla con las propiedades del modelo Empleado
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colSalario.setCellValueFactory(new PropertyValueFactory<>("salario"));

        // Configurar los ComboBox de filtros
        ObservableList<String> columnas = FXCollections.observableArrayList(
                "codigo", "nombre", "correo", "cargo", "salario"
        );
        cbFiltro1.setItems(columnas);
        cbFiltro2.setItems(columnas);
        cbFiltro3.setItems(columnas);

        // Cargar los datos iniciales
        cargarTodosLosEmpleados();
    }

    // --------------------------------------------------------
    // OPERACIONES CRUD
    // --------------------------------------------------------

    @FXML
    void guardarEmpleado(ActionEvent event) {
        if (!validarCamposFormulario()) return;

        String sql = "INSERT INTO empleado (codigo, nombre, correo, cargo, salario) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtCodigo.getText().trim());
            pstmt.setString(2, txtNombre.getText().trim());
            pstmt.setString(3, txtCorreo.getText().trim());
            pstmt.setString(4, txtCargo.getText().trim());
            pstmt.setDouble(5, Double.parseDouble(txtSalario.getText().trim()));

            pstmt.executeUpdate();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Empleado registrado correctamente.");
            limpiarFormulario();
            cargarTodosLosEmpleados();

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "No se pudo guardar el empleado.\nDetalle: " + e.getMessage());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", "El salario debe ser un número válido.");
        }
    }

    @FXML
    void actualizarEmpleado(ActionEvent event) {
        if (idEmpleadoSeleccionado == -1) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Seleccione un empleado de la tabla para actualizar.");
            return;
        }
        if (!validarCamposFormulario()) return;

        String sql = "UPDATE empleado SET codigo=?, nombre=?, correo=?, cargo=?, salario=? WHERE id_empleado=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtCodigo.getText().trim());
            pstmt.setString(2, txtNombre.getText().trim());
            pstmt.setString(3, txtCorreo.getText().trim());
            pstmt.setString(4, txtCargo.getText().trim());
            pstmt.setDouble(5, Double.parseDouble(txtSalario.getText().trim()));
            pstmt.setInt(6, idEmpleadoSeleccionado);

            pstmt.executeUpdate();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Empleado actualizado correctamente.");
            limpiarFormulario();
            cargarTodosLosEmpleados();

        } catch (SQLException | NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar: " + e.getMessage());
        }
    }

    @FXML
    void eliminarEmpleado(ActionEvent event) {
        if (idEmpleadoSeleccionado == -1) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Seleccione un empleado de la tabla para eliminar.");
            return;
        }

        // Logical Deletion: Se actualiza el estado a FALSE en lugar de borrar el registro
        String sql = "UPDATE empleado SET activo=FALSE WHERE id_empleado=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEmpleadoSeleccionado);
            pstmt.executeUpdate();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Empleado dado de baja exitosamente.");
            limpiarFormulario();
            cargarTodosLosEmpleados();

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar: " + e.getMessage());
        }
    }

    @FXML
    void seleccionarEmpleado(MouseEvent event) {
        Empleado emp = tblEmpleados.getSelectionModel().getSelectedItem();
        if (emp != null) {
            idEmpleadoSeleccionado = emp.getIdEmpleado();
            txtCodigo.setText(emp.getCodigo());
            txtNombre.setText(emp.getNombre());
            txtCorreo.setText(emp.getCorreo());
            txtCargo.setText(emp.getCargo());
            txtSalario.setText(String.valueOf(emp.getSalario()));
        }
    }

    @FXML
    void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtCorreo.clear();
        txtCargo.clear();
        txtSalario.clear();
        idEmpleadoSeleccionado = -1;
        tblEmpleados.getSelectionModel().clearSelection();
    }

    // --------------------------------------------------------
    // BÚSQUEDA PARAMETRIZADA
    // --------------------------------------------------------

    @FXML
    void buscarEmpleados(ActionEvent event) {
        // Consulta base: Solo registros activos
        StringBuilder sql = new StringBuilder("SELECT * FROM empleado WHERE activo = TRUE");
        List<Object> parametros = new ArrayList<>();

        // Evaluar cada filtro de forma independiente y añadirlos si tienen valor
        agregarCondicionFiltro(sql, parametros, cbFiltro1.getValue(), txtFiltro1.getText());
        agregarCondicionFiltro(sql, parametros, cbFiltro2.getValue(), txtFiltro2.getText());
        agregarCondicionFiltro(sql, parametros, cbFiltro3.getValue(), txtFiltro3.getText());

        ejecutarConsultaBusqueda(sql.toString(), parametros);
    }

    private void agregarCondicionFiltro(StringBuilder sql, List<Object> parametros, String columna, String valor) {
        if (columna != null && !columna.isEmpty() && valor != null && !valor.trim().isEmpty()) {
            sql.append(" AND ").append(columna).append("::text ILIKE ?");
            // ILIKE permite buscar sin distinguir mayúsculas/minúsculas. % % busca coincidencias parciales.
            parametros.add("%" + valor.trim() + "%");
        }
    }

    @FXML
    void quitarFiltros(ActionEvent event) {
        cbFiltro1.setValue(null); txtFiltro1.clear();
        cbFiltro2.setValue(null); txtFiltro2.clear();
        cbFiltro3.setValue(null); txtFiltro3.clear();
        cargarTodosLosEmpleados();
    }

    private void ejecutarConsultaBusqueda(String sql, List<Object> parametros) {
        ObservableList<Empleado> listaEmpleados = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Asignar dinámicamente los parámetros al PreparedStatement
            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Empleado emp = new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("cargo"),
                        rs.getDouble("salario")
                );
                listaEmpleados.add(emp);
            }
            tblEmpleados.setItems(listaEmpleados);

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error en la consulta", "Fallo al realizar la búsqueda: " + e.getMessage());
        }
    }

    private void cargarTodosLosEmpleados() {
        // Carga inicial y reset: Solo empleados activos
        ejecutarConsultaBusqueda("SELECT * FROM empleado WHERE activo = TRUE", new ArrayList<>());
    }

    // --------------------------------------------------------
    // UTILIDADES
    // --------------------------------------------------------

    private boolean validarCamposFormulario() {
        if (txtCodigo.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty() ||
                txtCorreo.getText().trim().isEmpty() || txtCargo.getText().trim().isEmpty() ||
                txtSalario.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor, complete todos los campos obligatorios.");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}