package MX.unison;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Esto asegura que la interfaz gráfica se ejecute en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            // Crear e iniciar la ventana de login
            UnisonApp login = new UnisonApp();
            login.setVisible(true);
        });
    }
}