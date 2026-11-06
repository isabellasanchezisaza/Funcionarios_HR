/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicios;
import dao.FuncionarioDAO;
import excepciones.AutorizacionException;
import excepciones.DAOException;
import excepciones.ValidacionException;
import modelo.Funcionarios;
import seguridad.SesionUsuario;
import validacion.ValidadorFuncionario;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 *
 * @author Usuario
 */
public class FuncionarioServicio {
    private static final Logger LOGGER = Logger.getLogger(FuncionarioServicio.class.getName());
    private final FuncionarioDAO funcionarioDAO;

    public FuncionarioServicio() {
        this.funcionarioDAO = new FuncionarioDAO();
}
    private void validarAcceso(String permisoRequerido) throws AutorizacionException {
        if (!SesionUsuario.estaAutenticado()) {
            throw new AutorizacionException("La sesión ha expirado o no hay un usuario autenticado.");
        }
        if (!SesionUsuario.tienePermiso(permisoRequerido)) {
            throw new AutorizacionException("Acceso denegado: El rol actual (" + SesionUsuario.getRolActual() + ") no tiene permiso para [" + permisoRequerido + "].");
        }
    }
    
    public List<Funcionarios> listarFuncionarios() throws Exception {
        validarAcceso("LISTAR");
        try {
            LOGGER.log(Level.INFO, "[LOG] Usuario ''{0}'' listó los funcionarios.", SesionUsuario.getUsuarioActual().getUsername());
            return funcionarioDAO.listar();
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "[LOG_ERROR] Error al listar funcionarios", e);
            throw new DAOException("Error al listar funcionarios: " + e.getMessage(), e);
        }
    }
    
    public void registrarFuncionario(Funcionarios funcionario) throws Exception {
        validarAcceso("CREAR");
        
        ValidadorFuncionario.validar(funcionario);

        try {
            LOGGER.log(Level.INFO, "[LOG] Usuario ''{0}'' creó un nuevo funcionario.", SesionUsuario.getUsuarioActual().getUsername());
            funcionarioDAO.insertar(funcionario); 
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "[LOG_ERROR] Error al registrar funcionario", e);
            throw new DAOException("Error al registrar funcionario: " + e.getMessage(), e);
        }
    }
    
    public void actualizarFuncionario(Funcionarios funcionario) throws Exception {
        validarAcceso("EDITAR");
        
        ValidadorFuncionario.validar(funcionario);

        try {
            LOGGER.log(Level.INFO, "[LOG] Usuario ''{0}'' actualizó el funcionario.", SesionUsuario.getUsuarioActual().getUsername());
            funcionarioDAO.actualizar(funcionario); 
        } catch (DAOException | ValidacionException e) {
            LOGGER.log(Level.SEVERE, "[LOG_ERROR] Error al actualizar funcionario", e);
            throw new DAOException("Error al actualizar funcionario: " + e.getMessage(), e);
        }
    } 
  
    public void eliminarFuncionario(int id) throws Exception {
        validarAcceso("ELIMINAR");
        try {
            LOGGER.log(Level.INFO, "[LOG] Usuario ''{0}'' eliminó el funcionario ID: {1}.", new Object[]{SesionUsuario.getUsuarioActual().getUsername(), id});
            funcionarioDAO.eliminar(id); 
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "[LOG_ERROR] Error al eliminar funcionario", e);
            throw new DAOException("Error al eliminar funcionario: " + e.getMessage(), e);
        }
    }
    
    public Funcionarios obtenerFuncionarioPorId(int id) throws Exception {
        validar_acceso_ver: {
            validarAcceso("VER");
        }
        try {
            LOGGER.log(Level.INFO, "[LOG] Usuario ''{0}'' consultó el funcionario ID: {1}.", new Object[]{SesionUsuario.getUsuarioActual().getUsername(), id});
            return funcionarioDAO.buscarPorId(id); 
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "[LOG_ERROR] Error al buscar funcionario", e);
            throw new DAOException("Error al buscar el funcionario: " + e.getMessage(), e);
        }
    }
}
