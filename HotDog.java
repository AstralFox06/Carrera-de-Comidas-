import java.awt.*;

/** Hot dog: las próximas 3 hamburguesas valen doble. Probabilidad 17%. */
public class HotDog extends Poder {

    public HotDog() {
        super(0.17);
    }

    @Override
    public void aplicar(Jugador j) {
        j.activarMultiplicador(3);
    }

    @Override
    public String getMensaje() {
        return "¡HOT DOG! Las próximas 3 hamburguesas valen x2";
    }
