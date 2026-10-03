/** Jugador controlado por la computadora: "presiona" solo, a velocidad aleatoria. */
public class JugadorCPU extends Jugador {
    private final double clicksPorSegundo;
    private double acumulado = 0;
    private long ultimo = 0;

    public JugadorCPU(String nombre) {
        super(nombre);
        this.clicksPorSegundo = 4.0 + Math.random() * 2.0; // entre 4 y 6 clicks/seg
    }

    @Override
    public boolean actualizar() {
        long ahora = System.currentTimeMillis();
        if (ultimo == 0) ultimo = ahora;
        acumulado += clicksPorSegundo * (ahora - ultimo) / 1000.0;
        ultimo = ahora;
        boolean comio = false;
        while (acumulado >= 1) {
