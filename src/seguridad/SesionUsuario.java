/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package seguridad;
import modelo.Usuarios;
/**
 *
 * @author Usuario
 */
public final class SesionUsuario {

    public static final int TIMEOUT_SEGUNDOS = 15 * 60;

    private static Usuarios usuarioActual;
    private static long ultimaActividadNs;

    private SesionUsuario() { }

    public static synchronized void iniciarSesion(Usuarios usuario) {
        usuarioActual = usuario;
        ultimaActividadNs = System.nanoTime();
    }

    public static synchronized void cerrarSesion() {
        usuarioActual = null;
    }

    public static synchronized long segundosRestantes() {
        if (usuarioActual == null) {
            return 0;
        }
        long transcurridos = (System.nanoTime() - ultimaActividadNs) / 1_000_000_000L;
        return Math.max(0, TIMEOUT_SEGUNDOS - transcurridos);
    }

    public static synchronized boolean estaAutenticado() {
        if (usuarioActual == null) {
            return false;
        }
        if (segundosRestantes() == 0) {
            cerrarSesion();
            return false;
        }
        return true;
    }

    public static synchronized void actualizarActividad() {
        if (usuarioActual != null && segundosRestantes() > 0) {
            ultimaActividadNs = System.nanoTime();
        }
    }

    public static synchronized Usuarios getUsuarioActual() {
        return usuarioActual;
    }

    public static synchronized String getNombreUsuario() {
        return usuarioActual != null ? usuarioActual.getUsername() : "desconocido";
    }

    public static synchronized String getRolActual() {
        if (usuarioActual != null && usuarioActual.getRol() != null) {
            return usuarioActual.getRol().getNombre();
        }
        return null;
    }

    public static synchronized boolean tienePermiso(Permiso permiso) {
        if (!estaAutenticado()) {
            return false;
        }
        String rol = getRolActual();
        if (rol == null) {
            return false;
        }
        return switch (rol.toUpperCase()) {
            case "ADMIN" -> true;
            case "RRHH" -> permiso != Permiso.ELIMINAR;
            case "CONSULTOR" -> permiso == Permiso.LISTAR || permiso == Permiso.VER;
            default -> false;
        };
    }
}