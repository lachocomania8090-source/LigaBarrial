# Contexto del Proyecto: Sistema de Gestión de Liga Barrial
- **Stack:** Java 26, Spring Boot 3+, Vaadin Flow 25+, MongoDB (Spring Data).
- **Diseño:** Uso exclusivo del tema Lumo de Vaadin y clases utilitarias (`LumoUtility`). CERO CSS personalizado a menos que sea estrictamente necesario.
- **Arquitectura Backend:** Los modelos (`Equipo`, `Jugador`, `Torneo`, `Partido`, `EstadisticaEquipo`) y los servicios (`EquipoService`, etc.) ya existen y funcionan. Solo se requiere construir las vistas (`@Route`).

# Reglas Globales de UI (Estándar de Desarrollo)
1. **Inyección de Dependencias:** Usar constructores para inyectar los servicios en las vistas.
2. **CRUD Modal:** Todas las creaciones y ediciones deben realizarse mediante `Dialog`. NUNCA usar vistas (Rutas) separadas para formularios.
3. **Notificaciones:** Usar `Notification.show("Mensaje", 3000, Notification.Position.BOTTOM_CENTER)` para dar feedback al usuario tras acciones (Guardar, Eliminar, Error).
4. **Data Binding:** Usar `Binder<T>` para enlazar los modelos a los formularios con validación.
5. **Iconografía:** Usar exclusivamente `VaadinIcon`.

---

# Instrucciones por Vista (Prompt para Generación)

## 1. Vista de Equipos (`EquiposView.java`)
- **Ruta:** `@Route(value = "equipos", layout = MainLayout.class)`
- **Layout Base:** `VerticalLayout`. Debe tener `setSizeFull()`.
- **Header:** Un `HorizontalLayout` que contenga un `TextField` (para buscar por nombre) y un `Button` primario ("Nuevo Equipo" con icono de suma).
- **Contenido Central:** Un `Grid<Equipo>` que ocupe todo el espacio sobrante (`expand(grid)`). Columnas: Nombre, Ciudad, Entrenador.
- **Formulario (Dialog):** Al presionar "Nuevo Equipo" o al hacer doble clic en una fila del Grid, abrir un `Dialog`.
    - Campos: Nombre (TextField), Ciudad (TextField), Entrenador (TextField).
    - Botones: "Guardar" (Primario), "Cancelar" (Terciario).
- **Lógica de Refresco:** Tras guardar en el modal, llamar a `grid.setItems(equipoService.obtenerTodos())`.

## 2. Vista de Jugadores (`JugadoresView.java`)
- **Ruta:** `@Route(value = "jugadores", layout = MainLayout.class)`
- **Layout Base:** Igual que Equipos.
- **Contenido Central:** `Grid<Jugador>`. Columnas: Nombre, Edad, Posición, Número, y una columna personalizada que muestre el *Nombre del Equipo* (obtenido consultando el `equipoService` con el `equipoId` del jugador).
- **Formulario (Dialog):** - Campos: Nombre (TextField), Edad (IntegerField), Posición (ComboBox con: "Portero", "Defensa", "Mediocampista", "Delantero"), Número (IntegerField).
    - **Relación Clave:** Un `ComboBox<Equipo>` que muestre el nombre del equipo, pero que por detrás asigne el `equipoId` al modelo Jugador. Llenar este ComboBox llamando a `equipoService.obtenerTodos()`.

## 3. Vista de Partidos (`PartidosView.java`)
- **Ruta:** `@Route(value = "partidos", layout = MainLayout.class)`
- **Contenido Central:** `Grid<Partido>`.
    - Columnas personalizadas (Renderer):
        - Equipo Local (Nombre)
        - Goles Local
        - Goles Visitante
        - Equipo Visitante (Nombre)
        - Estado (Badge usando LumoUtility: Verde para "JUGADO", Azul para "PROGRAMADO")
- **Flujo de Acción (Botones en Grid):** - Si el estado es "PROGRAMADO", mostrar un botón "Registrar Resultado" en la fila.
    - Este botón abre un `Dialog` pequeño con dos `IntegerField` (Goles Local, Goles Visitante) y un botón "Guardar".
    - Al guardar, llamar a `partidoService.registrarResultado(id, golesLocal, golesVisitante)` y refrescar el Grid.

## 4. Vista Tabla de Posiciones (`PosicionesView.java`)
- **Ruta:** `@Route(value = "posiciones", layout = MainLayout.class)`
- **Nota UX:** Esta vista es de SOLO LECTURA. No hay formularios.
- **Filtro Superior:** Un `ComboBox<Torneo>` para seleccionar qué liga se está viendo (auto-seleccionar el torneo "ACTIVO" por defecto).
- **Contenido:** `Grid<EstadisticaEquipo>`.
- **Orden de Columnas:** Equipo (Nombre), PJ (Partidos Jugados), PG (Ganados), PE (Empates), PP (Perdidos), GF (Goles Favor), GC (Goles Contra), DG (Diferencia = GF-GC), **PTS (Puntos - Columna destacada en negrita)**.
- **Lógica:** El Grid debe llenarse llamando a `estadisticaService.obtenerTablaPosiciones(torneoId)`. (El backend ya devuelve la lista ordenada).