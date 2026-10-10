# ConsolaJuegos — Documentación

Consola de juegos en Java para la terminal (sin interfaz gráfica). Desde un menú principal se elige un juego, se juega una partida y al terminar se registra el puntaje del jugador. Cada juego guarda un **top 3** de mejores puntajes en un archivo de texto (`puntajes.txt`), que se puede consultar desde el mismo menú.

---

## Índice

1. [Requisitos y ejecución](#1-requisitos-y-ejecución)
2. [Estructura del proyecto](#2-estructura-del-proyecto)
3. [Cómo funciona el programa (flujo general)](#3-cómo-funciona-el-programa-flujo-general)
4. [Interfaces](#4-interfaces)
5. [Paquete `datos`](#5-paquete-datos)
6. [Paquete `consolajuegos`](#6-paquete-consolajuegos)
7. [Paquete `games`: los juegos](#7-paquete-games-los-juegos)
8. [El archivo `puntajes.txt`](#8-el-archivo-puntajestxt)
9. [Algoritmo del top 3 paso a paso](#9-algoritmo-del-top-3-paso-a-paso)
10. [Cómo agregar un juego nuevo](#10-cómo-agregar-un-juego-nuevo)
11. [Cómo usar un juego en otra consola](#11-cómo-usar-un-juego-en-otra-consola)
12. [Validaciones y manejo de errores](#12-validaciones-y-manejo-de-errores)
13. [Limitaciones conocidas](#13-limitaciones-conocidas)
14. [Historial de cambios](#14-historial-de-cambios)

---

## 1. Requisitos y ejecución

### Requisitos

- **Java 14 o superior.** El código usa `switch` con flechas (`case 1 -> ...`), que no existe en versiones anteriores.
- NetBeans (o cualquier IDE de Java).

> En NetBeans: clic derecho al proyecto → **Properties → Sources → Source/Binary Format** debe ser 14 o mayor.

### Ejecutar desde NetBeans

1. Abrir el proyecto.
2. **Clean and Build** (ícono de escoba con martillo).
3. **Run** (F6). El programa arranca en `consolajuegos.ConsolaJuegos`.

### Ejecutar desde la terminal (CMD)

Después de hacer Clean and Build, desde la carpeta raíz del proyecto:

```
java -cp build/classes consolajuegos.ConsolaJuegos
```

Para que **Torre de Super Tierra** se vea con la pantalla fija y animada (ver sección 7.4):

```
java -Dpantalla=ansi -cp build/classes consolajuegos.ConsolaJuegos
```

---

## 2. Estructura del proyecto

```
ConsolaJuegos/
├── puntajes.txt                     ← se crea solo al guardar la primera partida
└── src/
    ├── consolajuegos/
    │   └── ConsolaJuegos.java       ← menú principal (main)
    ├── datos/
    │   ├── PuntuacionComparable.java← interfaz: comparar dos puntuaciones
    │   ├── Puntuacion.java          ← resultado de una partida
    │   └── GestorPuntajes.java      ← guarda, lee y muestra el top 3
    └── games/
        ├── Jugable.java             ← interfaz que implementa todo juego
        ├── Ahorcado.java
        ├── Buscaminas.java
        ├── RescateEspacial.java
        └── OperacionSuperTierra.java
```

### Responsabilidad de cada paquete

| Paquete | Qué hace | Qué **no** hace |
|---|---|---|
| `consolajuegos` | Muestra el menú, lanza el juego elegido y manda guardar su resultado. | No sabe cómo funciona ningún juego por dentro. |
| `games` | Cada clase es un juego completo: reglas, tablero, pantalla final y nombre del jugador. Devuelve una `Puntuacion`. | No escribe en el archivo de puntajes. |
| `datos` | Representa una puntuación, la compara, arma el top 3 y lee/escribe `puntajes.txt`. | No sabe nada de los juegos (solo sus nombres). |

Esta separación permite agregar o quitar juegos sin tocar la parte de datos, y cambiar la forma de guardar sin tocar los juegos.

### Diagrama de relaciones

```
                 ┌──────────────────┐
                 │  ConsolaJuegos   │
                 │  (menú, main)    │
                 └───┬──────────┬───┘
         usa start() │          │ guardar(...) / mostrarEstadisticas()
                     ▼          ▼
          ┌────────────────┐  ┌──────────────────┐
          │ «interface»    │  │  GestorPuntajes  │──── lee/escribe ──► puntajes.txt
          │ Jugable        │  └────────┬─────────┘
          │ start():       │           │ usa comparar()
          │   Puntuacion   │           ▼
          └───────▲────────┘  ┌──────────────────────┐
                  │           │ «interface»          │
     implements   │           │ PuntuacionComparable │
  ┌───────┬───────┼───────┐   │ comparar(otra): int  │
  │       │       │       │   └──────────▲───────────┘
Ahorcado Busca- Rescate  Operacion       │ implements
         minas  Espacial SuperTierra ┌───┴────────┐
  └───────┴───────┴───────┴─crean──► │ Puntuacion │
                                     └────────────┘
```

---

## 3. Cómo funciona el programa (flujo general)

```
Inicio
  │
  ▼
MENÚ PRINCIPAL ◄─────────────────────────────────────────┐
  1. Lanzar juego                                         │
  2. Ver estadisticas                                     │
  3. Salir                                                │
  │                                                       │
  ├─ 1 → Lista de juegos (1..4, 0 = volver)               │
  │        │                                              │
  │        ▼                                              │
  │      juego.start()                                    │
  │        • se juega la partida                          │
  │        • pantalla GANASTE / PERDISTE + puntaje        │
  │        • pide el nombre del jugador                   │
  │        • devuelve un objeto Puntuacion                │
  │        │                                              │
  │        ▼                                              │
  │      GestorPuntajes.guardar(puntuacion)               │
  │        • intenta meterla en el top 3 de ese juego     │
  │        • devuelve el lugar: 1, 2, 3 ó 0 (no entró)    │
  │        │                                              │
  │        ▼                                              │
  │      Mensaje: NUEVO RECORD / Entraste al top 3 /      │
  │               No entraste al top 3  ──────────────────┤
  │                                                       │
  ├─ 2 → GestorPuntajes.mostrarEstadisticas() ────────────┤
  │        (top 3 de cada juego, "-" en lugares vacíos)   │
  │                                                       │
  └─ 3 → "¡Hasta luego!" → Fin                            │
```

### Ejemplo de una sesión

```
===== CONSOLA DE JUEGOS =====
1. Lanzar juego
2. Ver estadisticas
3. Salir
Elige una opcion: 1

--- Elige un juego ---
1. Ahorcado
2. Buscaminas
3. Rescate Espacial
4. Torre de Super Tierra
0. Volver
Opcion: 1

===== AHORCADO =====
...
==============================
        ¡¡ GANASTE !!
==============================
Puntaje: 75 / 100
Ingresa tu nombre: Maria
*** ¡NUEVO RECORD! Quedaste en 1er lugar. ***
¡Gracias por jugar, Maria!
```

---

## 4. Interfaces

El proyecto usa dos interfaces. Una interfaz es un "contrato": dice **qué métodos** debe tener una clase, pero no **cómo** funcionan.

### 4.1 `Jugable` (paquete `games`)

```java
public interface Jugable {

    // Juega una partida completa y devuelve la puntuacion obtenida
    Puntuacion start();
}
```

Todo juego de la consola implementa `Jugable`. Gracias a eso, el menú guarda todos los juegos en un solo arreglo `Jugable[]` y los lanza igual, sin importar cuál sea:

```java
Puntuacion resultado = juegos[eleccion - 1].start();
```

**Contrato de `start()`:**

1. Juega una partida completa.
2. Muestra la pantalla final (GANASTE o PERDISTE) y el puntaje.
3. Pide el nombre del jugador (no acepta un nombre vacío).
4. Devuelve un objeto `Puntuacion` con nombre, juego, puntos y si ganó.

`start()` **no guarda** nada: eso lo decide quien lo llama (la consola).

### 4.2 `PuntuacionComparable` (paquete `datos`)

```java
public interface PuntuacionComparable {

    // Devuelve un numero positivo si esta puntuacion le gana a la otra,
    // negativo si pierde y 0 si empatan
    int comparar(PuntuacionComparable otra);
}
```

| `a.comparar(b)` devuelve | Significa |
|---|---|
| número **positivo** | `a` le gana a `b` |
| **0** | empatan |
| número **negativo** | `a` pierde contra `b` |

`GestorPuntajes` usa este método para decidir el orden del top 3 (ver sección 9).

---

## 5. Paquete `datos`

### 5.1 `Puntuacion`

Representa el resultado de **una** partida. Implementa `PuntuacionComparable`.

**Atributos** (todos `private final`, no cambian después de crear el objeto):

| Atributo | Tipo | Descripción |
|---|---|---|
| `nombre` | `String` | Nombre del jugador. |
| `juego` | `String` | Identificador del juego (`"Ahorcado"`, `"Buscaminas"`, ...). |
| `puntos` | `int` | Puntaje obtenido. **Más alto = mejor** en todos los juegos. |
| `gano` | `boolean` | `true` si ganó la partida. |

**Constructor:**

```java
new Puntuacion(String nombre, String juego, int puntos, boolean gano)
```

El constructor reemplaza cualquier `;` del nombre por `,`, porque el `;` es el separador del archivo y lo rompería (ver sección 8).

**Métodos:**

| Método | Qué hace |
|---|---|
| `getNombre()`, `getJuego()`, `getPuntos()`, `isGano()` | Getters. |
| `comparar(PuntuacionComparable otra)` | Devuelve `this.puntos - otra.puntos`. Positivo si esta tiene más puntos. |
| `toLinea()` | Convierte el objeto en una línea de texto: `juego;nombre;puntos;gano`. |
| `desdeLinea(String linea)` *(static)* | Hace lo contrario: crea una `Puntuacion` a partir de una línea del archivo. |

Ejemplo:

```java
Puntuacion a = new Puntuacion("Ana", "Ahorcado", 80, true);
Puntuacion b = new Puntuacion("Luis", "Ahorcado", 60, true);

a.comparar(b);   // 20  → a le gana a b
b.comparar(a);   // -20 → b pierde contra a
a.toLinea();     // "Ahorcado;Ana;80;true"
```

### 5.2 `GestorPuntajes`

Clase con métodos `static` que se encarga de todo lo relacionado con el archivo y el top 3.

**Constantes:**

| Constante | Valor | Uso |
|---|---|---|
| `ARCHIVO` | `"puntajes.txt"` | Nombre del archivo de puntajes. |
| `TOP` | `3` | Cuántos puntajes se guardan por juego. |
| `JUEGOS` | `{"Ahorcado", "Buscaminas", "RescateEspacial", "SuperTierra"}` | Juegos que **siempre** salen en las estadísticas, aunque nadie los haya jugado. Deben coincidir con el `NOMBRE_JUEGO` de cada juego. |

**Métodos públicos:**

| Método | Qué hace |
|---|---|
| `int guardar(Puntuacion nueva)` | Intenta meter la puntuación en el top 3 de su juego. Reescribe el archivo y devuelve el lugar obtenido (1, 2, 3) o **0** si no entró. |
| `void mostrarEstadisticas()` | Imprime el top 3 de cada juego. Los lugares vacíos se muestran con `-`. |

**Métodos privados:**

| Método | Qué hace |
|---|---|
| `Puntuacion[] topDe(List<Puntuacion> todas, String juego)` | Arma un arreglo de 3 posiciones con las mejores puntuaciones de un juego. Las posiciones sin puntuación quedan en `null`. |
| `List<Puntuacion> leerTodas()` | Lee todas las líneas del archivo. Ignora líneas vacías o dañadas. Si el archivo no existe devuelve una lista vacía. |
| `void escribirTodas(List<Puntuacion> lista)` | Sobrescribe el archivo completo con la lista recibida. |

**Ejemplo de estadísticas:**

```
===== TOP 3 POR JUEGO =====

--- Ahorcado ---
#   JUGADOR         PUNTOS  RESULTADO
1   Maria           100     Gano
2   Luis            75      Gano
3   Ana             0       Perdio

--- Buscaminas ---
#   JUGADOR         PUNTOS  RESULTADO
1   Carlos          10      Gano
2   -               -       -
3   -               -       -
```

Si en el archivo aparece un juego que no está en `JUEGOS` (por ejemplo, uno que se agregó después), también se muestra, al final de la lista.

---

## 6. Paquete `consolajuegos`

### `ConsolaJuegos`

Contiene el `main`. Tiene un único `Scanner` (`sc`) que se comparte con todos los juegos. Esto es importante: si cada juego creara su propio `Scanner` sobre `System.in`, uno podría "robarle" la entrada al otro.

**En `main`:**

- `juegos`: arreglo `Jugable[]` con una instancia de cada juego.
- `nombres`: arreglo `String[]` con el nombre que se muestra en el menú, **en el mismo orden** que `juegos`. Se necesita porque `Jugable` solo tiene `start()`, no un método para pedir el nombre.
- Un ciclo `do-while` que muestra el menú hasta que se elige la opción 3.

**Métodos:**

| Método | Qué hace |
|---|---|
| `elegirJuego(Jugable[] juegos, String[] nombres)` | Muestra la lista de juegos, lee la opción, lanza el juego y pasa el resultado a `guardarResultado`. Con 0 vuelve al menú. |
| `guardarResultado(Puntuacion resultado)` | Llama a `GestorPuntajes.guardar` y muestra el mensaje según el lugar: 1 → "NUEVO RECORD", 2 ó 3 → "Entraste al top 3", 0 → "No entraste al top 3". |
| `leerNumero(String mensaje)` | Pide un número hasta que el usuario escriba uno válido. |

**¿Por qué todo se lee con `nextLine()`?** Si se mezcla `nextInt()` con `nextLine()`, `nextInt()` deja un salto de línea pendiente y el siguiente `nextLine()` lo lee como una línea vacía (por ejemplo, se saltaría la pregunta del nombre). Por eso todo el programa lee líneas completas y las convierte con `Integer.parseInt`.

---

## 7. Paquete `games`: los juegos

Todos los juegos siguen la misma estructura:

```java
public class MiJuego implements Jugable {

    private static final String NOMBRE_JUEGO = "MiJuego"; // identificador en puntajes.txt
    private final Scanner sc;

    public MiJuego() { this(new Scanner(System.in)); }  // para usarlo solo
    public MiJuego(Scanner sc) { this.sc = sc; }        // para compartir el Scanner del menú

    @Override
    public Puntuacion start() {
        // ... lógica del juego ...
        return pantallaFinal(gano, puntaje);
    }

    private Puntuacion pantallaFinal(boolean gano, int puntaje) {
        // muestra GANASTE / PERDISTE y el puntaje
        // pide el nombre (no acepta vacío)
        return new Puntuacion(jugador, NOMBRE_JUEGO, puntaje, gano);
    }
}
```

**Resumen de los juegos:**

| Juego | `NOMBRE_JUEGO` | Cómo se gana | Puntaje (más alto = mejor) |
|---|---|---|---|
| Ahorcado | `Ahorcado` | Adivinar la palabra antes de 6 errores. | 0 a 100 según cuántas letras usaste. |
| Buscaminas | `Buscaminas` | Descubrir todas las casillas sin bomba. | 10 si ganas; si pierdes, bombas bien marcadas menos mal marcadas. |
| Rescate Espacial | `RescateEspacial` | Llegar a la cápsula sin chocar. | 100 menos los movimientos usados. |
| Torre de Super Tierra | `SuperTierra` | Completar las 3 torres. | Suma de puntos por pisos, combos, ritmo y torres. |

### 7.1 Ahorcado

**Reglas:** la computadora elige una palabra al azar. El jugador escribe una letra por turno. Si la letra está en la palabra se muestra; si no, se dibuja una parte del muñeco. Con **6 errores** se pierde.

**Palabras:** `PROGRAMA, COMPUTADORA, TECLADO, PANTALLA, VARIABLE, JAVA, CONSOLA, ALGORITMO, MEMORIA, INTERNET`.

**Puntaje:** mientras **menos letras** uses para adivinar la palabra, mejor.

```
puntaje = (letras distintas de la palabra × 100) / (letras que usaste)
```

- Solo letras correctas → **100** (puntaje perfecto).
- Si pierdes → **0**.
- Se cuentan las letras **distintas**: en `JAVA` son 3 (J, A, V), no 4.

| Palabra | Letras distintas | Letras usadas | Puntaje |
|---|---|---|---|
| JAVA | 3 | 3 | 100 |
| JAVA | 3 | 6 | 50 |
| MEMORIA | 6 | 8 | 75 |
| ALGORITMO | 8 | 10 | 80 |

Así es justo para todas las palabras: una palabra larga no da más puntos solo por ser larga.

**Métodos principales:**

| Método | Qué hace |
|---|---|
| `palabraCompleta(palabra, usadas)` | `true` si ya se adivinaron todas las letras. |
| `contarLetrasDistintas(palabra)` | Cuenta las letras sin repetir (usa un `Set`). |
| `mostrarPalabra(palabra, usadas)` | Devuelve la palabra con `_` en las letras no adivinadas, por ejemplo `J A _ A`. |
| `dibujar(errores)` | Dibuja el muñeco según los errores. |

**Validaciones:** solo acepta una letra; avisa si la letra ya se usó (no cuenta como error).

### 7.2 Buscaminas

**Reglas:** tablero de **8×8** con **10 bombas**. Cada casilla descubierta muestra cuántas bombas tiene alrededor. Si descubres una bomba, pierdes. Si descubres todas las casillas seguras, ganas.

**Comandos:**

| Comando | Acción | Ejemplo |
|---|---|---|
| `d fila columna` | Descubrir una casilla. | `d 3 5` |
| `m fila columna` | Marcar o desmarcar una bomba (bandera). | `m 1 1` |

Filas y columnas van del 1 al 8.

**Símbolos del tablero:**

| Símbolo | Significado |
|---|---|
| `#` | Casilla sin descubrir. |
| `F` | Casilla marcada como bomba. |
| `.` | Casilla descubierta sin bombas alrededor. |
| `1`–`8` | Cantidad de bombas alrededor. |
| `*` | Bomba (solo se ve al terminar). |

**Detalles de la lógica:**

- **La primera casilla nunca es bomba:** las bombas se colocan después del primer `d`, evitando esa casilla (`colocarMinas`).
- **Apertura automática:** si descubres una casilla con 0 bombas alrededor, se descubren solas sus vecinas. Esto se hace con **recursión** en `descubrir(f, c)`.
- `calcularVecinas()` precalcula, para cada casilla, cuántas bombas tiene en las 8 casillas de alrededor.

**Puntaje ("bombas desactivadas"):**

- Si ganas → **10** (todas las bombas).
- Si pierdes → **marcas correctas − marcas equivocadas** (mínimo 0).

Las marcas equivocadas restan para que no sirva marcar todo el tablero sin pensar (ver historial de cambios).

### 7.3 Rescate Espacial

*(Juego aportado por un compañero y adaptado a la consola.)*

**Reglas:** el jugador es la nave `[X]`, que empieza en la esquina superior izquierda de un mapa de 8×8. Debe llegar a la cápsula `[O]` sin chocar con los asteroides `*`.

**Controles:** `W` arriba, `S` abajo, `A` izquierda, `D` derecha, `Q` huir (abandonar).

**Detalles de la lógica:**

- Los asteroides son **fijos**: hay asteroide en `(x, y)` si `(x * y + 3) % 7 == 0` (método `esAsteroide`).
- La cápsula aparece en una posición al azar (fila y columna entre 2 y 7) y **nunca encima de un asteroide**.
- Un movimiento contra el borde no cuenta: se muestra "Movimiento bloqueado o no valido".

**Puntaje:** `100 − movimientos` si llegas a la cápsula (mínimo 1). Si chocas o huyes, **0**.

### 7.4 Torre de Super Tierra (`OperacionSuperTierra`)

*(Juego aportado por un compañero y adaptado a la consola.)*

**Reglas:** juego de ritmo para apilar bloques. Un contenedor `#` va y viene por la parte de arriba al compás de un metrónomo (`TUM`, `tak`). Al presionar **ENTER** cae sobre la torre `=`. Lo que sobresale de la torre se recorta, así que la torre se va haciendo más angosta. Si el bloque no toca la torre, pierdes.

**Niveles (3 torres):**

| Torre | Nombre | Pisos | Ancho base | BPM | Acelera | Cambios bruscos |
|---|---|---|---|---|---|---|
| 1 | Entrenamiento | 5 | 9 | 80 | No | 0 % |
| 2 | Zona de combate | 8 | 7 | 110 | Sí (+10 cada 3 pisos) | 10 % |
| 3 | Frente de Super Tierra | 11 | 5 | 140 | Sí (+10 cada 3 pisos) | 22 % |

**Puntaje:**

| Acción | Puntos |
|---|---|
| Bloque perfecto (exactamente alineado) | 100 + combo × 20 |
| Bloque parcial | ancho que quedó × 10 (y el combo vuelve a 0) |
| Soltar justo en el tiempo `TUM` | +50 |
| Completar una torre | 200 × número de torre |

Se gana al completar las 3 torres. Al final se muestran estadísticas extra (pisos, perfectos, mejor combo, BPM final), pero solo se guarda el puntaje total.

**Clases internas** (privadas, dentro del mismo archivo, para que el juego sea un solo archivo):

| Clase | Qué representa |
|---|---|
| `Despliegue` | La configuración de un nivel (pisos, ancho, BPM, etc.). |
| `Baluarte` | La torre. Su método `soltar(izquierda)` decide si el bloque cayó perfecto, parcial o falló. |
| `Contenedor` | Un bloque ya colocado (posición izquierda y ancho). |
| `Descarga` | El resultado de soltar: en qué posición y si fue al ritmo. |

**Modos de pantalla:**

- **Modo compacto** (por defecto, y siempre en la consola de NetBeans): cada movimiento del bloque se imprime en una línea nueva. Se juega mirando la **última línea**.
- **Modo ANSI** (pantalla fija con animación de caída): se activa ejecutando desde una terminal con `-Dpantalla=ansi` (ver sección 1).

**Cómo detecta el ENTER sin detener el juego:** usa `System.in.available()`, que dice si hay teclas esperando sin bloquear el programa. Así el bloque sigue moviéndose mientras espera. Antes de cada bloque se vacía la entrada (`vaciarEntrada`) para que un ENTER de más no suelte el siguiente bloque sin querer.

---

## 8. El archivo `puntajes.txt`

- **Ubicación:** carpeta raíz del proyecto (al mismo nivel que `src`), no dentro de los paquetes.
- **Se crea solo** la primera vez que se guarda un puntaje.
- **Formato:** una línea por puntuación, con los campos separados por `;`:

```
juego;nombre;puntos;gano
```

Ejemplo de contenido:

```
Ahorcado;Maria;100;true
Ahorcado;Luis;75;true
Ahorcado;Ana;0;false
Buscaminas;Carlos;10;true
RescateEspacial;Sofia;88;true
SuperTierra;Pedro;900;false
```

- Cada juego tiene **como máximo 3 líneas** (su top 3).
- Cada vez que se guarda, el archivo se **reescribe completo**: se conservan las líneas de los otros juegos y se reemplazan las del juego que se acaba de jugar.
- Se puede borrar para reiniciar todos los puntajes; se vuelve a crear solo.
- Si se edita a mano y alguna línea queda mal, esa línea se ignora (no cierra el programa).

---

## 9. Algoritmo del top 3 paso a paso

El top 3 de un juego es un arreglo de 3 posiciones (`Puntuacion[]`). La posición 0 es el 1er lugar. Los lugares vacíos son `null`.

Cuando llega una puntuación nueva (`GestorPuntajes.guardar`):

1. **Intentar entrar en el último lugar (posición 2).**
   - Si ese lugar está vacío, entra.
   - Si está ocupado, se compara: `nueva.comparar(top[2])`. Si da positivo (le gana), la reemplaza. Si no, **no entra** y se devuelve 0.
2. **Subir intercambiando.** Mientras no esté en el 1er lugar, se compara con la de arriba:
   - Si arriba está vacío, o `top[pos].comparar(top[pos - 1]) > 0` (le gana), **se intercambian** y sube un lugar.
   - Si no le gana, se queda donde está.
3. **Guardar** el archivo con el top nuevo.
4. **Devolver** el lugar final (`pos + 1`).

```java
int pos = TOP - 1;
if (top[pos] != null && nueva.comparar(top[pos]) <= 0) {
    return 0; // no le gano al tercero: no entra
}
top[pos] = nueva;

while (pos > 0 && (top[pos - 1] == null || top[pos].comparar(top[pos - 1]) > 0)) {
    Puntuacion aux = top[pos - 1];   // intercambio
    top[pos - 1] = top[pos];
    top[pos] = aux;
    pos--;
}
```

### Ejemplo

Top actual del Ahorcado:

| Lugar | Jugador | Puntos |
|---|---|---|
| 1 | Maria | 100 |
| 2 | Luis | 60 |
| 3 | Ana | 40 |

Llega **Pedro con 75**:

1. Se compara con el 3er lugar: `75 − 40 = 35` (positivo, le gana). Pedro reemplaza a Ana, que sale del top.
   `[Maria 100, Luis 60, Pedro 75]`
2. Se compara con el 2do lugar: `75 − 60 = 15` (positivo). Se intercambian.
   `[Maria 100, Pedro 75, Luis 60]`
3. Se compara con el 1er lugar: `75 − 100 = −25` (negativo). Se detiene.
4. Resultado: Pedro queda en el **lugar 2** → "¡Entraste al top 3! Quedaste en el lugar 2."

Si después llega **Rosa con 60**: se compara con Luis (60): `60 − 60 = 0`, no le gana → **no entra**. En un empate se queda el que llegó primero.

**Para mostrar las estadísticas** (`topDe`) se usa la misma idea: se recorren todas las líneas del archivo de ese juego y cada una "entra abajo y sube intercambiando". Así, aunque el archivo tuviera más de 3 líneas de un juego (por ejemplo, de una versión vieja), siempre se muestran las 3 mejores.

---

## 10. Cómo agregar un juego nuevo

1. **Crear la clase** en el paquete `games`, implementando `Jugable` y siguiendo la estructura de la sección 7. Lo importante:
   - Una constante `NOMBRE_JUEGO` única y sin espacios ni `;`.
   - Constructor que reciba un `Scanner`.
   - `start()` debe devolver `new Puntuacion(jugador, NOMBRE_JUEGO, puntaje, gano)`.
   - El puntaje debe ser **más alto = mejor**. Si en tu juego "menos es mejor" (como movimientos o tiempo), conviértelo, por ejemplo `100 - movimientos`.
2. **Agregarlo al menú** en `ConsolaJuegos.java`, en los dos arreglos y en el mismo lugar:

```java
import games.MiJuego;
...
Jugable[] juegos = {
    new Ahorcado(sc),
    new Buscaminas(sc),
    new RescateEspacial(sc),
    new OperacionSuperTierra(sc),
    new MiJuego(sc)
};
String[] nombres = {
    "Ahorcado",
    "Buscaminas",
    "Rescate Espacial",
    "Torre de Super Tierra",
    "Mi Juego"
};
```

3. **(Opcional)** Agregar su `NOMBRE_JUEGO` al arreglo `JUEGOS` de `GestorPuntajes` para que aparezca en las estadísticas con `-` aunque nadie lo haya jugado.

---

## 11. Cómo usar un juego en otra consola

Para llevar un juego a otro proyecto, hay que copiar:

- El archivo del juego (por ejemplo `Ahorcado.java`).
- `Jugable.java`.
- `Puntuacion.java` y `PuntuacionComparable.java`, porque `start()` devuelve un `Puntuacion`.

Si en el otro proyecto los paquetes se llaman distinto, hay que cambiar la línea `package` y los `import` de esos archivos.

Uso mínimo:

```java
Scanner sc = new Scanner(System.in);
Jugable juego = new Ahorcado(sc);
Puntuacion resultado = juego.start();

System.out.println(resultado.getNombre() + " hizo " + resultado.getPuntos() + " puntos");
```

Guardar el resultado (en un archivo o en otro lado) queda a cargo de esa otra consola.

---

## 12. Validaciones y manejo de errores

| Situación | Qué pasa |
|---|---|
| Se escribe texto donde se pide un número en el menú | "Ingresa un numero valido." y se vuelve a pedir. |
| Opción de menú inexistente | "Opcion no valida." / "Juego no valido." |
| Nombre vacío al final de una partida | Se vuelve a pedir hasta que se escriba algo. |
| Nombre con `;` | El `;` se cambia por `,` para no romper el archivo. |
| Ahorcado: más de una letra, un número o un símbolo | "Ingresa una sola letra." (no cuenta como error). |
| Ahorcado: letra repetida | "Ya usaste esa letra." (no cuenta como error). |
| Buscaminas: comando mal escrito o fuera del tablero | Mensaje de error y se vuelve a pedir. |
| Buscaminas: descubrir una casilla marcada o ya descubierta | Mensaje de aviso, no pasa nada. |
| Rescate Espacial: tecla que no es W/A/S/D/Q o choque con el borde | "Movimiento bloqueado o no valido." |
| `puntajes.txt` no existe | Se toma como "sin puntajes" y se crea al guardar. |
| Línea dañada en `puntajes.txt` | Se ignora. |
| Error al leer o escribir el archivo | Se muestra el mensaje del error y el programa sigue. |

---

## 13. Limitaciones conocidas

- **Torre de Super Tierra en NetBeans:** la consola de NetBeans no puede redibujar la pantalla, así que el juego usa el modo compacto (una línea por movimiento). Se ve mejor desde una terminal con `-Dpantalla=ansi`. Además, hay que hacer clic en la ventana de Output antes de jugar para que los ENTER lleguen al juego.
- **Empates:** con el mismo puntaje se queda arriba el que llegó primero; un empate con el 3er lugar no entra al top.
- **Puntajes de distintos juegos no se comparan entre sí:** cada juego tiene su propia escala (Ahorcado 0–100, Buscaminas 0–10, etc.).
- **Buscaminas:** todas las victorias valen 10, así que entre ganadores hay empate.
- **Nombres de juegos en el archivo:** si se cambia el `NOMBRE_JUEGO` de un juego, sus puntajes viejos quedan con el nombre anterior.
- **Acentos:** las palabras del Ahorcado no llevan tilde; si se escribe una letra con tilde (`Á`) cuenta como letra equivocada. En algunas consolas de Windows los signos `¡` y `ñ` pueden verse como `?`.

---

## 14. Historial de cambios

1. **Menú principal** con las opciones Lanzar juego, Ver estadísticas y Salir.
2. **Primeros juegos:** Ahorcado y Buscaminas, con pantalla de ganador/perdedor y guardado de nombre y puntaje en `puntajes.txt`.
3. **Solo el mejor puntaje por juego:** si se superaba el récord, se reemplazaba el anterior.
4. **Interfaz `Jugable`** con el método `start()`, para que los juegos se puedan usar en otras consolas.
5. **Un archivo por juego:** se eliminó la clase base `Juego`; cada juego implementa `Jugable` directamente.
6. **Rescate Espacial** (de un compañero): adaptado a `Jugable`. Se corrigió que pidiera el nombre sin guardarlo, que tuviera un `start()` vacío que lanzaba error y que la cápsula pudiera aparecer sobre un asteroide.
7. **Torre de Super Tierra** (de un compañero): venía en 4 archivos y dependía de clases que no estaban incluidas. Se juntó en un solo archivo con clases internas y se reemplazaron las clases faltantes. Se cambió el modo compacto para que el bloque se vea moverse en la consola de NetBeans.
8. **Top 3 por juego** en lugar de solo el récord; las estadísticas rellenan con `-` los lugares vacíos.
9. **`PuntuacionComparable` y `Puntuacion`:** cada `start()` devuelve una `Puntuacion` y la consola la guarda; el top 3 se arma comparando con `comparar()` e intercambiando. Se eliminaron `Juego.java` y `Puntaje.java`.
10. **Nuevo puntaje del Ahorcado:** de 0 a 100 según las letras usadas respecto a la palabra (antes se premiaba usar más letras).
11. **Correcciones de lógica (revisión final):**
    - **Buscaminas:** antes se podían marcar todas las casillas y, aunque explotaras, sacar casi 10 puntos. Ahora las marcas equivocadas restan.
    - **Nombres con `;`:** antes rompían el archivo y el programa se cerraba al ver las estadísticas. Ahora el `;` se cambia por `,`.
    - **Líneas dañadas en `puntajes.txt`:** antes cerraban el programa. Ahora se ignoran.
