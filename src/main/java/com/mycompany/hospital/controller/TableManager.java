package com.mycompany.hospital.controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.mycompany.hospital.util.Conexion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.Vector;

public class TableManager {
    private final String tableName;
    private final int medicoId;
    private JTable table;
    private DefaultTableModel model;
    private final JPanel panel;

    public TableManager(String tableName, int medicoId) {
        this.tableName = tableName;
        this.medicoId = medicoId;
        this.panel = new JPanel(new BorderLayout());
        cargarDatos();
    }

    public JPanel getPanel() {
        return panel;
    }

    private void cargarDatos() {
        panel.removeAll();
        try (Connection conn = Conexion.connect()) {
            String sql = construirConsultaSQL(tableName);
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (requiereFiltroMedico(tableName)) {
                stmt.setInt(1, medicoId);
            }
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            Vector<String> columnNames = new Vector<>();
            int columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(meta.getColumnName(i).replace("_", " "));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }

            model = new DefaultTableModel(data, columnNames) {
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };

            table = new JTable(model);
            estilizarTabla(table);

            JScrollPane scroll = new JScrollPane(table);

            // Botones CRUD
            JPanel botones = new JPanel();
            JButton btnAgregar = new JButton("➕ Agregar");
            JButton btnEliminar = new JButton("🗑 Eliminar");
            JButton btnActualizar = new JButton("✏ Actualizar");
            JButton btnExportar = new JButton("📁 Exportar Excel");

            btnAgregar.addActionListener(e -> agregarRegistro());
            btnEliminar.addActionListener(e -> eliminarRegistro());
            btnActualizar.addActionListener(e -> actualizarRegistro());
            btnExportar.addActionListener(e -> exportarExcel());

            botones.add(btnAgregar);
            botones.add(btnEliminar);
            botones.add(btnActualizar);
            botones.add(btnExportar);

            panel.add(scroll, BorderLayout.CENTER);
            panel.add(botones, BorderLayout.SOUTH);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Error al cargar datos: " + e.getMessage());
        }

