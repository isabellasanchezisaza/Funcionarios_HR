/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.Conexion;
import excepciones.DAOException;
import excepciones.DocDuplicadoException;
import excepciones.ValidacionException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.EstadoCivil;
import modelo.FormacionAcademica;
import modelo.GrupoFamiliar;
import modelo.Funcionarios;
import modelo.NivelEstudio;
import modelo.RolFamiliar;
import modelo.TipoDocumento;
import validacion.ValidadorFuncionario;
/**
 *
 * @author Isabella
 */
public class FuncionarioDAO {
 
    private static final String SQL_SELECT =
        "SELECT f.id, f.nombre, f.apellidos, f.numero_documento, f.sexo, "
        + "f.direccion, f.telefono, f.fecha_nacimiento, f.correo, "
        + "td.id AS id_tipo_documento, td.nombre AS tipo_documento, "
        + "ec.id AS id_estado_civil, ec.nombre AS estado_civil "
        + "FROM funcionarios f "
        + "JOIN tipo_documento td ON td.id = f.id_tipo_documento "
        + "JOIN estado_civil ec ON ec.id = f.id_estado_civil";

    private static final String SQL_LISTAR = SQL_SELECT + " ORDER BY f.apellidos, f.nombre";

    private static final String SQL_BUSCAR = SQL_SELECT + " WHERE f.id = ?";

    private static final String SQL_FAMILIARES =
        "SELECT g.id, g.nombre, g.apellidos, g.sexo, g.fecha_nacimiento, g.telefono, "
        + "r.id AS id_rol, r.nombre AS rol "
        + "FROM grupo_familiar g JOIN rol_familia r ON r.id = g.id_rol_familiar "
        + "WHERE g.id_funcionario = ? ORDER BY g.id";

    private static final String SQL_ESTUDIOS =
        "SELECT e.id, e.institucion, e.titulo_obtenido, e.anio_graduacion, "
        + "n.id AS id_nivel, n.nombre AS nivel "
        + "FROM formacion_academica e JOIN nivel_estudio n ON n.id = e.id_nivel_estudio "
        + "WHERE e.id_funcionario = ? ORDER BY e.id";

    private static final String SQL_ELIMINAR = "DELETE FROM funcionarios WHERE id = ?";

    private static final String SQL_INSERTAR =
        "INSERT INTO funcionarios (nombre, apellidos, id_tipo_documento, "
        + "numero_documento, sexo, id_estado_civil, direccion, telefono, "
        + "fecha_nacimiento, correo) VALUES (?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERTAR_FAMILIAR =
        "INSERT INTO grupo_familiar (id_funcionario, id_rol_familiar, nombre, "
        + "apellidos, sexo, fecha_nacimiento, telefono) VALUES (?,?,?,?,?,?,?)";

    private static final String SQL_INSERTAR_ESTUDIO =
        "INSERT INTO formacion_academica (id_funcionario, id_nivel_estudio, "
        + "institucion, titulo_obtenido, anio_graduacion) VALUES (?,?,?,?,?)";

    private static final String SQL_ACTUALIZAR =
        "UPDATE funcionarios SET nombre=?, apellidos=?, id_tipo_documento=?, "
        + "numero_documento=?, sexo=?, id_estado_civil=?, direccion=?, telefono=?, "
        + "fecha_nacimiento=?, correo=? WHERE id=?";

    private static final String SQL_BORRAR_FAMILIARES =
        "DELETE FROM grupo_familiar WHERE id_funcionario = ?";

    private static final String SQL_BORRAR_ESTUDIOS =
        "DELETE FROM formacion_academica WHERE id_funcionario = ?";
    
