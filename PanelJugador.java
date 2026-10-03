import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Panel donde se dibuja y se juega la carrera de hamburguesas.
 * Hace tres cosas: (1) lleva la lógica del juego, (2) escucha el teclado
 * y (3) dibuja todo en pantalla.
 */
public class PanelJuego extends JPanel implements KeyListener {

    // ============================================================
    //  ATRIBUTOS Y CONSTANTES
    // ============================================================

    /** Los 3 estados posibles de la partida. */
    private enum Estado { ESPERA, JUGANDO, FIN }

    private static final int DURACION = 60; // segundos
    // Posiciones en pantalla: donde nace la hamburguesa, donde llega y donde está la cara
    private static final int IZQ = 150, DER = 640, CARA_X = 790;
    // El "dado" de los poderes se tira en momentos al azar: entre 3 y 8 segundos
    private static final long SORTEO_MIN = 3000, SORTEO_MAX = 8000;

    private final Jugador[] jugadores;     // [0] = jugador 1, [1] = jugador 2 o CPU
    private final double[] posX;           // posición X actual de la hamburguesa de cada carril
    private final int[] previas;           // hamburguesas comidas la última vez (para detectar cuando se come una)
    private final long[] masticando;       // hasta qué momento la cara está "masticando"
    private final Set<Character> presionadas = new HashSet<>(); // teclas que siguen apretadas
    private final Runnable volverAlMenu;   // qué hacer al presionar ESC
    private final Timer timer;             // reloj que repite tick() cada 16 ms

    // Poderes compartidos (hot dog y helado): le llegan a AMBOS jugadores
    private final Poder[] poderes = { new HotDog(), new Helado() };
    private Poder poderActual;   // el último poder que salió
    private long poderHasta;     // hasta cuándo se muestra en pantalla
    private long proximoSorteo;  // cuándo toca el siguiente sorteo
    private int poderX;          // posición X al azar donde aparece el poder en el carril

    // Hot cake: sorteo independiente por jugador; le quita 2 pts al rival
    private final Poder hotCake = new HotCake();
    private final long[] proximoHotCake = new long[2]; // cuándo le toca el sorteo a cada jugador
    private final long[] hotCakeHasta = new long[2];   // hasta cuándo se muestra en su carril

    private Estado estado = Estado.ESPERA;
    private long inicio;                   // momento en que empezó la partida
    private double restante = DURACION;    // segundos que quedan

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public PanelJuego(Jugador j1, Jugador j2, Runnable volverAlMenu) {
        this.jugadores = new Jugador[]{j1, j2};
        this.posX = new double[]{IZQ, IZQ};
        this.previas = new int[2];
        this.masticando = new long[2];
        this.volverAlMenu = volverAlMenu;
        setPreferredSize(new Dimension(920, 520));
        setFocusable(true);       // necesario para recibir teclas
        addKeyListener(this);     // "este panel escucha el teclado"
        timer = new Timer(16, e -> tick()); // ~60 veces por segundo
        timer.start();
    }

    // ============================================================
    //  LÓGICA DEL JUEGO
    // ============================================================

    /** Se ejecuta ~60 veces por segundo: actualiza el tiempo, los poderes y las posiciones. */
    private void tick() {
        long ahora = System.currentTimeMillis();
        if (estado == Estado.JUGANDO) {
            // --- Tiempo restante ---
            restante = DURACION - (ahora - inicio) / 1000.0;
            if (restante <= 0) {
                restante = 0;
                estado = Estado.FIN;
            }
            for (Jugador j : jugadores) j.actualizar(); // polimorfismo: humano no hace nada, CPU juega solo

            // --- Sorteo de poderes compartidos (hot dog / helado) ---
            if (estado == Estado.JUGANDO && ahora >= proximoSorteo) {
                proximoSorteo = ahora + intervaloAleatorio(); // el próximo sorteo será en un momento al azar
                Poder p = sortearPoder();
                if (p != null) {
                    for (Jugador j : jugadores) p.aplicar(j); // le llega a AMBOS jugadores
                    poderActual = p;
                    poderHasta = ahora + 2000;
                    poderX = IZQ + 20 + (int) (Math.random() * (DER - IZQ - 120)); // lugar al azar del carril
                }
            }

            // --- Hot cake: cada jugador tiene su propio reloj, así no salen al mismo tiempo ---
            for (int i = 0; i < 2; i++) {
                if (estado == Estado.JUGANDO && ahora >= proximoHotCake[i]) {
                    proximoHotCake[i] = ahora + 5000 + (long) (Math.random() * 4000); // entre 5 y 9 s
                    int rival = 1 - i;
                    boolean rivalLoTiene = ahora < hotCakeHasta[rival]; // nunca dos a la vez
                    if (!rivalLoTiene && Math.random() < hotCake.getProbabilidad()) {
                        hotCake.aplicar(jugadores[rival]); // el rival pierde 2 pts
                        hotCakeHasta[i] = ahora + 1500;
                    }
                }
            }
        }

        // --- Animación de las hamburguesas y la cara (se hace siempre) ---
        for (int i = 0; i < 2; i++) {
            Jugador j = jugadores[i];
            if (j.getComidas() != previas[i]) { // ¡se comió una!
                previas[i] = j.getComidas();
                masticando[i] = ahora + 250;    // la cara mastica 0.25 s
                posX[i] = IZQ;                  // la nueva hamburguesa nace a la izquierda
            }
            // La hamburguesa avanza según los clicks (0/6, 1/6 ... 5/6 del camino)
            double objetivo = IZQ + (DER - IZQ) * (j.getClicks() / (double) Jugador.CLICKS_POR_HAMBURGUESA);
            posX[i] += (objetivo - posX[i]) * 0.3; // se desliza suave hacia el objetivo
        }
        repaint(); // pide volver a dibujar la pantalla
    }

