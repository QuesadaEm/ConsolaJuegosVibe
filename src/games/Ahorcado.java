package games;

import datos.Puntuacion;
import java.util.*;

public class Ahorcado implements Jugable {

    private static final String NOMBRE_JUEGO = "Ahorcado";
    private static final int MAX_ERRORES = 6;
    private static final String[] PALABRAS = {
        "PROGRAMA", "COMPUTADORA", "TECLADO", "PANTALLA", "VARIABLE",
        "JAVA", "CONSOLA", "ALGORITMO", "MEMORIA", "INTERNET"
    };

    private final Scanner sc;

    public Ahorcado() {
        this(new Scanner(System.in));
    }

    public Ahorcado(Scanner sc) {
        this.sc = sc;
    }

    @Override
    public Puntuacion start() {
        String palabra = PALABRAS[new Random().nextInt(PALABRAS.length)];
        Set<Character> usadas = new TreeSet<>();
        int errores = 0;

        System.out.println("\n===== AHORCADO =====");

        while (errores < MAX_ERRORES && !palabraCompleta(palabra, usadas)) {
            dibujar(errores);
            System.out.println("Palabra: " + mostrarPalabra(palabra, usadas));
            System.out.println("Letras usadas: " + usadas);
            System.out.println("Intentos restantes: " + (MAX_ERRORES - errores));
            System.out.print("Ingresa una letra: ");

            String entrada = sc.nextLine().trim().toUpperCase();
            if (entrada.length() != 1 || !Character.isLetter(entrada.charAt(0))) {
                System.out.println("Ingresa una sola letra.");
                continue;
            }

            char letra = entrada.charAt(0);
            if (!usadas.add(letra)) {
                System.out.println("Ya usaste esa letra.");
                continue;
            }

            if (palabra.indexOf(letra) >= 0) {
                System.out.println("¡Bien! La letra esta en la palabra.");
            } else {
                System.out.println("La letra no esta.");
                errores++;
            }
        }

        dibujar(errores);
        boolean gano = palabraCompleta(palabra, usadas);
        System.out.println("La palabra era: " + palabra);

        // Puntaje: letras distintas de la palabra / letras que usaste * 100
        // Si solo usaste letras correctas = 100. Mientras mas letras uses, menos puntos.
        // Si pierdes = 0.
        int letrasPalabra = contarLetrasDistintas(palabra);
        int letrasUsadas = usadas.size();
        int puntaje = gano ? letrasPalabra * 100 / letrasUsadas : 0;

        System.out.println("La palabra tenia " + letrasPalabra + " letras distintas y usaste " + letrasUsadas + ".");
        return pantallaFinal(gano, puntaje);
    }

    // ---------- Lógica del juego ----------

    private boolean palabraCompleta(String palabra, Set<Character> usadas) {
        for (char c : palabra.toCharArray()) {
            if (!usadas.contains(c)) {
                return false;
            }
        }
        return true;
    }

    private int contarLetrasDistintas(String palabra) {
        Set<Character> distintas = new TreeSet<>();
        for (char c : palabra.toCharArray()) {
            distintas.add(c);
        }
        return distintas.size();
    }

    private String mostrarPalabra(String palabra, Set<Character> usadas) {
        StringBuilder sb = new StringBuilder();
        for (char c : palabra.toCharArray()) {
            sb.append(usadas.contains(c) ? c : '_').append(' ');
        }
        return sb.toString();
    }

    private void dibujar(int errores) {
        String cabeza    = errores >= 1 ? "O" : " ";
        String torso     = errores >= 2 ? "|" : " ";
        String brazoIzq  = errores >= 3 ? "/" : " ";
        String brazoDer  = errores >= 4 ? "\\" : " ";
        String piernaIzq = errores >= 5 ? "/" : " ";
        String piernaDer = errores >= 6 ? "\\" : " ";

        System.out.println();
        System.out.println("  +---+");
        System.out.println("  |   |");
        System.out.println("  " + cabeza + "   |");
        System.out.println(" " + brazoIzq + torso + brazoDer + "  |");
        System.out.println(" " + piernaIzq + " " + piernaDer + "  |");
        System.out.println("      |");
        System.out.println("=========");
    }

    // ---------- Pantalla final ----------

    private Puntuacion pantallaFinal(boolean gano, int puntaje) {
        System.out.println("\n==============================");
        System.out.println(gano ? "        ¡¡ GANASTE !!" : "        PERDISTE :(");
        System.out.println("==============================");
        System.out.println("Puntaje: " + puntaje + " / 100");

        String jugador;
        do {
            System.out.print("Ingresa tu nombre: ");
            jugador = sc.nextLine().trim();
        } while (jugador.isEmpty());

        return new Puntuacion(jugador, NOMBRE_JUEGO, puntaje, gano);
    }
}
