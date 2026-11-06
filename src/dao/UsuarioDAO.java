/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.Conexion;
import excepciones.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import modelo.Roles;
import modelo.Usuarios;

/**
 *
 * @author Isabella
 */
public class UsuarioDAO {
    
    private static final String SQL_BUSCAR_POR_USERNAME =
            "SELECT u.id, u.username, u.password_hash, u. activo, "
            + "u.intentos_fallidos, u.bloqueado_hasta, "
            + "r.id AS id_rol, r.nombre AS rol "
            + "FROM usuarios u JOIN roles r ON r.id = u.id_rol "
            + "WHERE u.username = ?";
    
    private static final String SQL_INTENTO_FALLIDO =
            "UPDATE usuarios SET intentos_fallidos = intentos_fallidos + 1, "
            + "bloqueado_hasta = CASE WHEN intentos_fallidos + 1 >= ? "
            + "THEN NOW() + make_interval(mins => ?) ELSE bloqueado_hasta END "
            + "WHERE id = ?";
    
    private static final String SQL_REINICIAR_INTENTOS =
            "UPDATE usuarios SET intentos_fallidos = 0, bloqueado_hasta = NULL "
            + "WHERE id = ?";
    
    
    public Usuarios buscarPorUsername(String username) throws DAOException {
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_USERNAME)){
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()){
                if (!rs.next()){
                    return null;
                }
                Usuarios u = new Usuarios();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setActivo(rs.getBoolean("activo"));
                u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
                
                Timestamp bloqueo = rs.getTimestamp("bloqueado_hasta");
                if (bloqueo != null) {
                    u.setBloqueadoHasta(bloqueo.toLocalDateTime());
                }
                u.setRol(new Roles(rs.getInt("id_rol"), rs.getString("rol")));
                return u;
            }
        } catch (SQLException e){
            throw new DAOException("Error al consultar el usuario.", e);
        }
    }
    
    
    public void registrarIntentoFallido(int idUsuario, int maxIntentos, int minutosBloqueo) throws DAOException {
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(SQL_INTENTO_FALLIDO)){
            ps.setInt(1, maxIntentos);
            ps.setInt(2, minutosBloqueo);
            ps.setInt(3, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e){
            throw new DAOException("Error al registrar el intento fallido.", e);
        }
    }
    
    
    public void reiniciarIntentos(int idUsuario) throws DAOException {
       try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(SQL_REINICIAR_INTENTOS)){
           
          ps.setInt(1, idUsuario);
          ps.executeUpdate();
       } catch (SQLException e){
           throw new DAOException("Error al reiniciar los intentos", e);
       }
    }
}
