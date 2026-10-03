import java.awt.Graphics2D;

/** Clase base (HERENCIA) de los poderes que aparecen en pantalla. */
public abstract class Poder {
    protected final double probabilidad; // 0.17 = 17%

    public Poder(double probabilidad) {
        this.probabilidad = probabilidad;
    }

    public double getProbabilidad() { return probabilidad; }

    /** POLIMORFISMO: cada poder hace algo distinto al jugador. */
    public abstract void aplicar(Jugador j);

    /** Texto que se muestra en pantalla cuando sale el poder. */
    public abstract String getMensaje();

    /** Cada poder se dibuja a su manera. */
    public abstract void dibujar(Graphics2D g, int x, int y);
}
