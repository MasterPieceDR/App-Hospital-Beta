package com.mycompany.hospital.controller;

import com.mycompany.hospital.util.Conexion;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

public class TablaViewWindow {

    public void mostrar(String tabla) {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label titulo = new Label("Tabla: " + tabla);
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Map<String, Object>> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try (Connection conn = Conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabla)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                final int colIndex = i;
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(meta.getColumnName(i));
                column.setCellValueFactory(cellData -> {
                    Object value = null;
                    try {
                        value = cellData.getValue().get(meta.getColumnName(colIndex));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return new javafx.beans.property.SimpleStringProperty(value == null ? "" : value.toString());
                });
                tableView.getColumns().add(column);
            }

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                tableView.getItems().add(row);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error cargando datos: " + e.getMessage());
        }

        Button btnAgregar = new Button("➕ Agregar");
        btnAgregar.setOnAction(e -> mostrarFormularioInsertar(tabla, stage));

        Button btnEliminar = new Button("🗑️ Eliminar");
        btnEliminar.setOnAction(e -> {
            Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            try (Connection conn = Conexion.connect()) {
                Statement stmt = conn.createStatement();

                String pk = selected.keySet().iterator().next();
                Object pkValue = selected.get(pk);

                String sql = "DELETE FROM " + tabla + " WHERE " + pk + " = '" + pkValue + "'";
                stmt.executeUpdate(sql);
                System.out.println("🗑️ Registro eliminado");
                stage.close();
                mostrar(tabla);

            } catch (SQLException ex) {
                System.out.println("❌ Error al eliminar: " + ex.getMessage());
            }
        });

        HBox botones = new HBox(10, btnAgregar, btnEliminar);
        botones.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(titulo, tableView, botones);

        Scene scene = new Scene(root, 800, 500);
        stage.setTitle("Tabla: " + tabla);
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarFormularioInsertar(String tabla, Stage parentStage) {
        Stage stage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        List<TextField> campos = new ArrayList<>();
        List<String> nombresColumnas = new ArrayList<>();
        List<Boolean> columnasAutoIncrement = new ArrayList<>();

        ComboBox<String> usuarioComboBox = new ComboBox<>();
        boolean incluirUsuarioCombo = false;

        try (Connection conn = Conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabla + " WHERE 1=0")) {

            ResultSetMetaData meta = rs.getMetaData();

            for (int i = 1; i <= meta.getColumnCount(); i++) {
                boolean esAuto = meta.isAutoIncrement(i);
                String nombre = meta.getColumnName(i);

                columnasAutoIncrement.add(esAuto);

                if (esAuto) continue;

                if (nombre.equalsIgnoreCase("usuario_id")) {
                    incluirUsuarioCombo = true;
                    Label label = new Label("usuario_id");
                    usuarioComboBox.setPromptText("Seleccione un usuario válido");

                    try (Statement userStmt = conn.createStatement();
                         ResultSet rsUsuarios = userStmt.executeQuery("SELECT usuario_id, username FROM Usuario")) {
                        while (rsUsuarios.next()) {
                            int id = rsUsuarios.getInt("usuario_id");
                            String username = rsUsuarios.getString("username");
                            usuarioComboBox.getItems().add(id + " - " + username);
                        }
                    }

                    vbox.getChildren().addAll(label, usuarioComboBox);
                    nombresColumnas.add("usuario_id");
                    campos.add(new TextField()); // Placeholder
                } else {
                    Label label = new Label(nombre);
                    TextField tf = new TextField();
                    tf.setPromptText(meta.getColumnTypeName(i));
                    campos.add(tf);
                    nombresColumnas.add(nombre);
                    vbox.getChildren().addAll(label, tf);
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Error cargando columnas: " + e.getMessage());
            return;
        }

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> {
            StringBuilder columnasSQL = new StringBuilder();
            StringBuilder valoresSQL = new StringBuilder();

            for (int i = 0; i < campos.size(); i++) {
                String nombreCol = nombresColumnas.get(i);
                String valor;

                if (nombreCol.equalsIgnoreCase("usuario_id")) {
                    if (usuarioComboBox.getValue() == null) {
                        System.out.println("❌ Debes seleccionar un usuario");
                        return;
                    }
                    valor = usuarioComboBox.getValue().split(" - ")[0];
                } else {
                    valor = campos.get(i).getText();
                }

                columnasSQL.append(nombreCol);
                valoresSQL.append("'").append(valor).append("'");
                if (i < campos.size() - 1) {
                    columnasSQL.append(", ");
                    valoresSQL.append(", ");
                }
            }

            String sql = "INSERT INTO " + tabla + " (" + columnasSQL + ") VALUES (" + valoresSQL + ")";
            try (Connection conn = Conexion.connect();
                 Statement insertStmt = conn.createStatement()) {
                insertStmt.execute(sql);
                System.out.println("✅ Registro insertado");
                stage.close();
                parentStage.close();
                mostrar(tabla);
            } catch (SQLException ex) {
                System.out.println("❌ Error al insertar: " + ex.getMessage());
            }
        });

        vbox.getChildren().add(btnGuardar);
        stage.setScene(new Scene(vbox, 400, 500));
        stage.setTitle("Agregar registro en " + tabla);
        stage.show();
    }
}