/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package seguridad;

import dao.UsuarioDAO;
import excepciones.AutentificacionException;
import excepciones.DAOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import modelo.Usuarios;
/**
 *
 * @author Usuario
 */
public class AuthService {

    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_BLOQUEO = 15;
    private static final String MSG_GENERICO = "Usuario o contraseña incorrectos.";

    private static final String HASH_RELLENO =
            PasswordUtil.hashear("relleno-no-usar".toCharArray());

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuarios autenticar(String username, char[] clave)
            throws AutentificacionException, DAOException {

        if (username == null || username.isBlank() || clave == null || clave.length == 0) {
            throw new AutentificacionException(MSG_GENERICO);
        }

        Usuarios usuario = usuarioDAO.buscarPorUsername(username);

        if (usuario == null) {
            PasswordUtil.verificar(clave, HASH_RELLENO);
            throw new AutentificacionException(MSG_GENERICO);
        }
        if (!usuario.isActivo()) {
            throw new AutentificacionException(MSG_GENERICO);
        }

        LocalDateTime bloqueo = usuario.getBloqueadoHasta();
        if (bloqueo != null) {
            if (LocalDateTime.now().isBefore(bloqueo)) {
                throw new AutentificacionException(
                        "Cuenta bloqueada temporalmente. Intente de nuevo después de las "
                        + bloqueo.format(DateTimeFormatter.ofPattern("HH:mm")) + ".");
            }
            usuarioDAO.reiniciarIntentos(usuario.getId());
        }

        if (!PasswordUtil.verificar(clave, usuario.getPasswordHash())) {
            usuarioDAO.registrarIntentoFallido(usuario.getId(), MAX_INTENTOS, MINUTOS_BLOQUEO);
            throw new AutentificacionException(MSG_GENERICO);
        }

        usuarioDAO.reiniciarIntentos(usuario.getId());
        usuario.setPasswordHash(null);
        return usuario;
    }
}
