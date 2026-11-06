/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import dao.UsuarioDAO;
import excepciones.DAOException;
import modelo.Usuarios;

/**
 *
 * @author Isabella
 */
public class PruebaUsuarioDAO {
    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();
        try {
            Usuarios u = dao.buscarPorUsername("admin");
            if (u == null) {
                System.out.println("No existe el usuario admin.");
                return;
            }
            System.out.println(u.getUsername() + " - rol: " + u.getRol()
                    + " - intentos: " + u.getIntentosFallidos());

            for (int i = 1; i <= 3; i++) {                  // 3 fallos, máximo 3, 1 minuto
                dao.registrarIntentoFallido(u.getId(), 3, 1);
            }
            u = dao.buscarPorUsername("admin");
            System.out.println("Intentos: " + u.getIntentosFallidos()
                    + " - bloqueado hasta: " + u.getBloqueadoHasta());

            dao.reiniciarIntentos(u.getId());                // limpiar la prueba
            u = dao.buscarPorUsername("admin");
            System.out.println("Después de reiniciar: " + u.getIntentosFallidos()
                    + " - " + u.getBloqueadoHasta());
        } catch (DAOException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Causa: " + e.getCause().getMessage());
            }
        }
    }
}
