/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

/**
 *
 * @author maris
 */
import dao.FuncionarioDAO;
import excepciones.DAOException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Comparator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import modelo.Funcionarios;

//** Ventana Principal, lista de funcionarios y botones del CRUD.

public class VentanaPrincipal extends JFrame {
    // DAO funciona como intermediario de la BD
    private final FuncionarioDAO dao = new FuncionarioDAO();
    
    //Guardamos los datos de la tabla, si 'isCellEditable = false', evita
    // que el usuario escriba sobre las celdas directamente.
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Tipo Documento", "Número", "Nombres", "Apellidos"}, 0){
        @Override
        public boolean isCellEditable(int fila, int columna){
            return false;
            }
        };
    private final JTable tabla = new JTable(modelo);
    
public VentanaPrincipal(){
    setTitle("Gestión de Funcionarios - Recursos Humanos (RRHH)");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(800, 450);
    setLocationRelativeTo(null);
    
    tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    //BOTONES.
    JButton btnNuevo = new JButton("Nuevo");
    JButton btnEditar = new JButton("Editar");
    JButton btnActualizar = new JButton("Actualizar");
    JButton btnEliminar = new JButton("Eliminar");
    
    //Llamar Métodos.
    btnNuevo.addActionListener(e -> nuevo());
    btnEditar.addActionListener(e -> editar());
    btnActualizar.addActionListener(e -> cargarTabla());
    btnEliminar.addActionListener(e -> eliminar());
    
    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panelBotones.add(btnNuevo);
    panelBotones.add(btnEditar);
    panelBotones.add(btnActualizar);
    panelBotones.add(btnEliminar);
    
    add(panelBotones, BorderLayout.NORTH);
    add(new JScrollPane(tabla), BorderLayout.CENTER);
    
    cargarTabla();
}

//READ lista.
private void cargarTabla(){
    modelo.setRowCount(0);
    try{
       List<Funcionarios> lista = dao.listar();
       lista.sort(Comparator.comparingInt(Funcionarios::getId));
       for (Funcionarios f : lista){
           modelo.addRow(new Object[]{
                f.getId(),
                f.getTipoDocumento(),
                f.getNumeroDocumento(),
                f.getNombres(),
                f.getApellidos()
           });
       }
    } catch (DAOException e){
        JOptionPane.showMessageDialog(this,
            "No se pudo cargar la lista: " + e.getMessage(),
            "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

//CREATE
private void nuevo(){
    FormularioFuncionario form = new FormularioFuncionario(this, null);
    form.setVisible(true);
    cargarTabla();
    }

//UPDATE.
private void editar(){
    int fila = tabla.getSelectedRow();
    if (fila == -1){
        JOptionPane.showMessageDialog(this, "Seleccionar Funcionario de la tabla.");
        return;
    }
    int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
    try {
        Funcionarios f = dao.buscarPorId(id);
        FormularioFuncionario form = new FormularioFuncionario(this, f);
        form.setVisible(true);
        cargarTabla();
    } catch (DAOException e){
        JOptionPane.showMessageDialog(this,
                "No se pudo cargar el funcionario: " + e.getMessage(),
                "Error de la Base de Datos", JOptionPane.ERROR_MESSAGE);
    }
}

//DELETE.
private void eliminar(){
    int fila = tabla.getSelectedRow();
    if (fila == -1){
        JOptionPane.showMessageDialog(this, "Seleccionar un Funcionario de la tabla");
        return;
    }
    int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
    int opcion = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de eliminar el funcionario?\n"
            + "También se borrará su grupo familiar y formación académica.",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
    
if (opcion == JOptionPane.YES_OPTION){
    try{
        dao.eliminar(id);
        cargarTabla();
    } catch (DAOException e){
        JOptionPane.showMessageDialog(this,
        "No se pudo eliminar: " + e.getMessage(),
        "Error de la Base de Datos", JOptionPane.ERROR_MESSAGE);
    
            }
        }
    }

public static void main(String[] args){
    SwingUtilities.invokeLater(()-> new VentanaPrincipal().setVisible(true));
    }
}






    
   
