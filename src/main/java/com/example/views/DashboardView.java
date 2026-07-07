package com.example.views;

import com.example.logica.EquipoService;
import com.example.logica.PartidoService;
import com.example.logica.TorneoService;
import com.example.logica.EstadisticaService;
import com.example.modelos.Equipo;
import com.example.modelos.Partido;
import com.example.modelos.Torneo;
import com.example.modelos.EstadisticaEquipo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Route(value = "", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private final EquipoService equipoService;
    private final TorneoService torneoService;
    private final PartidoService partidoService;
    private final EstadisticaService estadisticaService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter fechaSimpleFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    public DashboardView(EquipoService equipoService, TorneoService torneoService, PartidoService partidoService, EstadisticaService estadisticaService) {
        this.equipoService = equipoService;
        this.torneoService = torneoService;
        this.partidoService = partidoService;
        this.estadisticaService = estadisticaService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(crearTitulo());
        add(crearResumen());
    }

    private VerticalLayout crearTitulo() {
        H2 titulo = new H2("Panel de Control");
        titulo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.MEDIUM);

        Paragraph descripcion = new Paragraph("Resumen rápido de la liga");
        descripcion.addClassNames(LumoUtility.Margin.Bottom.LARGE);

        VerticalLayout header = new VerticalLayout(titulo, descripcion);
        header.setPadding(false);
        header.setSpacing(false);
        return header;
    }

    private HorizontalLayout crearResumen() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassNames(LumoUtility.Gap.LARGE);
        layout.setSpacing(true);

        // Card 1: Equipos Registrados
        int equiposCount = equipoService.obtenerTodos().size();
        VerticalLayout cardEquipos = crearCard("Equipos Registrados", String.valueOf(equiposCount), null);

        // Card 2: Torneos Activos (con botón Gestionar)
        long torneosActivos = torneoService.obtenerTodos().stream()
                .filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()))
                .count();

        Button gestionarButton = new Button("Gestionar");
        gestionarButton.setIcon(VaadinIcon.COG.create());
        gestionarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        gestionarButton.addClickListener(e -> abrirDialogoGestionTorneo());

        VerticalLayout cardTorneos = crearCard("Torneos Activos", String.valueOf(torneosActivos), gestionarButton);

        // Card 3: Próximo Partido (Corregido para ignorar cancelados y torneos finalizados)
        String proximoTexto = "No hay partidos programados";

        // 1. Buscamos el ID del torneo activo actual
        String idTorneoActivo = torneoService.obtenerTodos().stream()
                .filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()))
                .map(Torneo::getId)
                .findFirst()
                .orElse(null);

        // 2. Solo buscamos el próximo partido si hay un torneo activo corriendo
        if (idTorneoActivo != null) {
            Partido proximo = partidoService.obtenerTodos().stream()
                    .filter(p -> idTorneoActivo.equals(p.getTorneoId())) // Que pertenezca al torneo activo
                    .filter(p -> "PROGRAMADO".equalsIgnoreCase(p.getEstado())) // Que NO esté jugado ni cancelado
                    .filter(p -> p.getFechaHora() != null && p.getFechaHora().isAfter(LocalDateTime.now())) // Fecha futura
                    .sorted(Comparator.comparing(Partido::getFechaHora))
                    .findFirst()
                    .orElse(null);

            if (proximo != null) {
                String local = equipoService.obtenerPorId(proximo.getEquipoLocalId()).map(Equipo::getNombre).orElse("-");
                String visitante = equipoService.obtenerPorId(proximo.getEquipoVisitanteId()).map(Equipo::getNombre).orElse("-");
                proximoTexto = local + " vs " + visitante + " — " + (proximo.getFechaHora() != null ? proximo.getFechaHora().format(formatter) : "");
            }
        }

        VerticalLayout cardProximo = crearCard("Próximo Partido", proximoTexto, null);

        // Card 4: Historial de Torneos
        Icon iconoTrofeo = VaadinIcon.TROPHY.create();
        iconoTrofeo.setSize("48px");

        Button verHistorialButton = new Button("Ver Historial");
        verHistorialButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        verHistorialButton.addClickListener(e -> abrirDialogoHistorial());

        VerticalLayout cardHistorial = new VerticalLayout();
        cardHistorial.add(iconoTrofeo, verHistorialButton);
        cardHistorial.setWidth("280px");
        cardHistorial.setHeight("180px");
        cardHistorial.setPadding(true);
        cardHistorial.setSpacing(true);
        cardHistorial.setAlignItems(Alignment.CENTER);
        cardHistorial.setJustifyContentMode(JustifyContentMode.CENTER);
        cardHistorial.getStyle().set("box-shadow", "0 2px 6px rgba(0, 0, 0, 0.1)");
        cardHistorial.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        cardHistorial.getStyle().set("background", "var(--lumo-base-color)");
        cardHistorial.getStyle().set("border", "1px solid var(--lumo-border-color)");

        layout.add(cardEquipos, cardTorneos, cardProximo, cardHistorial);

        return layout;
    }

    private VerticalLayout crearCard(String titulo, String contenido, Button boton) {
        H3 t = new H3(titulo);
        t.addClassNames(
                LumoUtility.FontSize.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL,
                LumoUtility.TextColor.SECONDARY
        );

        Paragraph p = new Paragraph(contenido);
        p.addClassNames(
                LumoUtility.FontSize.XXLARGE,
                LumoUtility.FontWeight.BOLD
        );

        VerticalLayout card = new VerticalLayout(t, p);

        if (boton != null) {
            card.add(boton);
        }

        card.setWidth("280px");
        card.setHeight("180px");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);

        card.getStyle().set("box-shadow", "0 2px 6px rgba(0, 0, 0, 0.1)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        card.getStyle().set("background", "var(--lumo-base-color)");
        card.getStyle().set("border", "1px solid var(--lumo-border-color)");

        return card;
    }

    private void abrirDialogoGestionTorneo() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(true);
        contenido.setSpacing(true);

        // Buscar torneo activo
        Optional<Torneo> torneoActivo = torneoService.obtenerTodos().stream()
                .filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()))
                .findFirst();

        if (torneoActivo.isPresent()) {
            // Existe torneo activo: mostrar opciones de finalizar o cancelar
            Torneo torneo = torneoActivo.get();
            dialog.setHeaderTitle("Gestionar Torneo Activo");

            H3 nombreTorneo = new H3(torneo.getNombre());
            nombreTorneo.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
            contenido.add(nombreTorneo);

            // Botón para Finalizar de forma normal
            Button finalizarButton = new Button("Finalizar Torneo");
            finalizarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
            finalizarButton.addClickListener(e -> {
                try {
                    torneo.setEstado("FINALIZADO");
                    torneoService.actualizar(torneo);

                    dialog.close();
                    Notification.show("Torneo finalizado correctamente", 3000, Notification.Position.BOTTOM_CENTER);

                    // Refrescar el Dashboard de forma limpia
                    getUI().ifPresent(ui -> ui.access(() -> {
                        removeAll();
                        add(crearTitulo());
                        add(crearResumen());
                    }));
                } catch (Exception ex) {
                    Notification.show("Error al finalizar torneo: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
                }
            });

            // NUEVO: Botón para Cancelar el torneo (Acción destructiva sutil)
            Button cancelarTorneoButton = new Button("Cancelar Torneo");
            cancelarTorneoButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            cancelarTorneoButton.addClassNames(LumoUtility.TextColor.ERROR);
            cancelarTorneoButton.addClickListener(e -> {
                try {
                    torneo.setEstado("CANCELADO");
                    torneoService.actualizar(torneo); // Guarda el estado en MongoDB sin calcular campeón

                    dialog.close();
                    Notification.show("Torneo cancelado correctamente", 3000, Notification.Position.BOTTOM_CENTER);

                    // Refrescar el Dashboard
                    getUI().ifPresent(ui -> ui.access(() -> {
                        removeAll();
                        add(crearTitulo());
                        add(crearResumen());
                    }));
                } catch (Exception ex) {
                    Notification.show("Error al cancelar torneo: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
                }
            });

            HorizontalLayout botonesLayout = new HorizontalLayout(finalizarButton, cancelarTorneoButton);
            botonesLayout.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Gap.MEDIUM);
            contenido.add(botonesLayout);

        } else {
            // No existe torneo activo: mostrar formulario para crear uno
            dialog.setHeaderTitle("Iniciar Nuevo Torneo");

            TextField nombreField = new TextField("Nombre del Torneo");
            nombreField.setWidthFull();
            nombreField.setRequiredIndicatorVisible(true);
            nombreField.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
            contenido.add(nombreField);

            Button iniciarButton = new Button("Iniciar Torneo");
            iniciarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            iniciarButton.addClickListener(e -> {
                if (nombreField.getValue() == null || nombreField.getValue().isEmpty()) {
                    Notification.show("Por favor, ingresa el nombre del torneo", 3000, Notification.Position.BOTTOM_CENTER);
                    return;
                }

                try {
                    Torneo nuevoTorneo = new Torneo();
                    nuevoTorneo.setNombre(nombreField.getValue());
                    nuevoTorneo.setEstado("ACTIVO");
                    torneoService.crearTorneo(nuevoTorneo);

                    dialog.close();
                    Notification.show("Torneo iniciado correctamente", 3000, Notification.Position.BOTTOM_CENTER);

                    getUI().ifPresent(ui -> ui.access(() -> {
                        removeAll();
                        add(crearTitulo());
                        add(crearResumen());
                    }));
                } catch (Exception ex) {
                    Notification.show("Error al iniciar torneo: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
                }
            });

            Button cancelarButton = new Button("Cancelar");
            cancelarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            cancelarButton.addClickListener(e -> dialog.close());

            HorizontalLayout botonesLayout = new HorizontalLayout(iniciarButton, cancelarButton);
            botonesLayout.addClassNames(LumoUtility.Gap.MEDIUM);
            contenido.add(botonesLayout);
        }

        dialog.add(contenido);
        dialog.open();
    }

    private void abrirDialogoHistorial() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Historial de Torneos");
        dialog.setWidth("950px");
        dialog.setHeight("750px");

        VerticalLayout contenido = new VerticalLayout();
        contenido.setSizeFull();
        contenido.setPadding(true);
        contenido.setSpacing(true);

        List<Torneo> torneosFinalizados = torneoService.obtenerTodos().stream()
                .filter(t -> "FINALIZADO".equalsIgnoreCase(t.getEstado()))
                .toList();

        ComboBox<Torneo> torneoBox = new ComboBox<>("Seleccionar Torneo");
        torneoBox.setWidthFull();
        torneoBox.setItems(torneosFinalizados);
        torneoBox.setItemLabelGenerator(t -> t.getNombre() + " - Campeón: " +
                (t.getNombreCampeon() != null ? t.getNombreCampeon() : "N/A"));

        H3 mensajeCampeon = new H3("🏆 Campeón: -");
        mensajeCampeon.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        mensajeCampeon.setVisible(false);

        // --- SISTEMA DE PESTAÑAS (TABS) ---
        Tab tabPosiciones = new Tab("Tabla de Posiciones");
        Tab tabPartidos = new Tab("Partidos Jugados");
        Tabs tabs = new Tabs(tabPosiciones, tabPartidos);
        tabs.setWidthFull();

        // 1. Grid de Estadísticas (Pestaña 1)
        Grid<EstadisticaEquipo> gridEstadisticas = new Grid<>(EstadisticaEquipo.class, false);
        gridEstadisticas.setSizeFull();
        gridEstadisticas.addClassNames(LumoUtility.Border.ALL);

        gridEstadisticas.addColumn(est -> equipoService.obtenerPorId(est.getEquipoId()).map(Equipo::getNombre).orElse("-"))
                .setHeader("Equipo").setAutoWidth(true);
        gridEstadisticas.addColumn(EstadisticaEquipo::getPartidosJugados).setHeader("PJ").setAutoWidth(true);
        gridEstadisticas.addColumn(EstadisticaEquipo::getPartidosGanados).setHeader("PG").setAutoWidth(true);
        gridEstadisticas.addColumn(EstadisticaEquipo::getPartidosEmpatados).setHeader("PE").setAutoWidth(true);
        gridEstadisticas.addColumn(EstadisticaEquipo::getPartidosPerdidos).setHeader("PP").setAutoWidth(true);
        gridEstadisticas.addColumn(EstadisticaEquipo::getGolesFavor).setHeader("GF").setAutoWidth(true);
        gridEstadisticas.addColumn(EstadisticaEquipo::getGolesContra).setHeader("GC").setAutoWidth(true);
        gridEstadisticas.addColumn(est -> est.getGolesFavor() - est.getGolesContra()).setHeader("DG").setAutoWidth(true);
        gridEstadisticas.addColumn(EstadisticaEquipo::getPuntos).setHeader("PTS").setAutoWidth(true);

        // 2. Grid de Partidos Históricos (Pestaña 2)
        Grid<Partido> gridPartidosHistorial = new Grid<>(Partido.class, false);
        gridPartidosHistorial.setSizeFull();
        gridPartidosHistorial.addClassNames(LumoUtility.Border.ALL);
        gridPartidosHistorial.setVisible(false); // Oculto por defecto

        gridPartidosHistorial.addColumn(p -> equipoService.obtenerPorId(p.getEquipoLocalId()).map(Equipo::getNombre).orElse("-"))
                .setHeader("Equipo Local").setAutoWidth(true);
        gridPartidosHistorial.addColumn(p -> "CANCELADO".equalsIgnoreCase(p.getEstado()) ?
                        "S/J (CANCELADO)" : p.getGolesLocal() + " - " + p.getGolesVisitante())
                .setHeader("Resultado").setAutoWidth(true);

        gridPartidosHistorial.addColumn(p -> equipoService.obtenerPorId(p.getEquipoVisitanteId()).map(Equipo::getNombre).orElse("-"))
                .setHeader("Equipo Visitante").setAutoWidth(true);
        gridPartidosHistorial.addColumn(p -> p.getFechaHora() != null ? p.getFechaHora().format(fechaSimpleFormatter) : "N/A")
                .setHeader("Fecha").setAutoWidth(true);

        // Listener de intercambio de pestañas
        tabs.addSelectedChangeListener(event -> {
            boolean posicionesActivo = event.getSelectedTab().equals(tabPosiciones);
            gridEstadisticas.setVisible(posicionesActivo);
            gridPartidosHistorial.setVisible(!posicionesActivo);
        });

        // Listener del ComboBox de Selección de Torneo pasado
        torneoBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                Torneo torneoSeleccionado = e.getValue();

                if (torneoSeleccionado.getNombreCampeon() != null) {
                    mensajeCampeon.setText("🏆 Campeón: " + torneoSeleccionado.getNombreCampeon());
                } else {
                    mensajeCampeon.setText("🏆 Campeón: -");
                }
                mensajeCampeon.setVisible(true);

                // Llenar ambos grids dinámicamente usando el ID del torneo archivado
                List<EstadisticaEquipo> estadisticas = estadisticaService.obtenerTablaPosiciones(torneoSeleccionado.getId());
                gridEstadisticas.setItems(estadisticas);

                List<Partido> partidosPasados = partidoService.obtenerPartidosPorTorneo(torneoSeleccionado.getId());
                gridPartidosHistorial.setItems(partidosPasados);
            } else {
                mensajeCampeon.setVisible(false);
                gridEstadisticas.setItems(List.of());
                gridPartidosHistorial.setItems(List.of());
            }
        });

        contenido.add(torneoBox, mensajeCampeon, tabs, gridEstadisticas, gridPartidosHistorial);
        dialog.add(contenido);
        dialog.open();
    }
}