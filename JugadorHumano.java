/** Jugador controlado con el teclado. */
public class JugadorHumano extends Jugador {
    private final char tecla;

    public JugadorHumano(String nombre, char tecla) {
        super(nombre);
        this.tecla = Character.toLowerCase(tecla);
    }

    public char getTecla() { return tecla; }

    @Override
    public boolean actualizar() {
        return false; // el humano juega con el teclado, no automáticamente
    }

    @Override
    public String getControl() {
        return "Tecla [" + Character.toUpperCase(tecla) + "]";
    }
}
