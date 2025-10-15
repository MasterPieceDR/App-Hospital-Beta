package com.mycompany.hospital.view;

import Application.Main;
import com.mycompany.hospital.util.Conexion;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboard {

    private VBox botonesVBox;
    private List<String> tablasActuales = new ArrayList<>();

    public void start(Stage stage) {
        Label titulo = new Label("Panel de Administración");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        botonesVBox = new VBox(10);
        botonesVBox.setAlignment(Pos.CENTER);
        botonesVBox.setPadding(new Insets(10));

        cargarBotonesDesdeBD();

        Button btnNuevaTabla = new Button("➕ Nueva tabla");
        btnNuevaTabla.setOnAction(e -> mostrarVentanaCrearTabla(stage));

        Button btnEliminarTabla = new Button("🗑️ Eliminar tabla");
        btnEliminarTabla.setOnAction(e -> mostrarVentanaEliminarTabla(stage));

        Button volver = new Button("⬅ Regresar");
        volver.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        volver.setOnAction(e -> {
            new Main().start(new Stage());
            stage.close();
        });

        HBox botonesExtras = new HBox(10, btnNuevaTabla, btnEliminarTabla);
        botonesExtras.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, titulo, botonesVBox, botonesExtras, volver);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-image: url('/images/fondo_login.jpg'); -fx-background-size: cover;");

        Scene scene = new Scene(layout, 1166, 651);
        stage.setTitle("Administrador - Hospital");
        stage.setScene(scene);
        stage.show();
    }

    private void cargarBotonesDesdeBD() {
        botonesVBox.getChildren().clear();
        tablasActuales.clear();

        try (Connection conn = Conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'")) {

            FlowPane flow = new FlowPane();
            flow.setHgap(10);
            flow.setVgap(10);
            flow.setAlignment(Pos.CENTER);

            while (rs.next()) {
                String tabla = rs.getString("TABLE_NAME");
                tablasActuales.add(tabla);

                Button boton = new Button(tabla);
                boton.setStyle("-fx-background-color: #2e8bfa; -fx-text-fill: white; -fx-font-weight: bold;");
                boton.setPrefWidth(150);

                boton.setOnAction(e -> new com.mycompany.hospital.controller.TablaViewWindow().mostrar(tabla));
                flow.getChildren().add(boton);
            }

            botonesVBox.getChildren().add(flow);

        } catch (SQLException e) {
            System.out.println("❌ Error cargando tablas: " + e.getMessage());
        }
    }

    private void mostrarVentanaCrearTabla(Stage dashboardStage) {
        Stage crearStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        TextField nombreTablaField = new TextField();
        nombreTablaField.setPromptText("Nombre de la nueva tabla");

        TextField columnasField = new TextField();
        columnasField.setPromptText("Ej: id INT PRIMARY KEY, nombre VARCHAR(50)");

        Button crearBtn = new Button("Crear");
        crearBtn.setOnAction(e -> {
            String nombre = nombreTablaField.getText().trim();
            String columnas = columnasField.getText().trim();

            if (!nombre.isEmpty() && !columnas.isEmpty()) {
                try (Connection conn = Conexion.connect();
                     Statement stmt = conn.createStatement()) {

                    String sql = "CREATE TABLE " + nombre + " (" + columnas + ")";
                    stmt.execute(sql);
                    System.out.println("✅ Tabla creada correctamente: " + nombre);
                    crearStage.close();
                    cargarBotonesDesdeBD();

                } catch (SQLException ex) {
                    mostrarAlerta("Error", "No se pudo crear la tabla:\n" + ex.getMessage());
                }
            } else {
                mostrarAlerta("Campo vacío", "Debes completar los dos campos.");
            }
        });

        layout.getChildren().addAll(new Label("Nueva tabla"), nombreTablaField, columnasField, crearBtn);
        crearStage.setScene(new Scene(layout, 400, 200));
        crearStage.setTitle("Crear nueva tabla");
        crearStage.show();
    }

    private void mostrarVentanaEliminarTabla(Stage dashboardStage) {
        Stage eliminarStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        ComboBox<String> comboTablas = new ComboBox<>();
        comboTablas.getItems().addAll(tablasActuales);

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setOnAction(e -> {
            String tabla = comboTablas.getValue();
            if (tabla != null) {
                try (Connection conn = Conexion.connect();
                     Statement stmt = conn.createStatement()) {

                    stmt.execute("DROP TABLE " + tabla);
                    System.out.println("🗑️ Tabla eliminada: " + tabla);
                    eliminarStage.close();
                    cargarBotonesDesdeBD();

                } catch (SQLException ex) {
                    mostrarAlerta("Error", "No se pudo eliminar la tabla:\n" + ex.getMessage());
                }
            }
        });

        layout.getChildren().addAll(new Label("Selecciona tabla a eliminar:"), comboTablas, eliminarBtn);
        eliminarStage.setScene(new Scene(layout, 400, 150));
        eliminarStage.setTitle("Eliminar tabla");
        eliminarStage.show();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}