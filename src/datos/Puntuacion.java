package datos;

public class Puntuacion implements PuntuacionComparable {

    private final String nombre;
    private final String juego;
    private final int puntos;
    private final boolean gano;

    public Puntuacion(String nombre, String juego, int puntos, boolean gano) {
        // El ; separa los campos en el archivo, asi que no puede ir en el nombre
        this.nombre = nombre.replace(";", ",");
        this.juego = juego;
        this.puntos = puntos;
        this.gano = gano;
    }

    public String getNombre() { return nombre; }
    public String getJuego() { return juego; }
    public int getPuntos() { return puntos; }
    public boolean isGano() { return gano; }

    @Override
    public int comparar(PuntuacionComparable otra) {
        Puntuacion o = (Puntuacion) otra;
        return this.puntos - o.puntos;   // positivo = esta le gana a la otra
    }

    // Convierte la puntuacion en una linea de texto para el archivo
    public String toLinea() {
        return juego + ";" + nombre + ";" + puntos + ";" + gano;
    }

    // Reconstruye una puntuacion a partir de una linea del archivo
    public static Puntuacion desdeLinea(String linea) {
        String[] p = linea.split(";");
        return new Puntuacion(p[1], p[0], Integer.parseInt(p[2]), Boolean.parseBoolean(p[3]));
    }
}