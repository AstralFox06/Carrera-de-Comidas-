import java.awt.*;

/**
 * Hot cake: le quita 2 puntos al RIVAL de quien lo recibe.
 * Aparece por separado para cada jugador (no al mismo tiempo).
 * Probabilidad: 35% cada vez que le toca el sorteo a ese jugador.
 */
public class HotCake extends Poder {

    public static final int PUNTOS_QUITADOS = 2;

    public HotCake() {
        super(0.35);
    }

    /** Ojo: aquí "j" es la VÍCTIMA (el rival), no quien recibe el hot cake. */
    @Override
    public void aplicar(Jugador j) {
        j.restarPuntos(PUNTOS_QUITADOS);
    }

    @Override
    public String getMensaje() {
        return "¡HOT CAKE! -" + PUNTOS_QUITADOS + " pts al rival";
    }

    @Override
    public void dibujar(Graphics2D g, int x, int y) {
        for (int k = 0; k < 3; k++) {                         // 3 hot cakes apilados
            int yy = y + 38 - k * 11;
            g.setColor(new Color(215, 160, 80));
            g.fillOval(x, yy, 70, 20);
            g.setColor(new Color(160, 110, 50));
            g.drawOval(x, yy, 70, 20);
        }
        g.setColor(new Color(250, 225, 90));
        g.fillRect(x + 25, y + 8, 20, 8);                     // mantequilla
        g.setColor(new Color(120, 60, 20));
        g.fillOval(x + 10, y + 14, 50, 12);                   // miel
    }
}
