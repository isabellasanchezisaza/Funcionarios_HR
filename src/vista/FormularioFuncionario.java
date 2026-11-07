/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

/**
 *
 * @author maris
 */

import dao.EstadoCivilDAO;
import dao.FuncionarioDAO;
import dao.NivelEstudioDAO;
import dao.RolFamiliarDAO;
import dao.TipoDocumentoDAO;
import excepciones.DAOException;
import excepciones.DocDuplicadoException;
import excepciones.ValidacionException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.EstadoCivil;
import modelo.FormacionAcademica;
import modelo.Funcionarios;
import modelo.GrupoFamiliar;
import modelo.NivelEstudio;
import modelo.RolFamiliar;
import modelo.TipoDocumento;

/**
 * Formulario
 */
public class FormularioFuncionario extends JDialog {

    private final Funcionarios original;
    private final FuncionarioDAO dao = new FuncionarioDAO();

    // Campos Datos personales
    private final JComboBox<TipoDocumento> cmbTipoDocumento = new JComboBox<>();
    private final JTextField txtNumero = new JTextField();
    private final JTextField txtNombres = new JTextField();
    private final JTextField txtApellidos = new JTextField();
    private final JComboBox<EstadoCivil> cmbEstadoCivil = new JComboBox<>();
    private final JComboBox<String> cmbSexo = new JComboBox<>(new String[]{"M", "F"});
    private final JTextField txtDireccion = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtFecha = new JTextField();   // AAAA-MM-DD
    private final JTextField txtCorreo = new JTextField();

    private final JComboBox<RolFamiliar> cmbRol = new JComboBox<>();
    private final JComboBox<String> cmbSexoFam = new JComboBox<>(new String[]{"M", "F"});
    private final JComboBox<NivelEstudio> cmbNivel = new JComboBox<>();

    // Tabla Grupo Familiar
    private final DefaultTableModel modeloFam = new DefaultTableModel(
            new String[]{"Nombres", "Apellidos", "Sexo", "Nacimiento (AAAA-MM-DD)", "Teléfono", "Rol"}, 0);
    private final JTable tblFam = new JTable(modeloFam);

    // Tabla Formación Académica
    private final DefaultTableModel modeloEst = new DefaultTableModel(
            new String[]{"Institución", "Nivel de estudio", "Título obtenido", "Año de graduación"}, 0);
    private final JTable tblEst = new JTable(modeloEst);

