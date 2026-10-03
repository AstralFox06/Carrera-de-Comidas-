import java.awt.*;

/** Helado: da 3 puntos de golpe. Probabilidad 25%. */
public class Helado extends Poder {

    public Helado() {
        super(0.25);
    }

    @Override
    public void aplicar(Jugador j) {
        j.sumarPuntos(3);
    }

    @Override
    public String getMensaje() {
        return "¡HELADO! +3 puntos de una";
    }
