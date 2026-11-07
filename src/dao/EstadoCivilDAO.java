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
import java.util.ArrayList;
import java.util.List;
import modelo.EstadoCivil;

/**
 *
 * @author Isabella
 */
public class EstadoCivilDAO {
    private static final String SQL_LISTAR =
            "SELECT id, nombre FROM estado_civil ORDER BY nombre";

    public List<EstadoCivil> listar() throws DAOException {
        List<EstadoCivil> lista = new ArrayList<>();
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new EstadoCivil(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al listar el estado civil del funcionario.", e);
        }
        return lista;
    }
}
