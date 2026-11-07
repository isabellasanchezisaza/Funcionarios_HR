/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicios;

import dao.FuncionarioDAO;
import excepciones.AutorizacionException;
import excepciones.DAOException;
import excepciones.ValidacionException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Funcionarios;
import seguridad.Permiso;
import seguridad.SesionUsuario;
/**
 *
 * @author Usuario
 */
public class FuncionarioServicio {

    private static final Logger LOGGER = Logger.getLogger(FuncionarioServicio.class.getName());
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    private String exigir(Permiso permiso) throws AutorizacionException {
        if (!SesionUsuario.estaAutenticado()) {
            throw new AutorizacionException("La sesión expiró o no hay un usuario autenticado.");
        }
        if (!SesionUsuario.tienePermiso(permiso)) {
            throw new AutorizacionException("Su rol no tiene permiso para realizar esta acción.");
        }
        return SesionUsuario.getNombreUsuario();
    }

    public List<Funcionarios> listarFuncionarios() throws AutorizacionException, DAOException {
        String usuario = exigir(Permiso.LISTAR);
        try {
            List<Funcionarios> lista = funcionarioDAO.listar();
            LOGGER.log(Level.INFO, "Usuario ''{0}'' listó los funcionarios.", usuario);
            return lista;
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Error al listar funcionarios", e);
            throw e;
        }
    }

    public Funcionarios obtenerFuncionarioPorId(int id) throws AutorizacionException, DAOException {
        String usuario = exigir(Permiso.VER);
        try {
            Funcionarios f = funcionarioDAO.buscarPorId(id);
            LOGGER.log(Level.INFO, "Usuario ''{0}'' consultó el funcionario ID: {1}.",
                    new Object[]{usuario, id});
            return f;
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar funcionario", e);
            throw e;
        }
    }

    public void registrarFuncionario(Funcionarios f)
            throws AutorizacionException, ValidacionException, DAOException {
        String usuario = exigir(Permiso.CREAR);
        try {
            funcionarioDAO.insertar(f);
            LOGGER.log(Level.INFO, "Usuario ''{0}'' creó un funcionario.", usuario);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar funcionario", e);
            throw e;
        }
    }

    public void actualizarFuncionario(Funcionarios f)
            throws AutorizacionException, ValidacionException, DAOException {
        String usuario = exigir(Permiso.EDITAR);
        try {
            funcionarioDAO.actualizar(f);
            LOGGER.log(Level.INFO, "Usuario ''{0}'' actualizó el funcionario ID: {1}.",
                    new Object[]{usuario, f.getId()});
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar funcionario", e);
            throw e;
        }
    }

    public void eliminarFuncionario(int id) throws AutorizacionException, DAOException {
        String usuario = exigir(Permiso.ELIMINAR);
        try {
            funcionarioDAO.eliminar(id);
            LOGGER.log(Level.INFO, "Usuario ''{0}'' eliminó el funcionario ID: {1}.",
                    new Object[]{usuario, id});
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar funcionario", e);
            throw e;
        }
    }
}
