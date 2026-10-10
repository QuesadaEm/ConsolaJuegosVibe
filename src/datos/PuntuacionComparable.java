package datos;

public interface PuntuacionComparable {

    // Devuelve un numero positivo si esta puntuacion le gana a la otra,
    // negativo si pierde y 0 si empatan
    int comparar(PuntuacionComparable otra);
}