    /** Devuelve un tiempo al azar (en milisegundos) entre SORTEO_MIN y SORTEO_MAX. */
    private long intervaloAleatorio() {
        return SORTEO_MIN + (long) (Math.random() * (SORTEO_MAX - SORTEO_MIN));
    }

    /** Devuelve un poder según su probabilidad, o null si no sale nada. */
    private Poder sortearPoder() {
        double r = Math.random();   // número entre 0 y 1
        double acumulado = 0;
        for (Poder p : poderes) {
            acumulado += p.getProbabilidad();   // 0.17, luego 0.42
            if (r < acumulado) return p;
        }
        return null;                // el resto (58%): no sale nada
    }

    /** Busca a qué jugador pertenece la tecla y le suma un click. */
    private void registrarClick(char c) throws PartidaNoActivaException {
        if (estado != Estado.JUGANDO) {
            throw new PartidaNoActivaException("La partida no está en curso");
        }
        for (Jugador j : jugadores) {
            if (j instanceof JugadorHumano) { // la CPU no usa teclado
                JugadorHumano h = (JugadorHumano) j;
                if (h.getTecla() == c) h.presionar();
            }
        }
    }

    /** Deja todo listo para empezar (o repetir) una partida. */
    private void reiniciar() {
        for (Jugador j : jugadores) j.reiniciar();
        for (int i = 0; i < 2; i++) { posX[i] = IZQ; previas[i] = 0; }
        restante = DURACION;
        inicio = System.currentTimeMillis();
        proximoSorteo = inicio + intervaloAleatorio();
        poderActual = null;
        for (int i = 0; i < 2; i++) {
            proximoHotCake[i] = inicio + 3000 + (long) (Math.random() * 5000); // arranque distinto por jugador
            hotCakeHasta[i] = 0;
        }
        estado = Estado.JUGANDO;
    }

    // ============================================================
    //  TECLADO
    // ============================================================

