package games;

import datos.Puntuacion;
import java.util.*;

public class RescateEspacial implements Jugable {

    private static final String NOMBRE_JUEGO = "RescateEspacial";
    private static final int SIZE = 8;
    private static final int PUNTOS_BASE = 100;

    private final Scanner scanner;

    public RescateEspacial() {
        this(new Scanner(System.in));
    }

    public RescateEspacial(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Puntuacion start() {
        System.out.println("\n=== RESCATE ESPACIAL ===");
        System.out.println("Eres la nave 'X'. Encuentra la capsula de escape 'O' en el campo de asteroides '*'.");
        System.out.println("Controles: [W]=Arriba, [S]=Abajo, [A]=Izquierda, [D]=Derecha | [Q]=Salir");

        int pX = 0;
        int pY = 0;

        // La capsula nunca aparece encima de un asteroide
        Random r = new Random();
        int metaX, metaY;
        do {
            metaX = r.nextInt(SIZE - 2) + 2;
            metaY = r.nextInt(SIZE - 2) + 2;
        } while (esAsteroide(metaX, metaY));

        boolean jugando = true;
        boolean gano = false;
        int movimientos = 0;

        while (jugando) {
            dibujarMapa(pX, pY, metaX, metaY, movimientos);

            if (pX == metaX && pY == metaY) {
                System.out.println("\n¡MISION CUMPLIDA! Rescataste la capsula en " + movimientos + " movimientos.");
                gano = true;
                jugando = false;
                continue;
            }

            System.out.print("Mover (W/A/S/D) o [Q] para huir: ");
            String mov = scanner.nextLine().trim().toUpperCase();

            if (mov.equals("Q")) {
                System.out.println("Mision abortada. Volviendo al centro de mando...");
                jugando = false;
                continue;
            }

            int prevX = pX;
            int prevY = pY;

            if (mov.equals("W") && pY > 0) pY--;
            if (mov.equals("S") && pY < SIZE - 1) pY++;
            if (mov.equals("A") && pX > 0) pX--;
            if (mov.equals("D") && pX < SIZE - 1) pX++;

            if (prevX == pX && prevY == pY) {
                System.out.println("Movimiento bloqueado o no valido.");
            } else {
                movimientos++;
                if (esAsteroide(pX, pY)) {
                    System.out.println("\n¡BOOM! Has chocado contra un asteroide. Nave destruida.");
                    System.out.println("--- FIN DEL JUEGO ---");
                    jugando = false;
                }
            }
        }

        // Menos movimientos = mas puntos. Si pierde o huye, 0 puntos.
        int puntaje = gano ? Math.max(1, PUNTOS_BASE - movimientos) : 0;
        return pantallaFinal(gano, puntaje);
    }

    // ---------- Lógica del juego ----------

    // Asteroides estaticos basados en una formula con las coordenadas
    private boolean esAsteroide(int x, int y) {
        return (x * y + 3) % 7 == 0;
    }

    private void dibujarMapa(int pX, int pY, int metaX, int metaY, int movimientos) {
        System.out.println("\nSector Espacial " + (movimientos + 1) + ":");
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (x == pX && y == pY) {
                    System.out.print("[X]");
                } else if (x == metaX && y == metaY) {
                    System.out.print("[O]");
                } else if (esAsteroide(x, y)) {
                    System.out.print(" * ");
                } else {
                    System.out.print(" . ");
                }
            }
            System.out.println();
        }
    }

    // ---------- Pantalla final ----------

    private Puntuacion pantallaFinal(boolean gano, int puntaje) {
        System.out.println("\n==============================");
        System.out.println(gano ? "        ¡¡ GANASTE !!" : "        PERDISTE :(");
        System.out.println("==============================");
        System.out.println("Puntos de rescate: " + puntaje);

        String jugador;
        do {
            System.out.print("Ingresa tu nombre: ");
            jugador = scanner.nextLine().trim();
        } while (jugador.isEmpty());

        return new Puntuacion(jugador, NOMBRE_JUEGO, puntaje, gano);
    }
}
