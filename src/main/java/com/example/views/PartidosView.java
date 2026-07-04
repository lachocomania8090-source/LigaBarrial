package com.example.views;

import com.example.logica.EquipoService;
import com.example.logica.PartidoService;
import com.example.modelos.Equipo;
import com.example.modelos.Partido;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;

@Route(value = "partidos", layout = MainLayout.class)
public class PartidosView extends VerticalLayout {

    private final PartidoService partidoService;
    private final EquipoService equipoService;
    private Grid<Partido> grid;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public PartidosView(PartidoService partidoService, EquipoService equipoService) {
        this.partidoService = partidoService;
        this.equipoService = equipoService;

        // Configurar layout principal
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Crear componentes
        add(crearTitulo());
        add(crearGrid());

        // Cargar datos
        cargarPartidos();
    }

    private H2 crearTitulo() {
        H2 titulo = new H2("Gestión de Partidos");
        titulo.addClassNames(LumoUtility.Margin.Bottom.LARGE);
        return titulo;
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

    private Span crearBadgeEstado(String estado) {
        Span badge = new Span(estado);
        badge.getElement().getThemeList().add("badge");

        if ("JUGADO".equals(estado)) {
            badge.addClassNames(LumoUtility.Background.SUCCESS, LumoUtility.TextColor.SUCCESS_CONTRAST);
        } else if ("PROGRAMADO".equals(estado)) {
            badge.addClassNames(LumoUtility.Background.PRIMARY, LumoUtility.TextColor.PRIMARY_CONTRAST);
        }

        return badge;
    }

    private Button crearBotonesAcciones(Partido partido) {
        Button registrarButton = new Button("Registrar");
        registrarButton.setIcon(VaadinIcon.PLUS.create());
        registrarButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        registrarButton.setEnabled("PROGRAMADO".equals(partido.getEstado()));
        registrarButton.addClickListener(e -> abrirDialogoRegistro(partido));

        return registrarButton;
    }

    private void cargarPartidos() {
        grid.setItems(partidoService.obtenerTodos());
    }

    private void abrirDialogoRegistro(Partido partido) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Registrar Resultado - " + obtenerNombreEquipo(partido.getEquipoLocalId()) + 
                " vs " + obtenerNombreEquipo(partido.getEquipoVisitanteId()));
        dialog.setWidth("500px");

        // Crear formulario
        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(true);
        contenido.setSpacing(true);

        // Campos del formulario
        IntegerField golesLocalField = new IntegerField("Goles Local");
        golesLocalField.setWidthFull();
        golesLocalField.setRequiredIndicatorVisible(true);
        golesLocalField.setMin(0);
        golesLocalField.setValue(0);

        IntegerField golesVisitanteField = new IntegerField("Goles Visitante");
        golesVisitanteField.setWidthFull();
        golesVisitanteField.setRequiredIndicatorVisible(true);
        golesVisitanteField.setMin(0);
        golesVisitanteField.setValue(0);

        contenido.add(golesLocalField, golesVisitanteField);

        // Botones del diálogo
        Button guardarButton = new Button("Guardar");
        guardarButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        guardarButton.addClickListener(e -> {
            try {
                int golesLocal = golesLocalField.getValue();
                int golesVisitante = golesVisitanteField.getValue();

                // Registrar el resultado
                partidoService.registrarResultado(partido.getId(), golesLocal, golesVisitante);

                dialog.close();
                cargarPartidos();
                Notification.show("Resultado registrado correctamente", 3000, Notification.Position.BOTTOM_CENTER);
            } catch (Exception ex) {
                Notification.show("Error al registrar resultado: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
            }
        });

        Button cancelarButton = new Button("Cancelar");
        cancelarButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        cancelarButton.addClickListener(e -> dialog.close());

        com.vaadin.flow.component.orderedlayout.HorizontalLayout botonesLayout = 
            new com.vaadin.flow.component.orderedlayout.HorizontalLayout(guardarButton, cancelarButton);
        botonesLayout.addClassNames(LumoUtility.Gap.MEDIUM);
        botonesLayout.setJustifyContentMode(JustifyContentMode.END);

        dialog.getFooter().add(botonesLayout);
        dialog.add(contenido);

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
