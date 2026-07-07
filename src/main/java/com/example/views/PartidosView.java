    package com.example.views;

    import com.example.logica.EquipoService;
    import com.example.logica.JugadorService;
    import com.example.logica.PartidoService;
    import com.example.modelos.Equipo;
    import com.example.modelos.Partido;
    import com.vaadin.flow.component.button.Button;
    import com.vaadin.flow.component.button.ButtonVariant;
    import com.vaadin.flow.component.combobox.ComboBox;
    import com.vaadin.flow.component.datetimepicker.DateTimePicker;
    import com.vaadin.flow.component.dialog.Dialog;
    import com.vaadin.flow.component.grid.Grid;
    import com.vaadin.flow.component.html.H2;
    import com.vaadin.flow.component.html.Span;
    import com.vaadin.flow.component.notification.Notification;
    import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
    import com.vaadin.flow.component.orderedlayout.VerticalLayout;
    import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
    import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
    import com.vaadin.flow.component.textfield.IntegerField;
    import com.vaadin.flow.data.binder.Binder;
    import com.vaadin.flow.router.Route;
    import com.vaadin.flow.theme.lumo.LumoUtility;
    import com.vaadin.flow.component.icon.VaadinIcon;
    import org.springframework.beans.factory.annotation.Autowired;

    import java.time.format.DateTimeFormatter;

    @Route(value = "partidos", layout = MainLayout.class)
    public class PartidosView extends VerticalLayout {

        private final PartidoService partidoService;
        private final EquipoService equipoService;
        private final JugadorService jugadorService;
        private final com.example.logica.TorneoService torneoService;
        private Grid<Partido> grid;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        @Autowired
        public PartidosView(PartidoService partidoService, EquipoService equipoService, JugadorService jugadorService, com.example.logica.TorneoService torneoService) {
            this.partidoService = partidoService;
            this.equipoService = equipoService;
            this.jugadorService = jugadorService;
            this.torneoService = torneoService;

            // Configurar layout principal
            setSizeFull();
            setPadding(true);
            setSpacing(true);

            // Crear componentes
            add(crearTitulo());
            add(crearBarraHerramientas());
            add(crearGrid());

            // Cargar datos
            cargarPartidos();
        }

        private H2 crearTitulo() {
            H2 titulo = new H2("Gestión de Partidos");
            titulo.addClassNames(LumoUtility.Margin.Bottom.LARGE);
            return titulo;
        }

        private HorizontalLayout crearBarraHerramientas() {
            HorizontalLayout toolbar = new HorizontalLayout();
            toolbar.addClassNames(
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Margin.Bottom.MEDIUM
            );

            // Botón Nuevo Partido
            Button nuevoButton = new Button("Nuevo Partido");
            nuevoButton.setIcon(VaadinIcon.PLUS.create());
            nuevoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            nuevoButton.addClickListener(e -> abrirDialogoCrear());

            toolbar.add(nuevoButton);
            return toolbar;
        }

        private Grid<Partido> crearGrid() {
            grid = new Grid<>(Partido.class, false);
            grid.addClassNames(LumoUtility.Border.ALL);
            grid.setSelectionMode(Grid.SelectionMode.SINGLE);

            // Columna: Equipo Local (nombre consultando EquipoService)
            grid.addColumn(partido -> obtenerNombreEquipo(partido.getEquipoLocalId()))
                .setHeader("Equipo Local")
                .setSortable(false)
                .setAutoWidth(true);

            // Columna: Goles Local
            grid.addColumn(Partido::getGolesLocal)
                .setHeader("Goles Local")
                .setSortable(true)
                .setAutoWidth(true);

            // Columna: Goles Visitante
            grid.addColumn(Partido::getGolesVisitante)
                .setHeader("Goles Visitante")
                .setSortable(true)
                .setAutoWidth(true);

            // Columna: Equipo Visitante (nombre consultando EquipoService)
            grid.addColumn(partido -> obtenerNombreEquipo(partido.getEquipoVisitanteId()))
                .setHeader("Equipo Visitante")
                .setSortable(false)
                .setAutoWidth(true);

            // Columna: Fecha
            grid.addColumn(partido -> partido.getFechaHora() != null ?
                    partido.getFechaHora().format(formatter) : "N/A")
                .setHeader("Fecha")
                .setSortable(false)
                .setAutoWidth(true);

            // Columna: Estado con Badge (usar componente para que se renderice correctamente)
            grid.addComponentColumn(partido -> crearBadgeEstado(partido.getEstado()))
                .setHeader("Estado")
                .setSortable(false)
                .setAutoWidth(true);

            // Columna: Acciones (botón Registrar solo si PROGRAMADO)
            grid.addComponentColumn(partido -> crearBotonesAcciones(partido))
                .setHeader("Acciones")
                .setSortable(false)
                .setAutoWidth(true);

            // Hacer que el grid ocupe todo el espacio disponible
            grid.setSizeFull();
            return grid;
        }

        private String obtenerNombreEquipo(String equipoId) {
            if (equipoId == null || equipoId.isEmpty()) {
                return "Desconocido";
            }
            return equipoService.obtenerPorId(equipoId)
                .map(Equipo::getNombre)
                .orElse("Equipo no encontrado");
        }

        private boolean validarEquiposConJugadores(String equipoLocalId, String equipoVisitanteId) {
            final int JUGADORES_MINIMOS = 12;

            // Validar equipo local
            int jugadoresLocal = jugadorService.obtenerPorEquipo(equipoLocalId).size();
            if (jugadoresLocal < JUGADORES_MINIMOS) {
                String nombreEquipoLocal = obtenerNombreEquipo(equipoLocalId);
                Notification.show(
                    "El equipo " + nombreEquipoLocal + " tiene solo " + jugadoresLocal +
                    " jugadores. Se requieren mínimo " + JUGADORES_MINIMOS + " jugadores (11 + 1 de cambio).",
                    5000, Notification.Position.BOTTOM_CENTER
                );
                return false;
            }

            // Validar equipo visitante
            int jugadoresVisitante = jugadorService.obtenerPorEquipo(equipoVisitanteId).size();
            if (jugadoresVisitante < JUGADORES_MINIMOS) {
                String nombreEquipoVisitante = obtenerNombreEquipo(equipoVisitanteId);
                Notification.show(
                    "El equipo " + nombreEquipoVisitante + " tiene solo " + jugadoresVisitante +
                    " jugadores. Se requieren mínimo " + JUGADORES_MINIMOS + " jugadores (11 + 1 de cambio).",
                    5000, Notification.Position.BOTTOM_CENTER
                );
                return false;
            }

            return true;
        }

        private Span crearBadgeEstado(String estado) {
            Span badge = new Span(estado);
            badge.getElement().getThemeList().add("badge");

            if ("JUGADO".equals(estado)) {
                badge.addClassNames(LumoUtility.Background.SUCCESS, LumoUtility.TextColor.SUCCESS_CONTRAST);
            } else if ("PROGRAMADO".equals(estado)) {
                badge.addClassNames(LumoUtility.Background.PRIMARY, LumoUtility.TextColor.PRIMARY_CONTRAST);
            } else if ("CANCELADO".equals(estado)) {
                badge.getElement().getThemeList().add("badge contrast"); // Aplica el gris de contraste nativo automáticamente
            }

            return badge;
        }

        private HorizontalLayout crearBotonesAcciones(Partido partido) {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setPadding(false);
            layout.setSpacing(true);
            layout.setAlignItems(Alignment.CENTER);

            // Botón Registrar (azul con letras blancas, solo si PROGRAMADO)
            Button registrarButton = new Button("Registrar");
            registrarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            registrarButton.setEnabled("PROGRAMADO".equals(partido.getEstado()));
            registrarButton.addClickListener(e -> abrirDialogoRegistro(partido));

            // Botón Eliminar
            Button eliminarButton = new Button(VaadinIcon.TRASH.create());
            eliminarButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            eliminarButton.addClassNames(LumoUtility.TextColor.ERROR);
            eliminarButton.setTooltipText("Eliminar partido");
            eliminarButton.addClickListener(e -> eliminarPartido(partido));

            layout.add(registrarButton, eliminarButton);
            return layout;
        }

        private void eliminarPartido(Partido partido) {
            partidoService.eliminar(partido.getId());
            cargarPartidos();
            Notification.show("Partido eliminado correctamente", 3000, Notification.Position.BOTTOM_CENTER);
        }

        private void cargarPartidos() {
            // Solo cargamos los partidos del torneo activo actual
            torneoService.obtenerTodos().stream()
                    .filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()))
                    .findFirst()
                    .ifPresentOrElse(
                            torneoActivo -> grid.setItems(partidoService.obtenerPartidosPorTorneo(torneoActivo.getId())),
                            () -> grid.setItems(java.util.Collections.emptyList())
                    );
        }

        private void abrirDialogoCrear() {
            // Buscamos el torneo activo actual antes de crear el partido
            com.example.modelos.Torneo torneoActivo = torneoService.obtenerTodos().stream()
                    .filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()))
                    .findFirst()
                    .orElse(null);

            if (torneoActivo == null) {
                Notification.show("⚠️ No se puede crear un partido porque no existe un torneo ACTIVO en el Dashboard.",
                        5000, Notification.Position.BOTTOM_CENTER);
                return;
            }

            Partido nuevoPartido = new Partido();
            nuevoPartido.setEstado("PROGRAMADO"); // Estado por defecto
            nuevoPartido.setTorneoId(torneoActivo.getId()); // <-- SOLUCIÓN: Amarramos el partido al torneo activo actual

            abrirDialogoFormulario(nuevoPartido, false);
        }

        private void abrirDialogoFormulario(Partido partido, boolean esEdicion) {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle(esEdicion ? "Editar Partido" : "Nuevo Partido");
            dialog.setWidth("500px");

            // Crear formulario
            VerticalLayout contenido = new VerticalLayout();
            contenido.setPadding(true);
            contenido.setSpacing(true);

            // ComboBox para Equipo Local
            ComboBox<Equipo> equipoLocalBox = new ComboBox<>("Equipo Local");
            equipoLocalBox.setWidthFull();
            equipoLocalBox.setRequiredIndicatorVisible(true);
            equipoLocalBox.setItems(equipoService.obtenerTodos());
            equipoLocalBox.setItemLabelGenerator(Equipo::getNombre);

            // ComboBox para Equipo Visitante
            ComboBox<Equipo> equipoVisitanteBox = new ComboBox<>("Equipo Visitante");
            equipoVisitanteBox.setWidthFull();
            equipoVisitanteBox.setRequiredIndicatorVisible(true);
            equipoVisitanteBox.setItems(equipoService.obtenerTodos());
            equipoVisitanteBox.setItemLabelGenerator(Equipo::getNombre);

            // DateTimePicker para Fecha y Hora
            DateTimePicker fechaHoraField = new DateTimePicker("Fecha y Hora");
            fechaHoraField.setWidthFull();
            fechaHoraField.setRequiredIndicatorVisible(true);

            contenido.add(equipoLocalBox, equipoVisitanteBox, fechaHoraField);

            // Configurar Binder
            Binder<Partido> binder = new Binder<>(Partido.class);
            binder.forField(equipoLocalBox)
                .asRequired("El equipo local es requerido")
                .bind(
                    p -> {
                        if (p.getEquipoLocalId() != null && !p.getEquipoLocalId().isEmpty()) {
                            return equipoService.obtenerPorId(p.getEquipoLocalId()).orElse(null);
                        }
                        return null;
                    },
                    (p, equipo) -> {
                        if (equipo != null) {
                            p.setEquipoLocalId(equipo.getId());
                        }
                    }
                );
            binder.forField(equipoVisitanteBox)
                .asRequired("El equipo visitante es requerido")
                .bind(
                    p -> {
                        if (p.getEquipoVisitanteId() != null && !p.getEquipoVisitanteId().isEmpty()) {
                            return equipoService.obtenerPorId(p.getEquipoVisitanteId()).orElse(null);
                        }
                        return null;
                    },
                    (p, equipo) -> {
                        if (equipo != null) {
                            p.setEquipoVisitanteId(equipo.getId());
                        }
                    }
                );
            binder.forField(fechaHoraField)
                .asRequired("La fecha y hora son requeridas")
                .bind(Partido::getFechaHora, Partido::setFechaHora);

            // Cargar datos en el formulario si es edición
            if (esEdicion) {
                binder.readBean(partido);
            }

            // Botones del diálogo
            Button guardarButton = new Button("Guardar");
            guardarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            guardarButton.addClickListener(e -> {
                try {
                    if (binder.writeBeanIfValid(partido)) {

                        // --- NUEVA VALIDACIÓN: Evitar que un equipo juegue contra sí mismo ---
                        if (partido.getEquipoLocalId().equals(partido.getEquipoVisitanteId())) {
                            Notification.show("⚠️ Un equipo no puede jugar contra sí mismo. Selecciona equipos diferentes.",
                                    4000, Notification.Position.BOTTOM_CENTER);
                            return; // Detiene el guardado
                        }

                        // Validar que ambos equipos tengan mínimo 12 jugadores
                        if (!validarEquiposConJugadores(partido.getEquipoLocalId(), partido.getEquipoVisitanteId())) {
                            return; // No continúa si la validación falla
                        }

                        partidoService.guardar(partido);
                        dialog.close();
                        cargarPartidos();
                        String mensaje = esEdicion ? "Partido actualizado correctamente" : "Partido creado correctamente";
                        Notification.show(mensaje, 3000, Notification.Position.BOTTOM_CENTER);
                    } else {
                        Notification.show("Por favor, completa todos los campos requeridos", 3000, Notification.Position.BOTTOM_CENTER);
                    }
                } catch (Exception ex) {
                    Notification.show("Error al guardar partido: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()),
                            3000, Notification.Position.BOTTOM_CENTER);
                    ex.printStackTrace();
                }
            });

            Button cancelarButton = new Button("Cancelar");
            cancelarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            cancelarButton.addClickListener(e -> {
                binder.removeBean();
                dialog.close();
            });

            HorizontalLayout botonesLayout = new HorizontalLayout(guardarButton, cancelarButton);
            botonesLayout.addClassNames(LumoUtility.Gap.MEDIUM);
            botonesLayout.setJustifyContentMode(JustifyContentMode.END);

            dialog.getFooter().add(botonesLayout);
            dialog.add(contenido);

            dialog.open();
        }

        private void abrirDialogoRegistro(Partido partido) {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Registrar Resultado");
            dialog.setWidth("600px");
            dialog.setCloseOnEsc(true);
            dialog.setCloseOnOutsideClick(false);

            // Crear layout principal
            VerticalLayout mainLayout = new VerticalLayout();
            mainLayout.setPadding(true);
            mainLayout.setSpacing(true);

            // Fila 1: Título "Marcador"
            H2 marcadorTitle = new H2("Marcador");
            marcadorTitle.addClassNames(LumoUtility.Margin.NONE, LumoUtility.Margin.Bottom.MEDIUM);

            // Fila 2: Equipos con controles de goles
            HorizontalLayout equiposLayout = new HorizontalLayout();
            equiposLayout.setSpacing(true);
            equiposLayout.setWidthFull();
            equiposLayout.setJustifyContentMode(JustifyContentMode.AROUND);

            String nombreLocal = obtenerNombreEquipo(partido.getEquipoLocalId());
            String nombreVisitante = obtenerNombreEquipo(partido.getEquipoVisitanteId());

            // Equipo Local
            HorizontalLayout equipoLocalLayout = new HorizontalLayout();
            equipoLocalLayout.setSpacing(true);
            equipoLocalLayout.setAlignItems(Alignment.CENTER);

            Button menosLocalButton = new Button("-");
            menosLocalButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            menosLocalButton.setWidth("40px");

            IntegerField golesLocalField = new IntegerField();
            golesLocalField.setValue(0);
            golesLocalField.setMin(0);
            golesLocalField.getElement().executeJs(
                    "this.addEventListener('keydown', function(e) { if (e.key === '-') { e.preventDefault(); } });"
            );
            golesLocalField.setWidth("80px");

            Button masLocalButton = new Button("+");
            masLocalButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            masLocalButton.setWidth("40px");

            menosLocalButton.addClickListener(e -> {
                Integer valor = golesLocalField.getValue();
                if (valor != null && valor > 0) {
                    golesLocalField.setValue(valor - 1);
                } else {
                    golesLocalField.setValue(0);
                }
            });

            masLocalButton.addClickListener(e -> {
                Integer valor = golesLocalField.getValue();
                golesLocalField.setValue(valor == null ? 1 : valor + 1);
            });

            VerticalLayout equipoLocalContent = new VerticalLayout();
            equipoLocalContent.setPadding(false);
            equipoLocalContent.setSpacing(false);
            equipoLocalContent.setAlignItems(Alignment.CENTER);
            Span nombreLocalSpan = new Span(nombreLocal);
            nombreLocalSpan.addClassNames(LumoUtility.FontWeight.BOLD);
            equipoLocalContent.add(nombreLocalSpan);
            equipoLocalContent.add(equipoLocalLayout);

            equipoLocalLayout.add(menosLocalButton, golesLocalField, masLocalButton);
            equiposLayout.add(equipoLocalContent);

            // Separador visual
            Span separador = new Span(" - ");
            separador.getStyle().set("font-weight", "bold");
            separador.getStyle().set("font-size", "20px");
            equiposLayout.add(separador);

            // Equipo Visitante
            HorizontalLayout equipoVisitanteLayout = new HorizontalLayout();
            equipoVisitanteLayout.setSpacing(true);
            equipoVisitanteLayout.setAlignItems(Alignment.CENTER);

            Button menosVisitanteButton = new Button("-");
            menosVisitanteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            menosVisitanteButton.setWidth("40px");

            IntegerField golesVisitanteField = new IntegerField();
            golesVisitanteField.setValue(0);
            golesVisitanteField.setMin(0);
            golesVisitanteField.getElement().executeJs(
                    "this.addEventListener('keydown', function(e) { if (e.key === '-') { e.preventDefault(); } });"
            );
            golesVisitanteField.setWidth("80px");

            Button masVisitanteButton = new Button("+");
            masVisitanteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            masVisitanteButton.setWidth("40px");

            menosVisitanteButton.addClickListener(e -> {
                int valor = golesVisitanteField.getValue();
                if (valor > 0) {
                    golesVisitanteField.setValue(valor - 1);
                }
            });

            masVisitanteButton.addClickListener(e -> {
                golesVisitanteField.setValue(golesVisitanteField.getValue() + 1);
            });

            VerticalLayout equipoVisitanteContent = new VerticalLayout();
            equipoVisitanteContent.setPadding(false);
            equipoVisitanteContent.setSpacing(false);
            equipoVisitanteContent.setAlignItems(Alignment.CENTER);
            Span nombreVisitanteSpan = new Span(nombreVisitante);
            nombreVisitanteSpan.addClassNames(LumoUtility.FontWeight.BOLD);
            equipoVisitanteContent.add(nombreVisitanteSpan);
            equipoVisitanteContent.add(equipoVisitanteLayout);

            equipoVisitanteLayout.add(menosVisitanteButton, golesVisitanteField, masVisitanteButton);
            equiposLayout.add(equipoVisitanteContent);

            // Fila 3: Botones de Finalizar, Cancelar Partido y Cerrar (X)
            HorizontalLayout botonesLayout = new HorizontalLayout();
            botonesLayout.setWidthFull();
            botonesLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
            botonesLayout.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Gap.MEDIUM);

            // Botón principal de guardar goles
            Button finalizarButton = new Button("Finalizar Partido");
            finalizarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            finalizarButton.addClickListener(e -> {
                try {
                    int golesLocal = golesLocalField.getValue();
                    int golesVisitante = golesVisitanteField.getValue();

                    partidoService.registrarResultado(partido.getId(), golesLocal, golesVisitante);

                    cargarPartidos();
                    Notification.show("Resultado registrado correctamente", 3000, Notification.Position.BOTTOM_CENTER);
                    dialog.close();
                } catch (Exception ex) {
                    Notification.show("Error al registrar resultado: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
                }
            });

            // NUEVO: Botón intermedio para anular el encuentro sin sumar goles ni estadísticas
            Button cancelarPartidoButton = new Button("Cancelar Partido");
            cancelarPartidoButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            cancelarPartidoButton.addClassNames(LumoUtility.TextColor.ERROR);
            cancelarPartidoButton.addClickListener(e -> {
                try {
                    partido.setEstado("CANCELADO");
                    partidoService.guardar(partido); // Persiste el cambio de estado de forma directa en MongoDB

                    cargarPartidos();
                    Notification.show("Partido cancelado correctamente", 3000, Notification.Position.BOTTOM_CENTER);
                    dialog.close();
                } catch (Exception ex) {
                    Notification.show("Error al cancelar el partido: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
                }
            });

            Button cerrarButton = new Button("X");
            cerrarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            cerrarButton.setWidth("40px");
            cerrarButton.addClickListener(e -> dialog.close());

            // Agrupamos el flujo de acciones de forma organizada
            HorizontalLayout accionesPrincipales = new HorizontalLayout(finalizarButton, cancelarPartidoButton);
            accionesPrincipales.setSpacing(true);

            botonesLayout.add(accionesPrincipales, cerrarButton);

            // Agregar todas las filas al layout principal
            mainLayout.add(marcadorTitle, equiposLayout, botonesLayout);

            dialog.add(mainLayout);
            dialog.open();
        }

        private java.util.List<Partido> obtenerTodosLosPartidos() {
            try {
                return partidoService.obtenerTodos();
            } catch (Exception ex) {
                Notification.show("Error al cargar partidos: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
                return java.util.Collections.emptyList();
            }
        }
    }