    // ---------- LISTAR ----------
    public List<Funcionarios> listar() throws DAOException {
        List<Funcionarios> lista = new ArrayList<>();
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al listar los funcionarios.", e);
        }
        return lista;
    }

    private Funcionarios mapear(ResultSet rs) throws SQLException {
        Funcionarios f = new Funcionarios();
        f.setId(rs.getInt("id"));
        f.setNombres(rs.getString("nombre"));
        f.setApellidos(rs.getString("apellidos"));
        f.setTipoDocumento(new TipoDocumento(
                rs.getInt("id_tipo_documento"), rs.getString("tipo_documento")));
        f.setNumeroDocumento(rs.getString("numero_documento"));

        String sexo = rs.getString("sexo");
        f.setSexo(sexo != null && !sexo.isEmpty() ? sexo.charAt(0) : ' ');

        f.setEstadoCivil(new EstadoCivil(
                rs.getInt("id_estado_civil"), rs.getString("estado_civil")));
        f.setDireccion(rs.getString("direccion"));
        f.setTelefono(rs.getString("telefono"));

        Date fecha = rs.getDate("fecha_nacimiento");
        if (fecha != null) {
            f.setFechaNacimiento(fecha.toLocalDate());
        }
        f.setCorreo(rs.getString("correo"));
        return f;
    }

    // ---------- INSERTAR (con transacción) ----------
    public void insertar(Funcionarios f) throws DAOException {
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps =
                         conn.prepareStatement(SQL_INSERTAR, new String[]{"id"})) {
                ps.setString(1, f.getNombres());
                ps.setString(2, f.getApellidos());
                ps.setInt(3, f.getTipoDocumento().getId());
                ps.setString(4, f.getNumeroDocumento());
                ps.setString(5, String.valueOf(f.getSexo()));
                ps.setInt(6, f.getEstadoCivil().getId());
                ps.setString(7, f.getDireccion());
                ps.setString(8, f.getTelefono());
                ps.setDate(9, Date.valueOf(f.getFechaNacimiento()));
                ps.setString(10, f.getCorreo());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        f.setId(keys.getInt(1));
                    }
                }
            }

            insertarFamiliares(conn, f);
            insertarEstudios(conn, f);
            conn.commit();

        } catch (SQLException e) {
            rollback(conn);
            if ("23505".equals(e.getSQLState())) {
                throw new DocDuplicadoException(
                        "Ya existe un funcionario con ese tipo y número de documento.", e);
            }
            throw new DAOException("Error al guardar el funcionario.", e);
        } finally {
            cerrar(conn);
        }
    }

    private void insertarFamiliares(Connection conn, Funcionarios f) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERTAR_FAMILIAR)) {
            for (GrupoFamiliar fam : f.getFamiliares()) {
                ps.setInt(1, f.getId());
                ps.setInt(2, fam.getRol().getId());
                ps.setString(3, fam.getNombres());
                ps.setString(4, fam.getApellidos());
                ps.setString(5, String.valueOf(fam.getSexo()));
                ps.setDate(6, fam.getFechaNacimiento() != null
                        ? Date.valueOf(fam.getFechaNacimiento()) : null);
                ps.setString(7, fam.getTelefono());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertarEstudios(Connection conn, Funcionarios f) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERTAR_ESTUDIO)) {
            for (FormacionAcademica e : f.getEstudios()) {
                ps.setInt(1, f.getId());
                ps.setInt(2, e.getNivelEstudio().getId());
                ps.setString(3, e.getInstitucion());
                ps.setString(4, e.getTituloObtenido());
                ps.setInt(5, e.getAnioGraduacion());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ---------- Auxiliares de transacción ----------
    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignorada) {
                // si el rollback también falla, no hay mucho más que hacer
            }
        }
    }

    private void cerrar(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ignorada) {
            }
        }
    }
    
    // ---------- BUSCAR POR ID (con familiares y estudios) ----------
    public Funcionarios buscarPorId(int id) throws DAOException {
        try (Connection conn = Conexion.getConexion()) {
        Funcionarios f;
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DAOException("No se encontró el funcionario con id " + id + ".");
                }
                f = mapear(rs);
            }
        }
        f.setFamiliares(listarFamiliares(conn, id));
        f.setEstudios(listarEstudios(conn, id));
        return f;
        } catch (SQLException e) {
            System.out.println("Detalle del error SQL: " + e.getMessage());
        throw new DAOException("Error al buscar el funcionario.", e);
        }
    }

    private List<GrupoFamiliar> listarFamiliares(Connection conn, int idFuncionario) throws SQLException {
        List<GrupoFamiliar> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FAMILIARES)) {
        ps.setInt(1, idFuncionario);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                GrupoFamiliar fam = new GrupoFamiliar();
                fam.setId(rs.getInt("id"));
                fam.setIdFuncionario(idFuncionario);
                fam.setRol(new RolFamiliar(rs.getInt("id_rol"), rs.getString("rol")));
                fam.setNombres(rs.getString("nombre"));
                fam.setApellidos(rs.getString("apellidos"));
                String sexo = rs.getString("sexo");
                fam.setSexo(sexo != null && !sexo.isEmpty() ? sexo.charAt(0) : ' ');
                Date fecha = rs.getDate("fecha_nacimiento");
                if (fecha != null) {
                    fam.setFechaNacimiento(fecha.toLocalDate());
                }
                fam.setTelefono(rs.getString("telefono"));
                lista.add(fam);
                }
            }
        }
    return lista;
    }

    private List<FormacionAcademica> listarEstudios(Connection conn, int idFuncionario) throws SQLException {
        List<FormacionAcademica> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ESTUDIOS)) {
        ps.setInt(1, idFuncionario);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FormacionAcademica e = new FormacionAcademica();
                e.setId(rs.getInt("id"));
                e.setIdFuncionario(idFuncionario);
                e.setNivelEstudio(new NivelEstudio(rs.getInt("id_nivel"), rs.getString("nivel")));
                e.setInstitucion(rs.getString("institucion"));
                e.setTituloObtenido(rs.getString("titulo_obtenido"));
                e.setAnioGraduacion(rs.getInt("anio_graduacion"));
                lista.add(e);
                }
            }
        }
    return lista;
    }

    // ---------- ELIMINAR ----------
    public void eliminar(int id) throws DAOException {
        try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(SQL_ELIMINAR)) {
        ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
            throw new DAOException("No existe un funcionario con id " + id + ".");
            }
        } catch (SQLException e) {
        throw new DAOException("Error al eliminar el funcionario.", e);
        }
    }
    // ---------- ACTUALIZAR (con transacción) ----------
    public void actualizar(Funcionarios f) throws DAOException, ValidacionException {
        ValidadorFuncionario.validar(f);

        Connection conn = null;
        try {
        conn = Conexion.getConexion();
        conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, f.getNombres());
            ps.setString(2, f.getApellidos());
            ps.setInt(3, f.getTipoDocumento().getId());
            ps.setString(4, f.getNumeroDocumento());
            ps.setString(5, String.valueOf(f.getSexo()));
            ps.setInt(6, f.getEstadoCivil().getId());
            ps.setString(7, f.getDireccion());
            ps.setString(8, f.getTelefono());
            ps.setDate(9, Date.valueOf(f.getFechaNacimiento()));
            ps.setString(10, f.getCorreo());
            ps.setInt(11, f.getId());

            if (ps.executeUpdate() == 0) {
                throw new DAOException("No existe un funcionario con id " + f.getId() + ".");
            }
        }

        borrarHijos(conn, SQL_BORRAR_FAMILIARES, f.getId());
        borrarHijos(conn, SQL_BORRAR_ESTUDIOS, f.getId());
        insertarFamiliares(conn, f);
        insertarEstudios(conn, f);
        conn.commit();

        } catch (SQLException e) {
            rollback(conn);
            if ("23505".equals(e.getSQLState())) {
            throw new DocDuplicadoException(
                    "Ya existe otro funcionario con ese tipo y número de documento.", e);
                }
            throw new DAOException("Error al actualizar el funcionario.", e);
            } catch (DAOException e) {
                rollback(conn);
                throw e;
            } finally {
            cerrar(conn);
        }
    }

    private void borrarHijos(Connection conn, String sql, int idFuncionario) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, idFuncionario);
        ps.executeUpdate();
        }
    }
}
