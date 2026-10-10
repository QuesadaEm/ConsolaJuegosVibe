package games;

import datos.Puntuacion;
import java.io.*;
import java.util.*;

/**
 * Torre de Super Tierra (tema Helldivers 2). Una torre fija en pantalla; los contenedores van y vienen
 * por la fila de arriba al compas del metronomo y, al pulsar ENTER, caen sobre la torre como en Tetris.
 * Hay varias torres: la primera es un tutorial y cada una siguiente es mas dificil.
 */
public class OperacionSuperTierra implements Jugable {

    private static final String NOMBRE_JUEGO = "SuperTierra";

    private static final int CAMPO = 15;
    private static final int ALTO = 15;          // filas de juego (la fila 0 es la de barrido)
    private static final int BPM_MAXIMO = 190;
    private static final int MS_CAIDA = 40;

    private static final Despliegue[] NIVELES = {
            new Despliegue("Entrenamiento", "TUTORIAL: pulsa ENTER cuando el bloque (#) este sobre la torre.",
                    5, 9, 80, false, 0.0, 0.25),
            new Despliegue("Zona de combate", "El ritmo se acelera y la base es mas estrecha.",
                    8, 7, 110, true, 0.10, 0.15),
            new Despliegue("Frente de Super Tierra", "Ritmo infernal, base minima y carga erratica. Libertad o muerte!",
                    11, 5, 140, true, 0.22, 0.12)
    };

    private final Scanner entrada;
    private final boolean ansi = soportaAnsi();
    private final Random azar = new Random();

    // estado de la partida en curso
    private int puntaje, combo, mejorCombo, perfectos, enBeat, pisosTotales, bpmFinal;
    private String mensaje = "";

    public OperacionSuperTierra() {
        this(new Scanner(System.in));
    }

    public OperacionSuperTierra(Scanner entrada) {
        this.entrada = entrada;
    }

    @Override
    public Puntuacion start() {
        limpiar();
        System.out.println("=== OPERACION: TORRE DE SUPER TIERRA ===");
        System.out.println("Soldado, la Democracia Administrada exige baluartes de suministros. Que la Libertad prevalezca!");
        System.out.println();
        System.out.println(getInstrucciones());
        if (!ansi) {
            System.out.println();
            System.out.println("(Tu consola no redibuja la pantalla: se usara el modo compacto. Para la pantalla fija,");
            System.out.println(" ejecutalo en una terminal o agrega la opcion de VM -Dpantalla=ansi)");
        }

        puntaje = combo = mejorCombo = perfectos = enBeat = pisosTotales = 0;
        bpmFinal = NIVELES[0].getBpm();
        int torresCompletas = 0;

        for (int n = 0; n < NIVELES.length; n++) {
            anunciarTorre(n);
            if (!jugarTorre(n)) break;
            torresCompletas++;
            puntaje += 200 * (n + 1);
            anunciarCompletada(n);
        }

        boolean gano = torresCompletas == NIVELES.length;
        if (gano) {
            limpiar();
            System.out.println(banner("VICTORIA"));
            System.out.println("  Las " + NIVELES.length + " torres se alzan. Super Tierra esta orgullosa de ti, Helldiver!");
        }

        System.out.println();
        System.out.println("  Torres: " + torresCompletas + "/" + NIVELES.length
                + "   Pisos: " + pisosTotales
                + "   Perfectos: " + perfectos
                + "   Mejor combo: " + mejorCombo
                + "   En el ritmo: " + enBeat
                + "   BPM final: " + bpmFinal);

        return pantallaFinal(gano, puntaje);
    }

    private String getInstrucciones() {
        return "ORDENES DE SUPER TIERRA, HELLDIVER:\n"
                + "- Hay " + NIVELES.length + " torres que erigir. La primera es entrenamiento; cada una es mas dura.\n"
                + "- La nave de apoyo pasea un contenedor (#) por la fila superior al ritmo de la Democracia: TUM, tak.\n"
                + "- Pulsa ENTER para soltarlo: caera sobre la torre (=). Lo que sobresalga se pierde.\n"
                + "- Si la carga no toca la torre, la mision fracasa y la Libertad espera.\n"
                + "- Entrega impecable: encadena entregas exactas para multiplicar tu gloria.\n"
                + "- Suelta justo en el TUM y el Alto Mando premiara tu disciplina (+50).\n"
                + "- Completar una torre otorga una bonificacion de Super Tierra.";
    }

