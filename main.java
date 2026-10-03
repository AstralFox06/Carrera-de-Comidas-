import java.awt.*;
import javax.swing.*;

/** Menú inicial del juego. */
public class Main extends JFrame {

    public Main() {
        super("Carrera de comidas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel p = new JPanel(new GridLayout(0, 1, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        p.setBackground(new Color(30, 24, 30));

        JLabel titulo = new JLabel("CARRERA DE HAMBURGUESAS", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(new Color(255, 170, 60));
        JLabel sub = new JLabel("¿Quién se come más en 1 minuto?", SwingConstants.CENTER);
        sub.setForeground(Color.WHITE);
        p.add(titulo);
        p.add(sub);

        p.add(boton("2 Jugadores", e -> iniciar(false)));
        p.add(boton("Jugador 1 vs CPU", e -> iniciar(true)));
        p.add(boton("Manual de usuario", e -> manual()));
        p.add(boton("Salir", e -> System.exit(0)));

        setContentPane(p);
        pack();
        setLocationRelativeTo(null);
    }

    private JButton boton(String texto, java.awt.event.ActionListener al) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.addActionListener(al);
        return b;
    }

    private String pedirNombre(String mensaje, String porDefecto) throws Exception {
        String n = JOptionPane.showInputDialog(this, mensaje, porDefecto);
        if (n == null) throw new Exception("Cancelado");
        n = n.trim();
        if (n.length() > 12) throw new IllegalArgumentException("Máximo 12 letras");
        return n.isEmpty() ? porDefecto : n;
    }

    private void iniciar(boolean contraCPU) {
        try {
            String n1 = pedirNombre("Nombre del Jugador 1 (tecla Ñ):", "Jugador 1");
            Jugador j1 = new JugadorHumano(n1, '\u00f1'); // 'ñ'
            Jugador j2;
            if (contraCPU) {
                j2 = new JugadorCPU("CPU");
            } else {
                String n2 = pedirNombre("Nombre del Jugador 2 (tecla A):", "Jugador 2");
                j2 = new JugadorHumano(n2, 'a');
            }
            abrirJuego(j1, j2);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Nombre inválido: " + ex.getMessage(), "Ups", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            // El usuario canceló: no pasa nada, se queda en el menú.
        }
    }

    private void abrirJuego(Jugador j1, Jugador j2) {
        setVisible(false);
        JFrame f = new JFrame("Hamburguesas Locas - ¡A comer!");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        PanelJuego panel = new PanelJuego(j1, j2, () -> {
            f.dispose();
            setVisible(true);
        });
        f.add(panel);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        panel.requestFocusInWindow();
    }

    private void manual() {
        String txt =
                "MANUAL DEL COMELÓN\n\n"
                        + "Objetivo: comerte más hamburguesas que tu rival en 60 segundos.\n\n"
                        + "Jugador 1: machaca la tecla Ñ\n"
                        + "Jugador 2: machaca la tecla A\n\n"
                        + "Cada click acerca la hamburguesa a tu boca.\n"
                        + "¡Cada 6 clicks te la tragas y suma 1 punto!\n\n"
                        + "Mantener la tecla apretada NO sirve: hay que clickear de verdad.\n\n"
                        + "ENTER = empezar / revancha\n"
                        + "ESC = volver al menú\n\n"
                        + "Consejo: estira los dedos antes de jugar.\n"
                        + "El que pierde invita LAS BURGUER.";
        JOptionPane.showMessageDialog(this, txt, "Manual de usuario", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