        panel.revalidate();
        panel.repaint();
    }

    private String construirConsultaSQL(String tabla) {
        return switch (tabla.toLowerCase()) {
            case "cita" -> "SELECT cita_id AS ID, fecha_hora, estado, motivo, paciente_id FROM Cita WHERE medico_id = ?";
            case "consulta" -> "SELECT consulta_id AS ID, paciente_id, fecha_consulta, motivo FROM Consulta WHERE medico_id = ?";
            case "diagnostico" -> "SELECT d.diagnostico_id, d.descripcion, p.nombre + ' ' + p.apellidos AS Paciente " +
                    "FROM Diagnostico d " +
                    "JOIN Consulta c ON d.consulta_id = c.consulta_id " +
                    "JOIN Paciente p ON c.paciente_id = p.paciente_id " +
                    "WHERE c.medico_id = ?";
            case "hospitalizacion" -> "SELECT h.hospitalizacion_id, s.ubicacion AS sala, h.fecha_ingreso, h.fecha_alta, h.motivo " +
                    "FROM Hospitalizacion h JOIN Sala s ON h.sala_id = s.sala_id " +
                    "WHERE h.medico_responsable = ?";
            case "tratamiento" -> "SELECT t.tratamiento_id, t.descripcion, t.duracion_dias, c.paciente_id " +
                    "FROM Tratamiento t JOIN Consulta c ON t.consulta_id = c.consulta_id " +
                    "WHERE c.medico_id = ?";
            case "prueba" -> "SELECT p.prueba_id, p.tipo_prueba_id, p.fecha_solicitud, p.resultado, pa.nombre + ' ' + pa.apellidos AS Paciente " +
                    "FROM Prueba p JOIN Paciente pa ON p.paciente_id = pa.paciente_id " +
                    "JOIN Consulta c ON p.consulta_id = c.consulta_id " +
                    "WHERE c.medico_id = ?";
            case "paciente" -> "SELECT paciente_id, nombre, apellidos, fecha_nacimiento, genero, telefono, email, numero_seguridad_social FROM Paciente";
            default -> "SELECT * FROM " + tabla;
        };
    }

    private boolean requiereFiltroMedico(String tabla) {
        return switch (tabla.toLowerCase()) {
            case "cita", "consulta", "diagnostico", "hospitalizacion", "prueba", "tratamiento" -> true;
            default -> false;
        };
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(24);
        tabla.setGridColor(Color.LIGHT_GRAY);
        tabla.setShowVerticalLines(true);
        tabla.setSelectionBackground(new Color(184, 207, 229));
        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(60, 120, 180));
        header.setForeground(Color.white);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    public void agregarRegistro() {
        new com.mycompany.hospital.view.DynamicForm(tableName, this::cargarDatos);
    }

    public void eliminarRegistro() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona una fila para eliminar.");
            return;
        }

        Object pkValue = model.getValueAt(row, 0);
        String pk = model.getColumnName(0).replace(" ", "_");

        try (Connection conn = Conexion.connect()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + tableName + " WHERE " + pk + " = ?");
            stmt.setObject(1, pkValue);
            stmt.executeUpdate();
            cargarDatos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Error al eliminar: " + e.getMessage());
        }
    }

    public void actualizarRegistro() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona una fila para actualizar.");
            return;
        }

        String pk = model.getColumnName(0).replace(" ", "_");
        Object pkValue = model.getValueAt(row, 0);

        try (Connection conn = Conexion.connect()) {
            StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
            for (int i = 1; i < model.getColumnCount(); i++) {
                sql.append(model.getColumnName(i).replace(" ", "_")).append(" = ?");
                if (i < model.getColumnCount() - 1) sql.append(", ");
            }
            sql.append(" WHERE ").append(pk).append(" = ?");

            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            for (int i = 1; i < model.getColumnCount(); i++) {
                stmt.setObject(i, model.getValueAt(row, i));
            }
            stmt.setObject(model.getColumnCount(), pkValue);
            stmt.executeUpdate();
            cargarDatos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Error al actualizar: " + e.getMessage());
        }
    }

    public void exportarExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Excel");
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;

        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Datos");

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            for (int i = 0; i < model.getColumnCount(); i++) {
                header.createCell(i).setCellValue(model.getColumnName(i));
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object value = model.getValueAt(i, j);
                    row.createCell(j).setCellValue(value != null ? value.toString() : "");
                }
            }

            FileOutputStream fileOut = new FileOutputStream(chooser.getSelectedFile() + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            JOptionPane.showMessageDialog(null, "✅ Datos exportados a Excel.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Error exportando a Excel: " + e.getMessage());
        }
    }

    public void buscarPorNSS(String nss) {
        try (Connection conn = Conexion.connect()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Paciente WHERE numero_seguridad_social = ?");
            stmt.setString(1, nss);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String info = "📋 DATOS DEL PACIENTE:\n" +
                        "👤 Nombre: " + rs.getString("nombre") + " " + rs.getString("apellidos") + "\n" +
                        "🗓 Fecha de nacimiento: " + rs.getDate("fecha_nacimiento") + "\n" +
                        "🧬 Género: " + rs.getString("genero") + "\n" +
                        "📞 Teléfono: " + rs.getString("telefono") + "\n" +
                        "✉️ Email: " + rs.getString("email") + "\n" +
                        "🏠 Dirección: " + rs.getString("direccion") + "\n" +
                        "🪪 NSS: " + rs.getString("numero_seguridad_social");

                JOptionPane.showMessageDialog(null, info, "Paciente encontrado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "⚠ No se encontró ningún paciente con ese número de seguridad social.", "Sin resultados", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Error al buscar el paciente: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void exportarHistorialPDF(String nss) {
        try (Connection conn = Conexion.connect()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Paciente WHERE numero_seguridad_social = ?");
            stmt.setString(1, nss);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Guardar historial PDF");
                if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;

                String nombre = rs.getString("nombre") + " " + rs.getString("apellidos");
                String nacimiento = rs.getString("fecha_nacimiento");
                String tel = rs.getString("telefono");
                String mail = rs.getString("email");
                String genero = rs.getString("genero");
                String direccion = rs.getString("direccion");

                String path = chooser.getSelectedFile().getAbsolutePath();

                PdfWriter writer = new PdfWriter(path + ".pdf");
                PdfDocument pdf = new PdfDocument(writer);
                Document doc = new Document(pdf);

                doc.add(new Paragraph("🏥 HISTORIAL CLÍNICO DEL PACIENTE").setBold().setFontSize(16));
                doc.add(new Paragraph("Nombre: " + nombre));
                doc.add(new Paragraph("Fecha nacimiento: " + nacimiento));
                doc.add(new Paragraph("Género: " + genero));
                doc.add(new Paragraph("Teléfono: " + tel));
                doc.add(new Paragraph("Email: " + mail));
                doc.add(new Paragraph("Dirección: " + direccion));
                doc.add(new Paragraph(" "));

                doc.add(new Paragraph("Psdt: pasenos el semestre porfa ;(((, LOS QUEREMOS "));
                doc.close();

                JOptionPane.showMessageDialog(null, "✅ PDF generado correctamente.");

            } else {
                JOptionPane.showMessageDialog(null, "⚠ No se encontró el paciente.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Error al generar PDF: " + e.getMessage());
        }
    }
}
