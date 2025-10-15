package com.mycompany.hospital.view;

import com.mycompany.hospital.controller.TableManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class MedicoDashboard {

    private final int medicoId;
    private final String nombre;
    private final String especialidad;

    public MedicoDashboard(int medicoId, String nombre, String especialidad) {
        this.medicoId = medicoId;
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    public void mostrar(Stage stage) {
        javafx.scene.control.Label saludo = new javafx.scene.control.Label("👨‍⚕️ Bienvenido, Dr(a). " + nombre);
        saludo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: navy;");

        javafx.scene.control.Label especialidadLbl = new javafx.scene.control.Label("Especialidad: " + especialidad);
        especialidadLbl.setStyle("-fx-font-size: 18px; -fx-text-fill: White;");

        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Número de Seguridad Social...");

        Button btnBuscar = new Button("🔍 Buscar paciente");
        btnBuscar.setStyle("-fx-background-color: #3d85c6; -fx-text-fill: white;");

        Button btnExportarPDF = new Button("📄 Exportar a PDF");
        btnExportarPDF.setStyle("-fx-background-color: #6fa8dc; -fx-text-fill: white;");

        HBox searchBox = new HBox(10, txtBuscar, btnBuscar, btnExportarPDF);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(10));

        btnBuscar.setOnAction(e -> {
            String nss = txtBuscar.getText().trim();
            if (!nss.isEmpty()) {
                TableManager manager = new TableManager("Paciente", medicoId);
                manager.buscarPorNSS(nss); // Muestra información detallada del paciente
            } else {
                JOptionPane.showMessageDialog(null, "Por favor, ingresa el número de seguridad social.");
            }
        });

        btnExportarPDF.setOnAction(e -> {
            String nss = txtBuscar.getText().trim();
            if (!nss.isEmpty()) {
                TableManager manager = new TableManager("Paciente", medicoId);
                manager.exportarHistorialPDF(nss); // Genera PDF con historial clínico
            } else {
                JOptionPane.showMessageDialog(null, "Ingresa el número de seguridad social.");
            }
        });

        List<String> tablas = Arrays.asList(
                "Cirugia", "Cita", "Consulta", "Diagnostico",
                "Hospitalizacion", "Paciente", "Prueba", "Sala",
                "Tipo_Prueba", "Tratamiento"
        );

        FlowPane flow = new FlowPane();
        flow.setHgap(15);
        flow.setVgap(15);
        flow.setPadding(new Insets(20));
        flow.setAlignment(Pos.CENTER);

        for (String tabla : tablas) {
            Button btn = new Button(tabla);
            btn.setStyle("-fx-background-color: #2e8bfa; -fx-text-fill: white; -fx-font-weight: bold;");
            btn.setPrefWidth(140);
            btn.setOnAction(e -> abrirTabla(tabla));
            flow.getChildren().add(btn);
        }

        Button btnVolver = new Button("⬅ Volver al login");
        btnVolver.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        btnVolver.setOnAction(e -> {
            Stage loginStage = new Stage();
            new Application.Main().start(loginStage);
            stage.close();
        });

        VBox layout = new VBox(20, saludo, especialidadLbl, searchBox, flow, btnVolver);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #f9f9f9, #d3eafd);");

        Scene scene = new Scene(layout, 950, 650);
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        stage.setTitle("Dashboard Médico");
        stage.setScene(scene);
        stage.show();
    }

    private void abrirTabla(String nombreTabla) {
        SwingUtilities.invokeLater(() -> {
            TableManager manager = new TableManager(nombreTabla, medicoId);
            JPanel panel = manager.getPanel();
            JFrame frame = new JFrame("📋 " + nombreTabla);
            frame.setContentPane(panel);
            frame.setSize(1000, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}