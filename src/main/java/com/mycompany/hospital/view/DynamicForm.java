package com.mycompany.hospital.view;

import com.mycompany.hospital.util.Conexion;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DynamicForm extends JFrame {

    private final String tableName;
    private final Map<String, JComponent> fieldComponents = new HashMap<>();
    private final Runnable onCloseCallback;

    public DynamicForm(String tableName, Runnable onCloseCallback) {
        this.tableName = tableName;
        this.onCloseCallback = onCloseCallback;

        setTitle("➕ Agregar registro a " + tableName);
        setSize(520, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // setIconImage(new ImageIcon(getClass().getResource("/images/icon.png")).getImage()); // opcional
        construirFormulario();
        setVisible(true);
    }

    private void construirFormulario() {
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        try (Connection conn = Conexion.connect()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " WHERE 1 = 0");
            ResultSetMetaData meta = rs.getMetaData();

            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (meta.isAutoIncrement(i)) continue;

                String columnName = meta.getColumnName(i);
                int columnType = meta.getColumnType(i);

                JLabel label = new JLabel(columnName.replace("_", " ") + ":");
                JComponent field;

                switch (columnType) {
                    case Types.DATE -> field = new JTextField("yyyy-MM-dd");
                    case Types.BIT -> field = new JCheckBox();
                    default -> field = new JTextField();
                }

                fieldComponents.put(columnName, field);
                formPanel.add(label);
                formPanel.add(field);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error cargando campos: " + e.getMessage());
            dispose();
            return;
        }

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarDatos());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnGuardar);

        add(new JScrollPane(formPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void guardarDatos() {
        try (Connection conn = Conexion.connect()) {
            StringBuilder columnas = new StringBuilder();
            StringBuilder valores = new StringBuilder();

            for (String key : fieldComponents.keySet()) {
                columnas.append(key).append(",");
                valores.append("?,");
            }

            columnas.setLength(columnas.length() - 1);
            valores.setLength(valores.length() - 1);

            String sql = "INSERT INTO " + tableName + " (" + columnas + ") VALUES (" + valores + ")";
            PreparedStatement stmt = conn.prepareStatement(sql);

            int index = 1;
            for (String key : fieldComponents.keySet()) {
                JComponent comp = fieldComponents.get(key);

                if (comp instanceof JTextField field) {
                    String value = field.getText().trim();

                    // Validaciones específicas para tabla Paciente
                    if (tableName.equalsIgnoreCase("Paciente")) {
                        switch (key) {
                            case "telefono":
                                if (!value.matches("\\d+")) {
                                    JOptionPane.showMessageDialog(this, "⚠ El número de teléfono debe contener solo números.");
                                    return;
                                }
                                break;
                            case "email":
                                if (!value.contains("@")) {
                                    JOptionPane.showMessageDialog(this, "⚠ El correo electrónico debe contener un '@'.");
                                    return;
                                }
                                break;
                            case "numero_seguridad_social":
                                if (value.isEmpty()) {
                                    JOptionPane.showMessageDialog(this, "⚠ El número de seguridad social no puede estar vacío.");
                                    return;
                                }
                                break;
                        }
                    }

                    if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        stmt.setDate(index, Date.valueOf(value));
                    } else if (value.matches("-?\\d+")) {
                        stmt.setInt(index, Integer.parseInt(value));
                    } else {
                        stmt.setString(index, value);
                    }

                } else if (comp instanceof JCheckBox checkBox) {
                    stmt.setBoolean(index, checkBox.isSelected());
                } else {
                    stmt.setNull(index, Types.NULL);
                }

                index++;
            }

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Registro insertado correctamente.");
            if (onCloseCallback != null) onCloseCallback.run();
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error al insertar: " + e.getMessage());
        }
    }
}