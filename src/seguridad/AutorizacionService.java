/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package seguridad;
import excepciones.AutorizacionException;
/**
 *
 * @author Usuario
 */
public class AutorizacionService {
    public static void verificarRol (String rolRequerido) throws AutorizacionException {
        if (!SesionUsuario.estaAutenticado()) {
            throw new AutorizacionException("Acceso denegado. No hay sesión activa.");
        }
        
        String rolActual = SesionUsuario.getRolActual();
        if (rolActual == null|| !rolActual.equalsIgnoreCase(rolRequerido)) {
            throw new AutorizacionException("Acceso no autorizado. Se requiere el rol: " + rolRequerido + "(Su rol actual es: " + rolActual + ")");
        }
    }
    public static boolean tieneRol(String rolRequerido) {
        String rolActual =SesionUsuario.getRolActual();
        return rolActual != null && rolActual.equalsIgnoreCase(rolRequerido);
    }
}

