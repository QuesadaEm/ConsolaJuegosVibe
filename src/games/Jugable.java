package games;

import datos.Puntuacion;

public interface Jugable {

    // Juega una partida completa y devuelve la puntuacion obtenida
    Puntuacion start();
}