    /** Se llama cuando se aprieta una tecla. */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {   // ESC: volver al menú
            timer.stop();
            volverAlMenu.run();
            return;
        }
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) { // ENTER: empezar / revancha
            if (estado != Estado.JUGANDO) reiniciar();
            return;
        }
        char c = Character.toLowerCase(e.getKeyChar());
        if (presionadas.contains(c)) return; // evita que mantener la tecla cuente como clicks
        presionadas.add(c);
        try {
            registrarClick(c);
        } catch (PartidaNoActivaException ex) {
            // Se ignora: aún no empieza o ya terminó la partida.
        }
    }

    /** Se llama al soltar la tecla: la quita de "presionadas" para que el próximo click cuente. */
    @Override
    public void keyReleased(KeyEvent e) {
        presionadas.remove(Character.toLowerCase(e.getKeyChar()));
    }

    @Override public void keyTyped(KeyEvent e) { } // obligatorio por la interfaz, no se usa

    // ============================================================
    //  DIBUJO
    // ============================================================

    /** Dibuja toda la pantalla. Swing la llama cada vez que se hace repaint(). */
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // --- Fondo con degradado y título ---
        g.setPaint(new GradientPaint(0, 0, new Color(24, 20, 30), 0, getHeight(), new Color(48, 28, 20)));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(255, 170, 60));
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString("HAMBURGUESAS LOCAS", 30, 40);

        // --- Barra de tiempo (se pone roja en los últimos 10 s) ---
        g.setColor(new Color(255, 255, 255, 40));
        g.fillRoundRect(30, 55, 860, 14, 14, 14);
        g.setColor(restante < 10 ? new Color(230, 70, 60) : new Color(255, 170, 60));
        g.fillRoundRect(30, 55, (int) (860 * restante / DURACION), 14, 14, 14);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(String.format("%.1f s", restante), 810, 36);

        long ahora = System.currentTimeMillis();
        boolean hayPoder = poderActual != null && ahora < poderHasta; // ¿hay un poder en pantalla?

        // --- Aviso del poder compartido ---
        if (hayPoder) {
            g.setColor(new Color(255, 230, 120));
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            centrar(g, poderActual.getMensaje(), 100);
        }

        // --- Un carril por jugador ---
        for (int i = 0; i < 2; i++) {
            Jugador j = jugadores[i];
            int y = 120 + i * 175; // el carril 2 queda 175 px más abajo

            // Nombre y tipo de control
            g.setColor(i == 0 ? new Color(120, 180, 255) : new Color(255, 130, 160));
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString(j.getNombre(), 30, y + 20);
            g.setColor(new Color(200, 200, 200));
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString(j.getControl(), 30, y + 40);

            // Puntaje, hamburguesas comidas y multiplicador activo
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            g.setColor(Color.WHITE);
            g.drawString(j.getPuntos() + " pts", 30, y + 80);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(new Color(200, 200, 200));
            g.drawString("Hamburguesas: " + j.getComidas(), 30, y + 100);
            if (j.getMultiplicador() > 0) {
                g.setColor(new Color(255, 200, 60));
                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                g.drawString("x2 (" + j.getMultiplicador() + " restantes)", 120, y + 80);
            }

            // Carril (rectángulo semitransparente)
            g.setColor(new Color(255, 255, 255, 25));
            g.fillRoundRect(IZQ - 20, y + 5, DER - IZQ + 130, 85, 20, 20);

            // Poder compartido, hot cake (si le salió a este jugador), hamburguesa y cara
            if (hayPoder) poderActual.dibujar(g, poderX, y + 15);
            if (ahora < hotCakeHasta[i]) {
                hotCake.dibujar(g, 500, y + 15);
                g.setColor(new Color(255, 120, 80));
                g.setFont(new Font("SansSerif", Font.BOLD, 16));
                g.drawString("¡HOT CAKE! -2 pts a " + jugadores[1 - i].getNombre(), IZQ, y + 112);
            }
            dibujarHamburguesa(g, (int) posX[i], y + 20);
            dibujarCara(g, CARA_X, y + 48, ahora < masticando[i]);
        }

        // --- Pantallas de inicio y de final ---
        if (estado == Estado.ESPERA) {
            mensaje(g, "¿Listos para comer?", "Presiona ENTER para empezar  ·  ESC = menú");
        } else if (estado == Estado.FIN) {
            int a = jugadores[0].getPuntos(), b = jugadores[1].getPuntos();
            String res = a == b ? "¡EMPATE!" : "¡Gana " + (a > b ? jugadores[0].getNombre() : jugadores[1].getNombre()) + "!";
            mensaje(g, res + "  (" + a + " - " + b + ")", "ENTER = revancha  ·  ESC = menú");
        }
    }

    /** Pantalla oscura con un título grande y un subtítulo (inicio y final). */
    private void mensaje(Graphics2D g, String titulo, String sub) {
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(255, 170, 60));
        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        centrar(g, titulo, getHeight() / 2 - 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        centrar(g, sub, getHeight() / 2 + 35);
    }

    /** Escribe un texto centrado horizontalmente. */
    private void centrar(Graphics2D g, String s, int y) {
        g.drawString(s, (getWidth() - g.getFontMetrics().stringWidth(s)) / 2, y);
    }

    /** Dibuja una hamburguesa con figuras: pan, semillas, lechuga, carne y pan. */
    private void dibujarHamburguesa(Graphics2D g, int x, int y) {
        g.setColor(new Color(222, 150, 60));
        g.fillArc(x, y, 70, 50, 0, 180);                       // pan de arriba
        g.setColor(new Color(245, 225, 160));
        g.fillOval(x + 18, y + 6, 4, 3);
        g.fillOval(x + 35, y + 3, 4, 3);
        g.fillOval(x + 50, y + 8, 4, 3);                       // semillas
        g.setColor(new Color(90, 170, 60));
        g.fillRoundRect(x - 4, y + 24, 78, 8, 8, 8);           // lechuga
        g.setColor(new Color(100, 50, 30));
        g.fillRoundRect(x - 2, y + 30, 74, 12, 10, 10);        // carne
        g.setColor(new Color(222, 150, 60));
        g.fillRoundRect(x, y + 42, 70, 12, 12, 12);            // pan de abajo
    }

    /** Dibuja la carita que se come las hamburguesas; la boca se cierra al masticar. */
    private void dibujarCara(Graphics2D g, int cx, int cy, boolean mastica) {
        g.setColor(new Color(255, 205, 80));
        g.fillOval(cx - 45, cy - 45, 90, 90);                  // cabeza
        g.setColor(Color.BLACK);
        g.fillOval(cx - 22, cy - 22, 10, 12);
        g.fillOval(cx + 8, cy - 22, 10, 12);                   // ojos
        int abierto = mastica ? 6 : 28;                        // boca cerrada si está masticando
        g.setColor(new Color(120, 30, 30));
        g.fillOval(cx - 28, cy + 8, 40, abierto);
        g.setColor(Color.BLACK);
        g.drawOval(cx - 28, cy + 8, 40, abierto);
    }
}