    private String getArteVictoria() {
        return String.join("\n",
                "",
                "   *     *     *     *     *     *     *",
                "  =========================================",
                "  ||     MISION CUMPLIDA, HELLDIVER!     ||",
                "  =========================================",
                "              ____________",
                "            /  __________  \\",
                "           |  /  ______  \\  |",
                "           | |  |      |  | |",
                "           | |  | (--) |  | |",
                "           |  \\ |______| /  |",
                "            \\_|__________|_/",
                "              |   ||||   |",
                "          ____|___||||___|____",
                "         |  SUPER TIERRA, LIBRE  |",
                "         |_______________________|",
                "",
                "   LA LIBERTAD PREVALECE. LA DEMOCRACIA TE SALUDA!",
                "   *     *     *     *     *     *     *",
                "");
    }

    // ------------------------------------------------------------------ torres

    private void anunciarTorre(int n) {
        Despliegue nv = NIVELES[n];
        limpiar();
        System.out.println(banner("TORRE " + (n + 1)));
        System.out.println("  " + nv.getNombre().toUpperCase() + "  -  " + nv.getPisos() + " pisos, base de "
                + nv.getAnchoBase() + ", " + nv.getBpm() + " BPM");
        System.out.println("  " + nv.getPista());
        System.out.println();
        System.out.println("  Pulsa ENTER para desplegarte...");
        entrada.nextLine();
    }

    private void anunciarCompletada(int n) {
        limpiar();
        System.out.println(banner("TORRE " + (n + 1) + " COMPLETADA"));
        if (n + 1 < NIVELES.length) {
            System.out.println("  Torre " + (n + 2) + " continua... La Democracia no descansa!");
        }
        dormir(2800);
    }

    /** Juega una torre completa. Devuelve true si se termino, false si la mision fracaso. */
    private boolean jugarTorre(int n) {
        Despliegue nv = NIVELES[n];
        Baluarte torre = new Baluarte(CAMPO, nv.getAnchoBase());
        mensaje = nv.getPista();
        combo = 0;
        limpiar();

        while (torre.getAltura() < nv.getPisos()) {
            int bpm = nv.acelera() ? Math.min(BPM_MAXIMO, nv.getBpm() + 10 * (torre.getAltura() / 3)) : nv.getBpm();
            bpmFinal = bpm;
            Descarga s = barrer(torre, nv, n, bpm);
            Baluarte.Resultado res = torre.soltar(s.posicion);
            if (res == Baluarte.Resultado.FALLO) {
                mensaje = "La carga se pierde en el vacio. Mision fallida, Helldiver";
                combo = 0;
                pantalla(torre, n, bpm, -1, 0, 0, -1, true);
                return false;
            }
            pisosTotales++;
            if (res == Baluarte.Resultado.PERFECTO) {
                perfectos++;
                combo++;
                mejorCombo = Math.max(mejorCombo, combo);
                puntaje += 100 + combo * 20;
                mensaje = "Entrega impecable! Cadena x" + combo;
            } else {
                combo = 0;
                puntaje += torre.getTope().getAncho() * 10;
                mensaje = "Carga mal asentada, se recorta el exceso. Ancho: " + torre.getTope().getAncho();
            }
            if (s.enBeat) {
                enBeat++;
                puntaje += 50;
                mensaje += "  +50 al compas de la Democracia";
            }
        }
        pantalla(torre, n, bpmFinal, -1, 0, 0, -1, true);
        return true;
    }

