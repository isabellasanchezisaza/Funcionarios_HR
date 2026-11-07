/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;
import dao.UsuarioDAO;
import modelo.Usuarios;
import excepciones.DAOException;
/**
 *
 * @author Usuario
 */
public class PruebaSistemaCompleto {
  public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA DE USUARIOS Y SEGURIDAD ===");
        
        UsuarioDAO dao = new UsuarioDAO();
        
        try {
            // 1. Buscar usuario administrador por username
            Usuarios u = dao.buscarPorUsername("admin");
            if (u == null) {
                System.out.println("No existe el usuario admin.");
                return;
            }
            
            System.out.println("Usuario encontrado: " + u.getUsername() + " | Rol: " + u.getRol() + " | Intentos fallidos iniciales: " + u.getIntentosFallidos());
            
            // 2. Simular intentos fallidos para probar el bloqueo (Ej: 3 intentos)
            System.out.println("\nSimulando 3 intentos fallidos...");
            for (int i = 1; i <= 3; i++) {
                dao.registrarIntentoFallido(u.getId(), 3, 5); // Máximo 3 intentos, 5 minutos de bloqueo
            }
            
            // Volver a consultar el estado del usuario para ver el bloqueo
            u = dao.buscarPorUsername("admin");
            System.out.println("Intentos fallidos actualizados: " + u.getIntentosFallidos());
            System.out.println("Bloqueado hasta: " + u.getBloqueadoHasta());
            
            // 3. Limpiar / reiniciar los intentos para dejar la prueba limpia
            dao.reiniciarIntentos(u.getId());
            u = dao.buscarPorUsername("admin");
            System.out.println("\nDespués de reiniciar: Intentos = " + u.getIntentosFallidos() + " | Bloqueo = " + u.getBloqueadoHasta());
            
        } catch (DAOException e) {
            System.out.println("Error en la base de datos: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Causa: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("=== FIN DE LA PRUEBA ===");
    }
}
