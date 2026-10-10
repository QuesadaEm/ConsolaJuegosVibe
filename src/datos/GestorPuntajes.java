package datos;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorPuntajes {

    private static final String ARCHIVO = "puntajes.txt";
    private static final int TOP = 3;

    // Juegos que siempre aparecen en las estadisticas, aunque nadie los haya jugado.
    // Deben coincidir con el NOMBRE_JUEGO de cada juego.
    private static final String[] JUEGOS = {
        "Ahorcado", "Buscaminas", "RescateEspacial", "SuperTierra"
    };

    // Inserta la puntuacion en el top 3 de su juego.
    // Devuelve el lugar que obtuvo (1, 2 o 3), o 0 si no entro.
    public static int guardar(Puntuacion nueva) {
        List<Puntuacion> todas = leerTodas();
        Puntuacion[] top = topDe(todas, nueva.getJuego());

        // 1. Entra en el ultimo lugar si esta vacio o si le gana al que esta ahi
        int pos = TOP - 1;
        if (top[pos] != null && nueva.comparar(top[pos]) <= 0) {
            return 0; // no le gano al tercero: no entra
        }
        top[pos] = nueva;

        // 2. Sube mientras le gane al de arriba (o arriba este vacio): se intercambian
        while (pos > 0 && (top[pos - 1] == null || top[pos].comparar(top[pos - 1]) > 0)) {
            Puntuacion aux = top[pos - 1];
            top[pos - 1] = top[pos];
            top[pos] = aux;
            pos--;
        }

        // 3. Reescribir el archivo: otros juegos igual + el top nuevo de este juego
        List<Puntuacion> resultado = new ArrayList<>();
        for (Puntuacion p : todas) {
            if (!p.getJuego().equals(nueva.getJuego())) {
                resultado.add(p);
            }
        }
        for (Puntuacion p : top) {
            if (p != null) {
                resultado.add(p);
            }
        }
        escribirTodas(resultado);

        return pos + 1;
    }

    // Arma el top 3 de un juego a partir de lo que hay en el archivo.
    // Los lugares sin puntuacion quedan en null.
    private static Puntuacion[] topDe(List<Puntuacion> todas, String juego) {
        Puntuacion[] top = new Puntuacion[TOP];
        for (Puntuacion p : todas) {
            if (!p.getJuego().equals(juego)) continue;

            // misma idea: entra abajo y sube intercambiando mientras gane
            int pos = TOP - 1;
            if (top[pos] != null && p.comparar(top[pos]) <= 0) continue;
            top[pos] = p;
            while (pos > 0 && (top[pos - 1] == null || top[pos].comparar(top[pos - 1]) > 0)) {
                Puntuacion aux = top[pos - 1];
                top[pos - 1] = top[pos];
                top[pos] = aux;
                pos--;
            }
        }
        return top;
    }

    private static List<Puntuacion> leerTodas() {
        List<Puntuacion> lista = new ArrayList<>();
        File f = new File(ARCHIVO);
        if (!f.exists()) {
            return lista;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                try {
                    lista.add(Puntuacion.desdeLinea(linea));
                } catch (RuntimeException e) {
                    // linea danada o editada a mano: se ignora para que el programa no se caiga
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer los puntajes: " + e.getMessage());
        }
        return lista;
    }

    private static void escribirTodas(List<Puntuacion> lista) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) {
            for (Puntuacion p : lista) {
                pw.println(p.toLinea());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar el puntaje: " + e.getMessage());
        }
    }

    public static void mostrarEstadisticas() {
        List<Puntuacion> todas = leerTodas();

        // Juegos fijos + cualquier otro juego que aparezca en el archivo
        List<String> juegos = new ArrayList<>(List.of(JUEGOS));
        for (Puntuacion p : todas) {
            if (!juegos.contains(p.getJuego())) {
                juegos.add(p.getJuego());
            }
        }

        System.out.println("\n===== TOP " + TOP + " POR JUEGO =====");
        for (String juego : juegos) {
            Puntuacion[] top = topDe(todas, juego);

            System.out.println("\n--- " + juego + " ---");
            System.out.printf("%-3s %-15s %-7s %s%n", "#", "JUGADOR", "PUNTOS", "RESULTADO");
            for (int i = 0; i < TOP; i++) {
                Puntuacion p = top[i];
                if (p != null) {
                    System.out.printf("%-3d %-15s %-7d %s%n",
                            i + 1, p.getNombre(), p.getPuntos(), p.isGano() ? "Gano" : "Perdio");
                } else {
                    System.out.printf("%-3d %-15s %-7s %s%n", i + 1, "-", "-", "-");
                }
            }
        }
    }
}