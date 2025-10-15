package Application;

import com.mycompany.hospital.util.Conexion;
import com.mycompany.hospital.util.IconUtil;
import com.mycompany.hospital.view.AdminDashboard;
import com.mycompany.hospital.view.MedicoDashboard;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.sql.*;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        //  Aplica ícono a esta ventana
        IconUtil.aplicarIcono(stage);

        try (Connection conn = Conexion.connect()) {
            System.out.println(" SE HA CONECTADO A LA BASE DE DATOS DE FORMA EXITOSA ");
        } catch (SQLException e) {
            System.out.println(" Error al conectar a SQL Server: " + e.getMessage());
        }

        // Logo
        ImageView logo = new ImageView(new Image(getClass().getResource("/images/logo.jpg").toExternalForm()));
        logo.setFitWidth(140);
        logo.setFitHeight(80);
        Rectangle logoClip = new Rectangle(140, 80);
        logoClip.setArcWidth(30);
        logoClip.setArcHeight(30);
        logo.setClip(logoClip);
        StackPane logoPane = new StackPane(logo);
        logoPane.setAlignment(Pos.TOP_RIGHT);
        logoPane.setPadding(new Insets(10, 10, 0, 0));

        // Título
        Label title = new Label("Hospital Baca Ortiz");
        title.getStyleClass().add("label-title");

        // Usuario
        ImageView iconUser = new ImageView(new Image(getClass().getResource("/images/usuario.jpg").toExternalForm()));
        iconUser.setFitWidth(22);
        iconUser.setFitHeight(22);
        Rectangle clipUser = new Rectangle(22, 22);
        clipUser.setArcWidth(20);
        clipUser.setArcHeight(20);
        iconUser.setClip(clipUser);
        Label userLabel = new Label("Agrega tu nombre:");
        userLabel.getStyleClass().add("login-label");
        HBox userBox = new HBox(5, iconUser, userLabel);
        userBox.setAlignment(Pos.CENTER_LEFT);

        // Contraseña
        ImageView iconKey = new ImageView(new Image(getClass().getResource("/images/llave.jpg").toExternalForm()));
        iconKey.setFitWidth(22);
        iconKey.setFitHeight(22);
        Rectangle clipKey = new Rectangle(22, 22);
        clipKey.setArcWidth(20);
        clipKey.setArcHeight(20);
        iconKey.setClip(clipKey);
        Label passLabel = new Label("Ingresa tu contraseña:");
        passLabel.getStyleClass().add("login-label");
        HBox passBox = new HBox(5, iconKey, passLabel);
        passBox.setAlignment(Pos.CENTER_LEFT);

        TextField usernameField = new TextField();
        usernameField.setPromptText("User Name");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("password-field");

        CheckBox rememberMe = new CheckBox("Remember me");
        Hyperlink forgotPassword = new Hyperlink("Forgot password?");
        HBox bottomOptions = new HBox(10, rememberMe, forgotPassword);
        bottomOptions.setAlignment(Pos.CENTER);

        Button loginButton = new Button("LOG IN");
        loginButton.getStyleClass().add("button-login");

        VBox form = new VBox(12, title, userBox, usernameField, passBox, passwordField, bottomOptions, loginButton);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40));

        BorderPane root = new BorderPane();
        root.setTop(logoPane);
        root.setCenter(form);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-image: url('/images/fondo_login.jpg'); -fx-background-size: cover;");

        Scene loginScene = new Scene(root, 800, 600);
        loginScene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        stage.setTitle("Login - Hospital");
        stage.setScene(loginScene);
        stage.show();

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                mostrarAlerta("Campos incompletos", "Por favor, ingresa usuario y contraseña.");
                return;
            }

            // ADMIN
            if (username.equals("proyectobdd") && password.equals("root")) {
                Stage adminStage = new Stage();
                IconUtil.aplicarIcono(adminStage);
                new AdminDashboard().start(adminStage);
                stage.close();
                return;
            }

            try (Connection conn = Conexion.connect()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT usuario_id FROM Usuario WHERE username = ? AND password_hash = ? AND activo = 1"
                );
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int usuarioId = rs.getInt("usuario_id");

                    PreparedStatement medicoStmt = conn.prepareStatement(
                            "SELECT m.medico_id, m.nombre, m.apellidos, e.nombre AS especialidad " +
                                    "FROM Medico m " +
                                    "JOIN Medico_Especialidad me ON m.medico_id = me.medico_id " +
                                    "JOIN Especialidad e ON me.especialidad_id = e.especialidad_id " +
                                    "WHERE m.usuario_id = ?"
                    );
                    medicoStmt.setInt(1, usuarioId);
                    ResultSet medRs = medicoStmt.executeQuery();

                    if (medRs.next()) {
                        int medicoId = medRs.getInt("medico_id");
                        String nombre = medRs.getString("nombre") + " " + medRs.getString("apellidos");
                        String especialidad = medRs.getString("especialidad");

                        Stage medicoStage = new Stage();
                        IconUtil.aplicarIcono(medicoStage);
                        new MedicoDashboard(medicoId, nombre, especialidad).mostrar(medicoStage);
                        stage.close();
                    } else {
                        mostrarAlerta("Acceso denegado", "Este usuario no tiene perfil de médico.");
                    }

                } else {
                    mostrarAlerta("Credenciales inválidas", "Usuario o contraseña incorrectos.");
                }

            } catch (SQLException ex) {
                mostrarAlerta("Error", "No se pudo conectar: " + ex.getMessage());
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}