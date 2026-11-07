/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import excepciones.AutentificacionException;
import excepciones.DAOException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import modelo.Usuarios;
import seguridad.AuthService;
import seguridad.SesionUsuario;

/**
 *
 * @author maris
 */
public class LoginDialog extends JDialog {

    private static final Logger LOGGER = Logger.getLogger(LoginDialog.class.getName());
    private static final Pattern PATRON_USUARIO = Pattern.compile("[A-Za-z0-9._-]{3,50}");
    private static final int MAX_CLAVE = 128;
    private static final String MSG_CREDENCIALES = "Usuario o contraseña incorrectos.";

    private final AuthService auth = new AuthService();

    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final JCheckBox chkMostrar = new JCheckBox("Mostrar contraseña");
    private final JLabel lblMensaje = new JLabel(" ");
    private final JButton btnIngresar = Estilo.boton("Iniciar sesión", Estilo.PRIMARIO);
    private final JButton btnSalir = Estilo.boton("Salir", Estilo.NEUTRO);
    private final char echoOriginal = txtClave.getEchoChar();

    private boolean autenticado = false;

    public LoginDialog(Frame propietario) {
        super(propietario, "Iniciar sesión", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        construirInterfaz();
        pack();
        setLocationRelativeTo(propietario);
    }

    public boolean isAutenticado() {
        return autenticado;
    }

    private void construirInterfaz() {
        JPanel lateral = new JPanel(new GridBagLayout());
        lateral.setBackground(Estilo.ENCABEZADO);
        lateral.setPreferredSize(new Dimension(230, 380));
        JLabel marca = new JLabel("<html><div style='text-align:center'>Gestión de<br>Funcionarios"
                + "<br><span style='font-size:11px;font-weight:normal'>Recursos Humanos</span></div></html>");
        marca.setForeground(Color.WHITE);
        marca.setFont(Estilo.FUENTE_TITULO);
        lateral.add(marca);

        JLabel titulo = new JLabel("Bienvenido/a");
        titulo.setFont(Estilo.FUENTE_TITULO);
        titulo.setForeground(Estilo.TEXTO);
        JLabel subtitulo = new JLabel("Ingrese sus credenciales para continuar");
        subtitulo.setFont(Estilo.FUENTE);
        subtitulo.setForeground(Estilo.NEUTRO);

        txtUsuario.setPreferredSize(new Dimension(280, 32));
        txtClave.setPreferredSize(new Dimension(280, 32));

        chkMostrar.setBackground(Color.WHITE);
        chkMostrar.setFont(Estilo.FUENTE);
        chkMostrar.addActionListener(e ->
                txtClave.setEchoChar(chkMostrar.isSelected() ? (char) 0 : echoOriginal));

        lblMensaje.setFont(Estilo.FUENTE_NEGRITA);
        lblMensaje.setForeground(Estilo.PELIGRO);
        lblMensaje.setVerticalAlignment(SwingConstants.TOP);
        lblMensaje.setPreferredSize(new Dimension(280, 40));

        JPanel botones = new JPanel(new GridLayout(1, 2, 8, 0));
        botones.setOpaque(false);
        botones.add(btnSalir);
        botones.add(btnIngresar);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(28, 32, 24, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        fila(form, c, titulo, 0);
        fila(form, c, subtitulo, 0);
        fila(form, c, etiqueta("Usuario"), 18);
        fila(form, c, txtUsuario, 3);
        fila(form, c, etiqueta("Contraseña"), 10);
        fila(form, c, txtClave, 3);
        fila(form, c, chkMostrar, 6);
        fila(form, c, lblMensaje, 8);
        fila(form, c, botones, 8);

        add(lateral, BorderLayout.WEST);
        add(form, BorderLayout.CENTER);

        btnIngresar.addActionListener(e -> intentarLogin());
        btnSalir.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnIngresar);
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.FUENTE_NEGRITA);
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    private void fila(JPanel panel, GridBagConstraints c, Component comp, int margenSuperior) {
        c.gridy++;
        c.insets = new Insets(margenSuperior, 0, 3, 0);
        panel.add(comp, c);
    }

    private void intentarLogin() {
        String usuario = txtUsuario.getText().trim();
        char[] clave = txtClave.getPassword();

        String error = validarEntrada(usuario, clave);
        if (error != null) {
            Arrays.fill(clave, '\0');
            txtClave.setText("");
            mostrarError(error);
            return;
        }

        setEnProceso(true);
        new SwingWorker<Usuarios, Void>() {
            @Override
            protected Usuarios doInBackground() throws Exception {
                try {
                    return auth.autenticar(usuario, clave);
                } finally {
                    Arrays.fill(clave, '\0');
                }
            }

            @Override
            protected void done() {
                setEnProceso(false);
                try {
                    SesionUsuario.iniciarSesion(get());
                    autenticado = true;
                    dispose();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    fallo(e.getCause());
                }
            }
        }.execute();
    }

    private String validarEntrada(String usuario, char[] clave) {
        if (usuario.isEmpty() || clave.length == 0) {
            return "Ingrese su usuario y su contraseña.";
        }
        if (!PATRON_USUARIO.matcher(usuario).matches() || clave.length > MAX_CLAVE) {
            return MSG_CREDENCIALES;
        }
        return null;
    }

    private void fallo(Throwable causa) {
        txtClave.setText("");
        if (causa instanceof AutentificacionException) {
            mostrarError(causa.getMessage());
        } else if (causa instanceof DAOException) {
            LOGGER.log(Level.SEVERE, "Error de base de datos durante el login", causa);
            mostrarError("No fue posible conectar con el sistema. Intente más tarde.");
        } else {
            LOGGER.log(Level.SEVERE, "Error inesperado durante el login", causa);
            mostrarError("Ocurrió un error inesperado. Contacte al administrador.");
        }
        txtClave.requestFocusInWindow();
    }

    private void mostrarError(String texto) {
        lblMensaje.setText("<html><div style='width:260px'>" + texto + "</div></html>");
    }

    private void setEnProceso(boolean enProceso) {
        txtUsuario.setEnabled(!enProceso);
        txtClave.setEnabled(!enProceso);
        chkMostrar.setEnabled(!enProceso);
        btnIngresar.setEnabled(!enProceso);
        btnIngresar.setText(enProceso ? "Verificando…" : "Iniciar sesión");
        if (enProceso) {
            lblMensaje.setText(" ");
        }
    }
}
