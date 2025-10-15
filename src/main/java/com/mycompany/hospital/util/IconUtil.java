package com.mycompany.hospital.util;

import javafx.stage.Stage;
import javafx.scene.image.Image;

public class IconUtil {

    // 🟩 Para ventanas JavaFX (Stage)
    public static void aplicarIcono(Stage stage) {
        try {
            Image icono = new Image(IconUtil.class.getResource("/images/icon.png").toExternalForm());
            stage.getIcons().add(icono);
        } catch (Exception e) {
            System.out.println("❌ Error al aplicar ícono a Stage: " + e.getMessage());
        }
    }
}


