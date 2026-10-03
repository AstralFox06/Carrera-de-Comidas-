/**
 * Clase base (HERENCIA). Un jugador come una hamburguesa cada 6 clicks.
 * Es "abstract": no se puede crear un Jugador a secas, solo sus hijos
 * (JugadorHumano y JugadorCPU).
 */
public abstract class Jugador {

    // ================= ATRIBUTOS =================

    /** Cuántos clicks hacen falta para comerse una hamburguesa. */
    public static final int CLICKS_POR_HAMBURGUESA = 6;

    protected String nombre;     // nombre que se muestra en pantalla
    protected int clicks;        // clicks acumulados para la hamburguesa actual (0-5)
    protected int comidas;       // hamburguesas comidas
    protected int puntos;        // puntaje (es lo que decide quién gana)
    protected int multiplicador; // cuántas hamburguesas más valen x2 (hot dog)

    // ================= CONSTRUCTOR =================

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    // ================= LÓGICA DEL JUEGO =================

    /**
     * Suma un click. Devuelve true si con este click se comió una hamburguesa.
     * Es el corazón del juego: se llama cada vez que el jugador presiona su tecla.
     */
    public boolean presionar() {
        clicks++; // un click más para la hamburguesa actual
        if (clicks >= CLICKS_POR_HAMBURGUESA) {
            clicks = 0;   // la hamburguesa se terminó: llega otra desde cero
            comidas++;    // cuenta como hamburguesa comida
            if (multiplicador > 0) {
                puntos += 2;      // hamburguesa con x2
                multiplicador--;  // gasta una de las 3 hamburguesas del hot dog
            } else {
                puntos += 1;      // hamburguesa normal
            }
            return true;
        }
        return false; // todavía no se la come
    }

    // ================= PODERES =================

    /** Poder del hot dog: las próximas n hamburguesas valen doble. */
    public void activarMultiplicador(int n) {
        multiplicador = n; // se reinicia (no se acumula si ya tenía uno activo)
    }

    /** Poder del helado: puntos directos. */
    public void sumarPuntos(int n) {
        puntos += n;
    }

    /** Poder del hot cake: quita puntos (nunca baja de 0). */
    public void restarPuntos(int n) {
        puntos = Math.max(0, puntos - n); // Math.max evita puntajes negativos
    }

    // ================= MÉTODOS ABSTRACTOS (los hijos DEBEN definirlos) =================

    /** POLIMORFISMO: cada tipo de jugador decide cómo "juega" en cada tick. */
    public abstract boolean actualizar();

    /** POLIMORFISMO: descripción distinta según el tipo de jugador. */
    public abstract String getControl();

    // ================= REINICIO =================

    /** Deja todo en cero para una revancha. */
    public void reiniciar() {
        clicks = 0;
        comidas = 0;
        puntos = 0;
        multiplicador = 0;
    }

    //  GETTERS (el panel los usa para dibujar)

    public String getNombre()      { return nombre; }
    public int getClicks()         { return clicks; }
    public int getComidas()        { return comidas; }
    public int getPuntos()         { return puntos; }
    public int getMultiplicador()  { return multiplicador; }
}
