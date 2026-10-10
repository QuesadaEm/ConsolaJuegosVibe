package games;

import datos.Puntuacion;
import java.util.*;

public class Buscaminas implements Jugable {

    private static final String NOMBRE_JUEGO = "Buscaminas";
    private static final int TAM = 8;
    private static final int MINAS = 10;

    private final Scanner sc;

    private boolean[][] minas;
    private boolean[][] descubierta;
    private boolean[][] marcada;
    private int[][] vecinas;
    private int descubiertas;
    private boolean primeraJugada;

    public Buscaminas() {
        this(new Scanner(System.in));
    }

    public Buscaminas(Scanner sc) {
        this.sc = sc;
    }

    @Override
    public Puntuacion start() {
        minas = new boolean[TAM][TAM];
        descubierta = new boolean[TAM][TAM];
        marcada = new boolean[TAM][TAM];
        vecinas = new int[TAM][TAM];
        descubiertas = 0;
        primeraJugada = true;

        int seguras = TAM * TAM - MINAS;
        boolean exploto = false;

        System.out.println("\n===== BUSCAMINAS =====");
        System.out.println("Comandos: d fila columna  -> descubrir");
        System.out.println("          m fila columna  -> marcar/desmarcar bomba");

        while (!exploto && descubiertas < seguras) {
            mostrarTablero(false);
            System.out.print("Comando: ");
            String[] partes = sc.nextLine().trim().toLowerCase().split("\\s+");

            if (partes.length != 3) {
                System.out.println("Formato invalido. Ejemplo: d 3 5");
                continue;
            }

            int f, c;
            try {
                f = Integer.parseInt(partes[1]) - 1;
                c = Integer.parseInt(partes[2]) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Fila y columna deben ser numeros.");
                continue;
            }
            if (f < 0 || f >= TAM || c < 0 || c >= TAM) {
                System.out.println("Fuera del tablero. Usa numeros del 1 al " + TAM + ".");
                continue;
            }

            switch (partes[0]) {
                case "d" -> {
                    if (marcada[f][c]) {
                        System.out.println("Esa casilla esta marcada. Desmarcala primero.");
                    } else if (descubierta[f][c]) {
                        System.out.println("Esa casilla ya esta descubierta.");
                    } else {
                        if (primeraJugada) {
                            colocarMinas(f, c);
                            primeraJugada = false;
                        }
                        if (minas[f][c]) {
                            exploto = true;
                        } else {
                            descubrir(f, c);
                        }
                    }
                }
                case "m" -> {
                    if (descubierta[f][c]) {
                        System.out.println("No puedes marcar una casilla descubierta.");
                    } else {
                        marcada[f][c] = !marcada[f][c];
                    }
                }
                default -> System.out.println("Comando no valido. Usa d o m.");
            }
        }

        mostrarTablero(true);
        boolean gano = !exploto;
        int puntaje;

        if (gano) {
            System.out.println("¡Despejaste todo el campo!");
            puntaje = MINAS;
        } else {
            System.out.println("¡BOOM! Pisaste una bomba.");
            puntaje = calcularBombasDesactivadas();
        }

        return pantallaFinal(gano, puntaje);
    }

    // ---------- Lógica del juego ----------

    private void colocarMinas(int filaSegura, int colSegura) {
        Random r = new Random();
        int colocadas = 0;
        while (colocadas < MINAS) {
            int f = r.nextInt(TAM);
            int c = r.nextInt(TAM);
            if (!minas[f][c] && !(f == filaSegura && c == colSegura)) {
                minas[f][c] = true;
                colocadas++;
            }
        }
        calcularVecinas();
    }

    private void calcularVecinas() {
        for (int f = 0; f < TAM; f++) {
            for (int c = 0; c < TAM; c++) {
                int cuenta = 0;
                for (int df = -1; df <= 1; df++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nf = f + df, nc = c + dc;
                        if (nf >= 0 && nf < TAM && nc >= 0 && nc < TAM && minas[nf][nc]) {
                            cuenta++;
                        }
                    }
                }
                vecinas[f][c] = cuenta;
            }
        }
    }

    private void descubrir(int f, int c) {
        if (f < 0 || f >= TAM || c < 0 || c >= TAM) return;
        if (descubierta[f][c] || marcada[f][c] || minas[f][c]) return;

        descubierta[f][c] = true;
        descubiertas++;

        if (vecinas[f][c] == 0) {
            for (int df = -1; df <= 1; df++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (df != 0 || dc != 0) {
                        descubrir(f + df, c + dc);
                    }
                }
            }
        }
    }

    // Marcas correctas menos marcas equivocadas (nunca menos de 0).
    // Asi no sirve marcar todo el tablero para sacar puntos.
    private int calcularBombasDesactivadas() {
        int correctas = 0;
        int equivocadas = 0;
        for (int f = 0; f < TAM; f++) {
            for (int c = 0; c < TAM; c++) {
                if (marcada[f][c] && minas[f][c]) {
                    correctas++;
                } else if (marcada[f][c]) {
                    equivocadas++;
                }
            }
        }
        return Math.max(0, correctas - equivocadas);
    }

    private void mostrarTablero(boolean revelar) {
        int marcas = 0;
        System.out.print("\n   ");
        for (int c = 0; c < TAM; c++) {
            System.out.printf("%2d", c + 1);
        }
        System.out.println();

        for (int f = 0; f < TAM; f++) {
            System.out.printf("%2d ", f + 1);
            for (int c = 0; c < TAM; c++) {
                char s;
                if (marcada[f][c]) marcas++;

                if (revelar && minas[f][c]) {
                    s = '*';
                } else if (marcada[f][c]) {
                    s = 'F';
                } else if (!descubierta[f][c]) {
                    s = '#';
                } else if (vecinas[f][c] == 0) {
                    s = '.';
                } else {
                    s = (char) ('0' + vecinas[f][c]);
                }
                System.out.printf("%2c", s);
            }
            System.out.println();
        }
        System.out.println("Marcas: " + marcas + " / " + MINAS);
    }

    // ---------- Pantalla final ----------

    private Puntuacion pantallaFinal(boolean gano, int puntaje) {
        System.out.println("\n==============================");
        System.out.println(gano ? "        ¡¡ GANASTE !!" : "        PERDISTE :(");
        System.out.println("==============================");
        System.out.println("Bombas desactivadas: " + puntaje);

        String jugador;
        do {
            System.out.print("Ingresa tu nombre: ");
            jugador = sc.nextLine().trim();
        } while (jugador.isEmpty());

        return new Puntuacion(jugador, NOMBRE_JUEGO, puntaje, gano);
    }
}