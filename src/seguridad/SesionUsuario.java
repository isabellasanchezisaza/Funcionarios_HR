/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package seguridad;
import modelo.Usuarios;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
/**
 *
 * @author Usuario
 */
public class SesionUsuario {
    private static Usuarios usuarioActual = null;
    private static LocalDateTime ultimaActividad = null;
    private static final int TIMEOUT_MINUTOS = 15;
    
    public static void IniciarSesión(Usuarios usuario) {
        usuarioActual = usuario;
        ultimaActividad = LocalDateTime.now();
    }
    
    public static void cerrarSesion() {
        usuarioActual = null;
        ultimaActividad = null;
    }
    
    public static Usuarios getUsuarioActual() {
        actualizarActividad();
        return usuarioActual;
    }
    
    public static boolean estaAutenticado() {
        if (usuarioActual == null) {
        return false;
    }
        
        if(ultimaActividad != null && ChronoUnit.MINUTES.between(ultimaActividad, LocalDateTime.now()) >= TIMEOUT_MINUTOS) {
        cerrarSesion();
        return false;
    }
    
        actualizarActividad();
        return true;
    }
    
        public static void actualizarActividad() {
            if (usuarioActual != null) {
                ultimaActividad = LocalDateTime.now();
            }
        }
        
    public static String getRolActual() {
        if (usuarioActual != null && usuarioActual.getRol() != null) {
        return usuarioActual.getRol().getNombre();
    }
    return null;
  }
    
    public static boolean tienePermiso(String permiso) {
        String rol = getRolActual();
        if (rol == null) return false;
        
        rol = rol.toUpperCase();
        permiso = permiso.toUpperCase();
        
        return switch (rol) {
            case "ADMIN" -> true;
            case "RRHH" -> !permiso.equals("ELIMINAR");
            case "CONSULTOR" -> permiso.equals("LISTAR") || permiso.equals("VER");
            default -> false;
        };
    }
}