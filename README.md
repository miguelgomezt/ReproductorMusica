# Reproductor de Música Lenguajes y Compiladores

Aplicación de escritorio en Java (Swing) que administra y reproduce una biblioteca de canciones, usando una estructura de datos distinta según el modo de reproducción seleccionado. Proyecto desarrollado para la asignatura Lenguajes y Compiladores — Universidad EIA.

El objetivo del proyecto no es construir un reproductor musical comercial, sino demostrar la correcta implementación y aplicación de las estructuras de datos vistas en el curso (lista circular doble, cola simple, árbol binario de búsqueda, y una estructura adicional de creatividad).

## Tabla de contenido
-Funcionalidades. 

-Modos de reproducción.

-Arquitectura.

-Cómo ejecutar.

-Equipo y responsabilidades.

-Estructuras de datos y complejidad.

-Requerimientos técnicos cumplidos.

## Funcionalidades
-Agregar, editar, eliminar y buscar canciones en la biblioteca.

-Calificar una canción entre 0 y 100.

-Visualizar la canción que se encuentra "reproduciendo" (simulado, sin audio real).

-Barra de progreso simulada con reproducción, pausa, siguiente y anterior (cuando aplica según el modo).

-Portada de álbum, con imagen genérica de respaldo si la canción no tiene una.

-Cuatro modos de reproducción, cada uno respaldado por una estructura de datos distinta implementada desde cero.

## Modos de reproducción
-Modo	Estructura de datos	Comportamiento

-Aleatorio	Lista Circular Doblemente Ligada	Navegación infinita en ambas direcciones; al llegar al final vuelve al inicio.

-Orden de llegada	Cola Simple (FIFO)	Solo avanza; cada canción sale de la cola tras reproducirse. No admite retroceder.

-Alfabético	Árbol Binario de Búsqueda	Recorrido inorden por título de la canción; avanza y retrocede sin perder canciones.

-Por calificación (bonus / creatividad)	Priority Queue (Max-Heap binario)	Navega de mayor a menor calificación (0–100); avanza y retrocede sin perder canciones.

Cada modo implementa la interfaz común structures.PlaybackMode, lo que permite que la interfaz gráfica cambie de estructura sin conocer los detalles internos de cada una (polimorfismo).


## Estructuras de datos y complejidad
-Estructura	Insertar	Eliminar	Buscar	Avanzar/Retroceder

-Lista Circular Doble	O(1)	O(n)	O(n)	O(1)

-Cola Simple	O(1)	O(1)	O(n)	O(1) (solo avanza)

-Árbol Binario de Búsqueda	O(h)*	O(h)*	O(h)*	O(1) (vía caché de recorrido inorden)

-Priority Queue (Max-Heap)	O(log n)	O(n)	O(n)	O(1) (vía caché ordenada)

-* h = altura del árbol. O(log n) en promedio, O(n) en el peor caso (árbol degenerado, p. ej. al insertar canciones ya ordenadas alfabéticamente).
dades.

## Requerimientos técnicos cumplidos
-Encapsulamiento (atributos privados, acceso vía getters/setters)
 
-Polimorfismo (PlaybackMode con 4 implementaciones intercambiables)
 
-Interfaces (PlaybackMode, MusicLibraryManager.LibraryChangeListener)
 
-Separación entre lógica (structures, app) y presentación (ui)
 
-Estructuras de datos implementadas desde cero (sin java.util.LinkedList, java.util.Queue ni java.util.PriorityQueue)
 
-Interfaz gráfica con Swing: biblioteca, reproductor, selector de modo
 
-Genéricos (no aplica en la versión final: PlaybackMode trabaja directamente con Song por decisión de diseño del equipo)
 
-Persistencia, filtros, favoritos, atajos de teclado (funcionalidades de bonificación, opcionales)
