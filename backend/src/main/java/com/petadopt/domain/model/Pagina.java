package com.petadopt.domain.model;

import java.util.List;

/** Una página de resultados y el total de elementos que cumplen el filtro. */
public record Pagina<T>(List<T> contenido, long total, int numero, int tamano) {
}
