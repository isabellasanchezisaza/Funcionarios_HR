/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import javax.swing.SwingUtilities;

/**
 *
 * @author maris
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Estilo.instalar();
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);          // se queda aquí hasta que cierren el login
            if (login.isAutenticado()) {
                new VentanaPrincipal().setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