    public FormularioFuncionario(JFrame padre, Funcionarios funcionario) {
        super(padre, true);
        this.original = funcionario;
        setTitle(funcionario == null ? "Nuevo funcionario" : "Editar funcionario");
        setSize(900, 650);
        setLocationRelativeTo(padre);

        construirInterfaz();
        cargarCombos();
        
        tblFam.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(cmbSexoFam));
        tblFam.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(cmbRol));
        tblEst.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(cmbNivel));

        if (original != null) {
            llenarCampos();
        }
    }

    //Intz
    private void construirInterfaz() {
        JPanel datos = new JPanel(new GridLayout(0, 4, 8, 8));
        datos.setBorder(new EmptyBorder(10, 10, 10, 10));
        datos.add(new JLabel("Tipo de documento:"));
        datos.add(cmbTipoDocumento);
        datos.add(new JLabel("Número:"));
        datos.add(txtNumero);
        datos.add(new JLabel("Nombres:"));
        datos.add(txtNombres);
        datos.add(new JLabel("Apellidos:"));
        datos.add(txtApellidos);
        datos.add(new JLabel("Estado civil:"));
        datos.add(cmbEstadoCivil);
        datos.add(new JLabel("Sexo (M/F):"));
        datos.add(cmbSexo);
        datos.add(new JLabel("Dirección:"));
        datos.add(txtDireccion);
        datos.add(new JLabel("Teléfono:"));
        datos.add(txtTelefono);
        datos.add(new JLabel("Nacimiento (AAAA-MM-DD):"));
        datos.add(txtFecha);
        datos.add(new JLabel("Correo:"));
        datos.add(txtCorreo);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Grupo familiar", crearPanelFamilia());
        pestanas.addTab("Formación académica", crearPanelEstudios());

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel inferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inferior.add(btnCancelar);
        inferior.add(btnGuardar);

        add(datos, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelFamilia() {
        JButton btnAgregar = new JButton("Agregar familiar");
        JButton btnQuitar = new JButton("Quitar familiar");

        btnAgregar.addActionListener(e ->
                modeloFam.addRow(new Object[]{"", "", "M", "", "", cmbRol.getItemAt(0)}));

        btnQuitar.addActionListener(e -> {
            int fila = tblFam.getSelectedRow();
            if (fila != -1) {
                modeloFam.removeRow(fila);
            }
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnAgregar);
        botones.add(btnQuitar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(botones, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblFam), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelEstudios() {
        JButton btnAgregar = new JButton("Agregar estudio");
        JButton btnQuitar = new JButton("Quitar estudio");

        btnAgregar.addActionListener(e ->
                modeloEst.addRow(new Object[]{"", cmbNivel.getItemAt(0), "",
                    String.valueOf(LocalDate.now().getYear())}));

        btnQuitar.addActionListener(e -> {
            int fila = tblEst.getSelectedRow();
            if (fila != -1) {
                modeloEst.removeRow(fila);
            }
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnAgregar);
        botones.add(btnQuitar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(botones, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblEst), BorderLayout.CENTER);
        return panel;
    }

    //Llenar.
    private void cargarCombos() {
        try {
            for (TipoDocumento t : new TipoDocumentoDAO().listar()) {
                cmbTipoDocumento.addItem(t);
            }
            for (EstadoCivil ec : new EstadoCivilDAO().listar()) {
                cmbEstadoCivil.addItem(ec);
            }
            for (RolFamiliar r : new RolFamiliarDAO().listar()) {
                cmbRol.addItem(r);
            }
            for (NivelEstudio n : new NivelEstudioDAO().listar()) {
                cmbNivel.addItem(n);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar las listas: " + e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Editar.
    private void llenarCampos() {
        Funcionarios f = original;
        txtNumero.setText(txt(f.getNumeroDocumento()));
        txtNombres.setText(txt(f.getNombres()));
        txtApellidos.setText(txt(f.getApellidos()));
        txtDireccion.setText(txt(f.getDireccion()));
        txtTelefono.setText(txt(f.getTelefono()));
        txtCorreo.setText(txt(f.getCorreo()));
        txtFecha.setText(fecha(f.getFechaNacimiento()));
        cmbSexo.setSelectedItem(String.valueOf(f.getSexo()));
        
        for (int i = 0; i < cmbTipoDocumento.getItemCount(); i++) {
            if (cmbTipoDocumento.getItemAt(i).getId() == f.getTipoDocumento().getId()) {
                cmbTipoDocumento.setSelectedIndex(i);
            }
        }
        for (int i = 0; i < cmbEstadoCivil.getItemCount(); i++) {
            if (cmbEstadoCivil.getItemAt(i).getId() == f.getEstadoCivil().getId()) {
                cmbEstadoCivil.setSelectedIndex(i);
            }
        }

        for (GrupoFamiliar g : f.getFamiliares()) {
            modeloFam.addRow(new Object[]{
                txt(g.getNombres()),
                txt(g.getApellidos()),
                String.valueOf(g.getSexo()),
                fecha(g.getFechaNacimiento()),
                txt(g.getTelefono()),
                buscarRol(g.getRol())});
        }
        for (FormacionAcademica fa : f.getEstudios()) {
            modeloEst.addRow(new Object[]{
                txt(fa.getInstitucion()),
                buscarNivel(fa.getNivelEstudio()),
                txt(fa.getTituloObtenido()),
                String.valueOf(fa.getAnioGraduacion())});
        }
    }

    private String txt(String s) {
        return s == null ? "" : s;
    }

    private String fecha(LocalDate d) {
        return d == null ? "" : d.toString();
    }

    private RolFamiliar buscarRol(RolFamiliar r) {
        for (int i = 0; i < cmbRol.getItemCount(); i++) {
            if (cmbRol.getItemAt(i).getId() == r.getId()) {
                return cmbRol.getItemAt(i);
            }
        }
        return r;
    }

    private NivelEstudio buscarNivel(NivelEstudio n) {
        for (int i = 0; i < cmbNivel.getItemCount(); i++) {
            if (cmbNivel.getItemAt(i).getId() == n.getId()) {
                return cmbNivel.getItemAt(i);
            }
        }
        return n;
    }

    //Leer Tablas
    private List<GrupoFamiliar> leerFamilia() {
        if (tblFam.isEditing()) {
            tblFam.getCellEditor().stopCellEditing();
        }
        List<GrupoFamiliar> lista = new ArrayList<>();
        for (int i = 0; i < modeloFam.getRowCount(); i++) {
            GrupoFamiliar g = new GrupoFamiliar();
            g.setNombres(modeloFam.getValueAt(i, 0).toString().trim());
            g.setApellidos(modeloFam.getValueAt(i, 1).toString().trim());
            g.setSexo(modeloFam.getValueAt(i, 2).toString().charAt(0));
            g.setFechaNacimiento(LocalDate.parse(modeloFam.getValueAt(i, 3).toString().trim()));
            g.setTelefono(modeloFam.getValueAt(i, 4).toString().trim());
            g.setRol((RolFamiliar) modeloFam.getValueAt(i, 5));
            lista.add(g);
        }
        return lista;
    }

    private List<FormacionAcademica> leerEstudios() {
        if (tblEst.isEditing()) {
            tblEst.getCellEditor().stopCellEditing();
        }
        List<FormacionAcademica> lista = new ArrayList<>();
        for (int i = 0; i < modeloEst.getRowCount(); i++) {
            FormacionAcademica fa = new FormacionAcademica();
            fa.setInstitucion(modeloEst.getValueAt(i, 0).toString().trim());
            fa.setNivelEstudio((NivelEstudio) modeloEst.getValueAt(i, 1));
            fa.setTituloObtenido(modeloEst.getValueAt(i, 2).toString().trim());
            fa.setAnioGraduacion(Integer.parseInt(modeloEst.getValueAt(i, 3).toString().trim()));
            lista.add(fa);
        }
        return lista;
    }

   //Guardar.
    private void guardar() {
        try {
            Funcionarios f = new Funcionarios();
            if (original != null) {
                f.setId(original.getId());
            }
            f.setTipoDocumento((TipoDocumento) cmbTipoDocumento.getSelectedItem());
            f.setNumeroDocumento(txtNumero.getText().trim());
            f.setNombres(txtNombres.getText().trim());
            f.setApellidos(txtApellidos.getText().trim());
            f.setEstadoCivil((EstadoCivil) cmbEstadoCivil.getSelectedItem());
            f.setSexo(cmbSexo.getSelectedItem().toString().charAt(0));
            f.setDireccion(txtDireccion.getText().trim());
            f.setTelefono(txtTelefono.getText().trim());
            f.setFechaNacimiento(LocalDate.parse(txtFecha.getText().trim()));
            f.setCorreo(txtCorreo.getText().trim());
            f.setFamiliares(leerFamilia());
            f.setEstudios(leerEstudios());

            if (original == null) {
                dao.insertar(f);
                JOptionPane.showMessageDialog(this, "Funcionario creado correctamente.");
            } else {
                dao.actualizar(f);
                JOptionPane.showMessageDialog(this, "Funcionario actualizado correctamente.");
            }
            dispose();

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Las fechas deben tener el formato AAAA-MM-DD (ejemplo: 1995-03-27).",
                    "Fecha inválida", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "El año de graduación debe ser un número (ejemplo: 2020).",
                    "Año inválido", JOptionPane.WARNING_MESSAGE);
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (DocDuplicadoException e) {
            JOptionPane.showMessageDialog(this,
                    "Ya existe un funcionario con ese tipo y número de documento.",
                    "Documento duplicado", JOptionPane.WARNING_MESSAGE);
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error de base de datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