    /** El contenedor recorre la fila superior al ritmo; al pulsar ENTER cae sobre la torre. */
    private Descarga barrer(Baluarte torre, Despliegue nv, int nivel, int bpm) {
        int ancho = torre.getTope().getAncho();
        long beatMs = 60000L / bpm;
        long pasoMs = beatMs / 2; // dos pasos por beat
        vaciarEntrada();
        if (!ansi) pantalla(torre, nivel, bpm, -1, 0, ancho, -1, false);
        long reloj = System.currentTimeMillis();
        int pos = 0, dir = 1;
        for (long k = 0; ; k++) {
            int tiempo = (int) (k % 2);
            if (ansi) {
                pantalla(torre, nivel, bpm, 0, pos, ancho, tiempo, true);
            } else {
                // Consolas como la de NetBeans no reescriben la linea: cada paso va en una linea nueva
                System.out.println("  " + fila(pos, ancho, ' ', '#') + (tiempo == 0 ? "  TUM" : "  tak"));
                System.out.flush();
            }
            long limite = reloj + (k + 1) * pasoMs;
            while (System.currentTimeMillis() < limite) {
                if (hayEntrada()) {
                    long t = System.currentTimeMillis() - reloj;
                    long resto = t % beatMs;
                    long distancia = Math.min(resto, beatMs - resto);
                    vaciarEntrada();
                    caer(torre, nivel, bpm, pos, ancho);
                    return new Descarga(pos, distancia <= beatMs * nv.getTolerancia());
                }
                dormir(4);
            }
            if (k > 0 && azar.nextDouble() < nv.getRebote()) dir = -dir;
            int sig = pos + dir;
            if (sig < 0 || sig + ancho > CAMPO) {
                dir = -dir;
                sig = pos + dir;
            }
            pos = Math.max(0, Math.min(CAMPO - ancho, sig));
        }
    }

    /** Animacion de caida (solo con pantalla fija). */
    private void caer(Baluarte torre, int nivel, int bpm, int pos, int ancho) {
        if (!ansi) {
            return;
        }
        int filaTope = ALTO - 1 - torre.getAltura();
        for (int fila = 1; fila < filaTope; fila++) {
            pantalla(torre, nivel, bpm, fila, pos, ancho, -1, true);
            dormir(MS_CAIDA);
        }
    }

    // ------------------------------------------------------------------ dibujo

    /** Dibuja la pantalla completa. filaMovil < 0 = sin bloque movil; tiempo 0 = TUM, 1 = tak. */
    private void pantalla(Baluarte torre, int nivel, int bpm, int filaMovil, int posMovil, int ancho,
                          int tiempo, boolean redibujar) {
        Despliegue nv = NIVELES[nivel];
        StringBuilder sb = new StringBuilder();
        String fin = ansi ? "\033[K\n" : "\n";
        if (ansi && redibujar) sb.append("\033[H");
        sb.append("  TORRE ").append(nivel + 1).append("/").append(NIVELES.length).append(" - ")
                .append(nv.getNombre()).append("   Piso ").append(Math.min(torre.getAltura() + 1, nv.getPisos()))
                .append("/").append(nv.getPisos()).append(fin);
        sb.append("  ").append(bpm).append(" BPM   Puntos ").append(puntaje)
                .append("   Cadena x").append(combo).append("   ")
                .append(tiempo == 0 ? "[ TUM ]" : tiempo == 1 ? "[ tak ]" : "       ").append(fin);
        for (int fila = 0; fila < ALTO; fila++) {
            int piso = ALTO - 1 - fila;
            String linea;
            if (piso <= torre.getAltura()) {
                linea = fila(torre.getPisos().get(piso).getIzquierda(), torre.getPisos().get(piso).getAncho(), ' ', '=');
            } else if (fila == filaMovil) {
                linea = fila(posMovil, ancho, ' ', '#');
            } else {
                linea = fila(0, 0, ' ', ' ');
            }
            sb.append("  ").append(linea).append(fin);
        }
        sb.append("  +").append("-".repeat(CAMPO)).append("+").append(fin);
        sb.append("  ").append(mensaje).append(fin);
        if (ansi) sb.append("  [ENTER] soltar la carga").append(fin).append("\033[J");
        else sb.append("  [ENTER] soltar la carga\n");
        System.out.print(sb);
        System.out.flush();
    }

