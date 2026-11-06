/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package seguridad;
import dao.UsuarioDAO;
import excepciones.AutentificacionException;
import excepciones.DAOException;
import modelo.Usuarios;
import java.time.LocalDateTime;
/**
 *
 * @author Usuario
 */
public class AuthService {
   private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public Usuarios autenticar(String username, String passwordIngresada) throws AutentificacionException, DAOException {
        Usuarios usuario = usuarioDAO.buscarPorUsername(username);
        
        if (usuario == null) {
            throw new AutentificacionException("El usuario o la contraseña son incorrectos.");
        }

        if (!usuario.isActivo()) {
            throw new AutentificacionException("La cuenta está desactivada. Comuníquese con el administrador.");
        }

        if (usuario.getBloqueadoHasta() != null) {
            if (LocalDateTime.now().isBefore(usuario.getBloqueadoHasta())) {
                throw new AutentificacionException("La cuenta está bloqueada temporalmente hasta: " + usuario.getBloqueadoHasta());
            } else {
                usuarioDAO.reiniciarIntentos(usuario.getId());
            }
        }

        boolean passwordValida = usuario.getPasswordHash().equals(passwordIngresada);

        if (!passwordValida) {
            usuarioDAO.registrarIntentoFallido(usuario.getId(), 3, 15);
            throw new AutentificacionException("El usuario o la contraseña son incorrectos.");
        }

        usuarioDAO.reiniciarIntentos(usuario.getId());

        return usuario;
    }
}
