package consolajuegos;

import datos.GestorPuntajes;
import datos.Puntuacion;
import games.Ahorcado;
import games.Buscaminas;
import games.Jugable;
import games.OperacionSuperTierra;
import games.RescateEspacial;
import java.util.Scanner;

public class ConsolaJuegos {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Jugable[] juegos = {
            new Ahorcado(sc),
            new Buscaminas(sc),
            new RescateEspacial(sc),
            new OperacionSuperTierra(sc)
        };
        String[] nombres = {
            "Ahorcado",
            "Buscaminas",
            "Rescate Espacial",
            "Torre de Super Tierra"
        };

        int opcion;
        do {
            System.out.println("\n===== CONSOLA DE JUEGOS =====");
            System.out.println("1. Lanzar juego");
            System.out.println("2. Ver estadisticas");
            System.out.println("3. Salir");
            opcion = leerNumero("Elige una opcion: ");

            switch (opcion) {
                case 1 -> elegirJuego(juegos, nombres);
                case 2 -> GestorPuntajes.mostrarEstadisticas();
                case 3 -> System.out.println("¡Hasta luego!");
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 3);
    }

    private static void elegirJuego(Jugable[] juegos, String[] nombres) {
        System.out.println("\n--- Elige un juego ---");
        for (int i = 0; i < juegos.length; i++) {
            System.out.println((i + 1) + ". " + nombres[i]);
        }
        System.out.println("0. Volver");

        int eleccion = leerNumero("Opcion: ");
        if (eleccion >= 1 && eleccion <= juegos.length) {
            Puntuacion resultado = juegos[eleccion - 1].start();
            guardarResultado(resultado);
        } else if (eleccion != 0) {
            System.out.println("Juego no valido.");
        }
    }

    private static void guardarResultado(Puntuacion resultado) {
        if (resultado == null) {
            return;
        }
        int lugar = GestorPuntajes.guardar(resultado);
        if (lugar == 1) {
            System.out.println("*** ¡NUEVO RECORD! Quedaste en 1er lugar. ***");
        } else if (lugar > 0) {
            System.out.println("¡Entraste al top 3! Quedaste en el lugar " + lugar + ".");
        } else {
            System.out.println("No entraste al top 3, no se guardo.");
        }
        System.out.println("¡Gracias por jugar, " + resultado.getNombre() + "!");
    }

    private static int leerNumero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero valido.");
            }
        }
    }
}