    private String fila(int izquierda, int ancho, char vacio, char lleno) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < CAMPO; i++) {
            sb.append(i >= izquierda && i < izquierda + ancho ? lleno : vacio);
        }
        return sb.append('|').toString();
    }

    /** Titulo grande en un recuadro (reemplaza a la clase Heraldo). */
    private String banner(String texto) {
        String borde = "=".repeat(texto.length() + 8);
        return "\n  " + borde + "\n  ||  " + texto + "  ||\n  " + borde + "\n";
    }

    // ------------------------------------------------------------------ pantalla (reemplaza a Cogitador)

    private static boolean soportaAnsi() {
        String forzado = System.getProperty("pantalla", "");
        if (forzado.equalsIgnoreCase("ansi")) return true;
        if (forzado.equalsIgnoreCase("simple")) return false;
        return System.console() != null && System.getenv("TERM") != null;
    }

    private void limpiar() {
        if (ansi) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } else {
            System.out.println();
        }
    }

    // ------------------------------------------------------------------ entrada

    private boolean hayEntrada() {
        try {
            return System.in.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    private void vaciarEntrada() {
        try {
            while (System.in.available() > 0) System.in.read();
        } catch (IOException ignorado) {
            // nada que vaciar
        }
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ------------------------------------------------------------------ pantalla final

    private Puntuacion pantallaFinal(boolean gano, int puntaje) {
        System.out.println("\n==============================");
        System.out.println(gano ? "        ¡¡ GANASTE !!" : "        PERDISTE :(");
        System.out.println("==============================");
        System.out.println("Puntos de gloria: " + puntaje);

        String jugador;
        do {
            System.out.print("Ingresa tu nombre: ");
            jugador = entrada.nextLine().trim();
        } while (jugador.isEmpty());

        if (gano) {
            System.out.println(getArteVictoria());
        }
        return new Puntuacion(jugador, NOMBRE_JUEGO, puntaje, gano);
    }

    // ================================================================== clases internas
    // (antes eran archivos separados: Despliegue.java, Baluarte.java, Contenedor.java)

    /** Resultado de soltar una carga. */
    private static class Descarga {
        final int posicion;
        final boolean enBeat;

        Descarga(int posicion, boolean enBeat) {
            this.posicion = posicion;
            this.enBeat = enBeat;
        }
    }

    /** Configuracion de una torre (cada torre es mas dificil que la anterior). */
    private static class Despliegue {
        private final String nombre;
        private final String pista;
        private final int pisos;
        private final int anchoBase;
        private final int bpm;
        private final boolean acelera;
        private final double rebote;
        private final double tolerancia;

        Despliegue(String nombre, String pista, int pisos, int anchoBase, int bpm,
                   boolean acelera, double rebote, double tolerancia) {
            this.nombre = nombre;
            this.pista = pista;
            this.pisos = pisos;
            this.anchoBase = anchoBase;
            this.bpm = bpm;
            this.acelera = acelera;
            this.rebote = rebote;
            this.tolerancia = tolerancia;
        }

        String getNombre() { return nombre; }
        String getPista() { return pista; }
        int getPisos() { return pisos; }
        int getAnchoBase() { return anchoBase; }
        int getBpm() { return bpm; }
        /** true = el BPM sube cada 3 pisos. */
        boolean acelera() { return acelera; }
        /** Probabilidad por paso de que el bloque cambie de sentido de golpe. */
        double getRebote() { return rebote; }
        /** Margen (fraccion del beat) para contar como "en el ritmo". */
        double getTolerancia() { return tolerancia; }
    }

    /** La torre de bloques apilados. Decide si un bloque soltado encaja. */
    private static class Baluarte {

        enum Resultado { PERFECTO, PARCIAL, FALLO }

        private final List<Contenedor> pisos = new ArrayList<>();

        Baluarte(int campo, int anchoBase) {
            pisos.add(new Contenedor((campo - anchoBase) / 2, anchoBase));
        }

        List<Contenedor> getPisos() { return Collections.unmodifiableList(pisos); }
        Contenedor getTope() { return pisos.get(pisos.size() - 1); }
        /** Pisos construidos sin contar la base. */
        int getAltura() { return pisos.size() - 1; }

        /** Suelta un bloque (del mismo ancho que el tope) con su borde izquierdo en 'izquierda'. */
        Resultado soltar(int izquierda) {
            Contenedor tope = getTope();
            int ancho = tope.getAncho();
            int izq = Math.max(tope.getIzquierda(), izquierda);
            int der = Math.min(tope.getDerecha(), izquierda + ancho);
            if (der - izq <= 0) return Resultado.FALLO;
            boolean perfecto = izquierda == tope.getIzquierda();
            pisos.add(new Contenedor(izq, der - izq));
            return perfecto ? Resultado.PERFECTO : Resultado.PARCIAL;
        }
    }

    /** Un contenedor de suministros ya colocado en la torre. */
    private static class Contenedor {
        private final int izquierda;
        private final int ancho;

        Contenedor(int izquierda, int ancho) {
            this.izquierda = izquierda;
            this.ancho = ancho;
        }

        int getIzquierda() { return izquierda; }
        int getAncho() { return ancho; }
        int getDerecha() { return izquierda + ancho; }
    }
}
