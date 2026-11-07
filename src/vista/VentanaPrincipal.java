/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

/**
 *
 * @author maris
 */
import excepciones.AutorizacionException;
import excepciones.DAOException;
import excepciones.ValidacionException;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import modelo.Funcionarios;
import seguridad.Permiso;
import seguridad.SesionUsuario;
import servicios.FuncionarioServicio;

public class VentanaPrincipal extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(VentanaPrincipal.class.getName());
    private static final int AVISO_SEGUNDOS = 60;

    private final FuncionarioServicio servicio = new FuncionarioServicio();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Tipo Documento", "Número", "Nombres", "Apellidos"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);

    private final JTextField txtBuscar = new JTextField(18);
    private final JLabel lblUsuario = new JLabel(" ");
    private final JLabel lblConteo = new JLabel(" ");
    private final JLabel lblSesion = new JLabel(" ");

    private final JButton btnNuevo = Estilo.boton("Nuevo", Estilo.EXITO);
    private final JButton btnVer = Estilo.boton("Ver detalle", Estilo.PRIMARIO);
    private final JButton btnEditar = Estilo.boton("Editar", Estilo.PRIMARIO);
    private final JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
    private final JButton btnActualizar = Estilo.boton("Actualizar", Estilo.NEUTRO);
    private final JButton btnCerrarSesion = Estilo.boton("Cerrar sesión", Estilo.NEUTRO);

    private final Timer temporizador = new Timer(1000, e -> revisarSesion());
    private boolean pidiendoLogin = false;

    public VentanaPrincipal() {
        setTitle("Gestión de Funcionarios - Recursos Humanos (RRHH)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 580);
        setMinimumSize(new Dimension(800, 450));
        setLocationRelativeTo(null);

        construirInterfaz();
        escucharActividad();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SesionUsuario.cerrarSesion();
            }
        });

        iniciarSesionEnVista();
    }

    private void construirInterfaz() {
        getContentPane().setBackground(Estilo.FONDO);

        // Encabezado
        JLabel titulo = new JLabel("Gestión de Funcionarios");
        titulo.setFont(Estilo.FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);
        lblUsuario.setFont(Estilo.FUENTE);
        lblUsuario.setForeground(Color.WHITE);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        derecha.add(lblUsuario);
        derecha.add(btnCerrarSesion);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilo.ENCABEZADO);
        encabezado.setBorder(new EmptyBorder(14, 20, 14, 20));
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnNuevo);
        acciones.add(btnVer);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);
        acciones.add(btnActualizar);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(Estilo.FUENTE_NEGRITA);
        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buscador.setOpaque(false);
        buscador.add(lblBuscar);
        buscador.add(txtBuscar);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(new EmptyBorder(12, 16, 8, 16));
        barra.add(acciones, BorderLayout.WEST);
        barra.add(buscador, BorderLayout.EAST);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(encabezado, BorderLayout.NORTH);
        norte.add(barra, BorderLayout.CENTER);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        Estilo.tabla(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB)));
        scroll.getViewport().setBackground(Color.WHITE);
        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(0, 16, 0, 16));
        centro.add(scroll);

        lblConteo.setFont(Estilo.FUENTE);
        lblSesion.setFont(Estilo.FUENTE_NEGRITA);
        JPanel estado = new JPanel(new BorderLayout());
        estado.setOpaque(false);
        estado.setBorder(new EmptyBorder(8, 20, 8, 20));
        estado.add(lblConteo, BorderLayout.WEST);
        estado.add(lblSesion, BorderLayout.EAST);

        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);

        btnNuevo.addActionListener(e -> nuevo());
        btnVer.addActionListener(e -> abrirFormulario(true));
        btnEditar.addActionListener(e -> abrirFormulario(false));
        btnEliminar.addActionListener(e -> eliminar());
        btnActualizar.addActionListener(e -> cargarTabla());
        btnCerrarSesion.addActionListener(e -> cerrarSesionManual());

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && btnVer.isEnabled()) {
                    abrirFormulario(true);
                }
            }
        });

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
    }

    //Sesión y permisos
    private void escucharActividad() {
        Toolkit.getDefaultToolkit().addAWTEventListener(
                ev -> SesionUsuario.actualizarActividad(),
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    private void iniciarSesionEnVista() {
        lblUsuario.setText("Usuario: " + SesionUsuario.getNombreUsuario()
                + "   |   Rol: " + SesionUsuario.getRolActual());
        lblSesion.setText("Sesión activa");
        lblSesion.setForeground(Estilo.EXITO);
        txtBuscar.setText("");
        aplicarPermisos();
        temporizador.start();
        cargarTabla();
    }

    private void aplicarPermisos() {
        habilitar(btnNuevo, Permiso.CREAR);
        habilitar(btnVer, Permiso.VER);
        habilitar(btnEditar, Permiso.EDITAR);
        habilitar(btnEliminar, Permiso.ELIMINAR);
        habilitar(btnActualizar, Permiso.LISTAR);
    }

    private void habilitar(JButton boton, Permiso permiso) {
        boolean permitido = SesionUsuario.tienePermiso(permiso);
        boton.setEnabled(permitido);
        boton.setToolTipText(permitido ? null : "Su rol no tiene permiso para esta acción");
    }

    private void revisarSesion() {
        if (!SesionUsuario.estaAutenticado()) {
            volverALogin("Su sesión expiró por inactividad.\nInicie sesión de nuevo para continuar.");
            return;
        }
        long seg = SesionUsuario.segundosRestantes();
        if (seg <= AVISO_SEGUNDOS) {
            lblSesion.setText(String.format(
                    "⚠ La sesión expira en %d:%02d (haga clic para continuar)", seg / 60, seg % 60));
            lblSesion.setForeground(Estilo.ADVERTENCIA);
        } else {
            lblSesion.setText("Sesión activa");
            lblSesion.setForeground(Estilo.EXITO);
        }
    }

    private void cerrarSesionManual() {
        int opcion = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión?",
                "Cerrar sesión", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            volverALogin(null);
        }
    }

    /**Cierra la sesión*/
    private void volverALogin(String mensaje) {
        if (pidiendoLogin) {
            return;
        }
        pidiendoLogin = true;
        try {
            temporizador.stop();
            SesionUsuario.cerrarSesion();
            for (Window w : getOwnedWindows()) {
                w.dispose();
            }
            modelo.setRowCount(0);
            actualizarConteo();
            aplicarPermisos();
            setVisible(false);

            if (mensaje != null) {
                JOptionPane.showMessageDialog(null, mensaje, "Sesión finalizada",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (login.isAutenticado()) {
                setVisible(true);
                iniciarSesionEnVista();
            } else {
                dispose();
                System.exit(0);
            }
        } finally {
            pidiendoLogin = false;
        }
    }

    private void cargarTabla() {
        btnActualizar.setEnabled(false);
        lblConteo.setText("Cargando…");
        new SwingWorker<List<Funcionarios>, Void>() {
            @Override
            protected List<Funcionarios> doInBackground() throws Exception {
                return servicio.listarFuncionarios();
            }

            @Override
            protected void done() {
                try {
                    llenarTabla(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    modelo.setRowCount(0);
                    actualizarConteo();
                    manejarError(e.getCause(), "No se pudo cargar la lista de funcionarios.");
                } finally {
                    aplicarPermisos();
                }
            }
        }.execute();
    }

    private void llenarTabla(List<Funcionarios> lista) {
        modelo.setRowCount(0);
        for (Funcionarios f : lista) {
            modelo.addRow(new Object[]{
                f.getId(), f.getTipoDocumento(), f.getNumeroDocumento(),
                f.getNombres(), f.getApellidos()
            });
        }
        actualizarConteo();
    }

    private void filtrar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
        }
        actualizarConteo();
    }

    private void actualizarConteo() {
        int total = modelo.getRowCount();
        int visibles = tabla.getRowCount();
        lblConteo.setText(visibles == total
                ? total + " funcionario(s)"
                : "Mostrando " + visibles + " de " + total + " funcionario(s)");
    }

    private Integer idSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un funcionario de la tabla.",
                    "Sin selección", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        return Integer.parseInt(String.valueOf(modelo.getValueAt(filaModelo, 0)));
    }

    private void nuevo() {
        new FormularioFuncionario(this, null, false).setVisible(true);
        cargarTabla();
    }

    private void abrirFormulario(boolean soloLectura) {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        try {
            Funcionarios f = servicio.obtenerFuncionarioPorId(id);
            new FormularioFuncionario(this, f, soloLectura).setVisible(true);
            if (!soloLectura) {
                cargarTabla();
            }
        } catch (AutorizacionException | DAOException e) {
            manejarError(e, "No se pudo cargar el funcionario.");
        }
    }

    private void eliminar() {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el funcionario?\n"
                + "También se borrará su grupo familiar y formación académica.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminarFuncionario(id);
            cargarTabla();
        } catch (AutorizacionException | DAOException e) {
            manejarError(e, "No se pudo eliminar el funcionario.");
        }
    }

    private void manejarError(Throwable e, String mensajeBase) {
        if (e instanceof AutorizacionException) {
            if (!SesionUsuario.estaAutenticado()) {
                volverALogin("Su sesión expiró.\nInicie sesión de nuevo para continuar.");
            } else {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                        "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            }
        } else if (e instanceof ValidacionException) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } else if (e instanceof DAOException) {
            LOGGER.log(Level.SEVERE, mensajeBase, e);
            JOptionPane.showMessageDialog(this,
                    mensajeBase + "\nVerifique la conexión con la base de datos.",
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        } else {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            JOptionPane.showMessageDialog(this,
                    "Ocurrió un error inesperado. Contacte al administrador.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}






    
   
