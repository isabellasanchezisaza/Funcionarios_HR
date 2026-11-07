/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;


/**
 *
 * @author maris
 */
public final class Estilo {

    public static final Color ENCABEZADO  = new Color(0x1F3A5F);
    public static final Color PRIMARIO    = new Color(0x2F6FDE);
    public static final Color EXITO       = new Color(0x2E8B57);
    public static final Color PELIGRO     = new Color(0xC0392B);
    public static final Color NEUTRO      = new Color(0x6B7280);
    public static final Color ADVERTENCIA = new Color(0xD97706);
    public static final Color FONDO       = new Color(0xF4F6F9);
    public static final Color TEXTO       = new Color(0x1F2937);

    public static final Font FUENTE         = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_NEGRITA = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FUENTE_TITULO  = new Font("Segoe UI", Font.BOLD, 22);

    private Estilo() { }

    /** Llamar una sola vez al arrancar, antes de crear ventanas. */
    public static void instalar() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // si falla, se queda el aspecto por defecto
        }
        UIManager.put("OptionPane.messageFont", FUENTE);
        UIManager.put("OptionPane.buttonFont", FUENTE);
    }

    public static JButton boton(String texto, Color color) {
        return new BotonEstilo(texto, color);
    }

    public static void tabla(JTable t) {
        t.setFont(FUENTE);
        t.setForeground(TEXTO);
        t.setRowHeight(28);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xE5E7EB));
        t.setSelectionBackground(new Color(0xDCE8FB));
        t.setSelectionForeground(TEXTO);
        t.setFillsViewportHeight(true);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object valor,
                    boolean seleccionada, boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(tb, valor, seleccionada, false, fila, col);
                if (!seleccionada) {
                    setBackground(fila % 2 == 0 ? Color.WHITE : new Color(0xF7F9FC));
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 32));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object valor,
                    boolean seleccionada, boolean foco, int fila, int col) {
                String texto = String.valueOf(valor);
                RowSorter<?> rs = tb.getRowSorter();
                if (rs != null && !rs.getSortKeys().isEmpty()) {
                    RowSorter.SortKey k = rs.getSortKeys().get(0);
                    if (k.getColumn() == tb.convertColumnIndexToModel(col)
                            && k.getSortOrder() != SortOrder.UNSORTED) {
                        texto += k.getSortOrder() == SortOrder.ASCENDING ? "  ▲" : "  ▼";
                    }
                }
                super.getTableCellRendererComponent(tb, texto, false, false, fila, col);
                setBackground(ENCABEZADO);
                setForeground(Color.WHITE);
                setFont(FUENTE_NEGRITA);
                setBorder(new EmptyBorder(6, 8, 6, 8));
                return this;
            }
        });
    }

    private static final class BotonEstilo extends JButton {
        private final Color base;
        private boolean encima;

        BotonEstilo(String texto, Color base) {
            super(texto);
            this.base = base;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(FUENTE_NEGRITA);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { encima = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fondo = !isEnabled() ? new Color(0xC9CED6)
                    : getModel().isPressed() ? base.darker().darker()
                    : encima ? base.darker() : base;
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
