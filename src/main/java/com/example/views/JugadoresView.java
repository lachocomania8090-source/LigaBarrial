package com.example.views;

import com.example.logica.EquipoService;
import com.example.logica.JugadorService;
import com.example.modelos.Equipo;
import com.example.modelos.Jugador;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.util.Optional;

@Route(value = "jugadores", layout = MainLayout.class)
public class JugadoresView extends VerticalLayout {

    private final JugadorService jugadorService;
    private final EquipoService equipoService;
    private Grid<Jugador> grid;
    private TextField searchField;

    @Autowired
    public JugadoresView(JugadorService jugadorService, EquipoService equipoService) {
        this.jugadorService = jugadorService;
        this.equipoService = equipoService;

        // Configurar layout principal
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Crear componentes
        add(crearTitulo());
        add(crearBarraHerramientas());
        add(crearGrid());

        // Cargar datos
        cargarJugadores();
    }

    private H2 crearTitulo() {
        H2 titulo = new H2("Gestión de Jugadores");
        titulo.addClassNames(LumoUtility.Margin.Bottom.LARGE);
        return titulo;
    }

    private HorizontalLayout crearBarraHerramientas() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addClassNames(
            LumoUtility.Gap.MEDIUM,
            LumoUtility.Margin.Bottom.MEDIUM
        );

        // Campo de búsqueda
        searchField = new TextField("Buscar por nombre");
        searchField.setPlaceholder("Ingrese nombre del jugador");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addValueChangeListener(e -> filtrarJugadores(e.getValue()));

        // Botón Nuevo Jugador
        Button nuevoButton = new Button("Nuevo Jugador");
        nuevoButton.setIcon(VaadinIcon.PLUS.create());
        nuevoButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        nuevoButton.addClickListener(e -> abrirDialogoCrear());

        toolbar.add(searchField, nuevoButton);
        return toolbar;
    }

    private Grid<Jugador> crearGrid() {
        grid = new Grid<>(Jugador.class, false);
        grid.addClassNames(LumoUtility.Border.ALL);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Columna: Nombre
        grid.addColumn(Jugador::getNombre)
            .setHeader("Nombre")
            .setSortable(true)
            .setAutoWidth(true);

        // Columna: Edad
        grid.addColumn(Jugador::getEdad)
            .setHeader("Edad")
            .setSortable(true)
            .setAutoWidth(true);

        // Columna: Posición
        grid.addColumn(Jugador::getPosicion)
            .setHeader("Posición")
            .setSortable(true)
            .setAutoWidth(true);

        // Columna: Número
        grid.addColumn(Jugador::getNumero)
            .setHeader("Número")
            .setSortable(true)
            .setAutoWidth(true);

        // Columna: Equipo (personalizada - obtiene el nombre del equipo por equipoId)
        grid.addColumn(jugador -> obtenerNombreEquipo(jugador.getEquipoId()))
            .setHeader("Equipo")
            .setSortable(false)
            .setAutoWidth(true);

        // Columna: Acciones
        grid.addComponentColumn(jugador -> crearBotonesAcciones(jugador))
            .setHeader("Acciones")
            .setAutoWidth(true);

        // Hacer que el grid ocupe todo el espacio disponible
        grid.setSizeFull();
        return grid;
    }

    private HorizontalLayout crearBotonesAcciones(Jugador jugador) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setAlignItems(Alignment.CENTER);

        // Botón Editar
        Button editarButton = new Button(VaadinIcon.EDIT.create());
        editarButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        editarButton.setTooltipText("Editar jugador");
        editarButton.addClickListener(e -> abrirDialogoEditar(jugador));

        // Botón Eliminar
        Button eliminarButton = new Button(VaadinIcon.TRASH.create());
        eliminarButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        eliminarButton.addClassNames(LumoUtility.TextColor.ERROR);
        eliminarButton.setTooltipText("Eliminar jugador");
        eliminarButton.addClickListener(e -> eliminarJugador(jugador));

        layout.add(editarButton, eliminarButton);
        return layout;
    }

    private void eliminarJugador(Jugador jugador) {
        jugadorService.eliminar(jugador);
        cargarJugadores();
        searchField.clear();
        Notification.show("Jugador eliminado correctamente", 3000, Notification.Position.BOTTOM_CENTER);
    }

    private String obtenerNombreEquipo(String equipoId) {
        if (equipoId == null || equipoId.isEmpty()) {
            return "Sin equipo";
        }
        return equipoService.obtenerPorId(equipoId)
            .map(Equipo::getNombre)
            .orElse("Equipo no encontrado");
    }

    private void cargarJugadores() {
        grid.setItems(jugadorService.obtenerTodos());
    }

    private void filtrarJugadores(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            cargarJugadores();
        } else {
            grid.setItems(jugadorService.obtenerTodos().stream()
                .filter(jugador -> jugador.getNombre().toLowerCase().contains(filtro.toLowerCase()))
                .toList());
        }
    }

    private void abrirDialogoCrear() {
        Jugador nuevoJugador = new Jugador();
        abrirDialogo(nuevoJugador, false);
    }

    private void abrirDialogoEditar(Jugador jugador) {
        abrirDialogo(jugador, true);
    }

    private void abrirDialogo(Jugador jugador, boolean esEdicion) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(esEdicion ? "Editar Jugador" : "Nuevo Jugador");
        dialog.setWidth("500px");

        // Crear formulario
        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(true);
        contenido.setSpacing(true);

        // Campos del formulario
        TextField nombreField = new TextField("Nombre");
        nombreField.setWidthFull();
        nombreField.setRequiredIndicatorVisible(true);

        IntegerField edadField = new IntegerField("Edad");
        edadField.setWidthFull();
        edadField.setRequiredIndicatorVisible(true);
        edadField.setMin(1);
        edadField.setMax(150);

        IntegerField numeroField = new IntegerField("Número");
        numeroField.setWidthFull();
        numeroField.setRequiredIndicatorVisible(true);
        numeroField.setMin(1);
        numeroField.setMax(99);

        // ComboBox para Posición
        ComboBox<String> posicionBox = new ComboBox<>("Posición");
        posicionBox.setWidthFull();
        posicionBox.setRequiredIndicatorVisible(true);
        posicionBox.setItems("Portero", "Defensa", "Mediocampista", "Delantero");

        // ComboBox para Equipo - carga todos los equipos
        ComboBox<Equipo> equipoBox = new ComboBox<>("Equipo");
        equipoBox.setWidthFull();
        equipoBox.setRequiredIndicatorVisible(true);
        equipoBox.setItems(equipoService.obtenerTodos());
        equipoBox.setItemLabelGenerator(Equipo::getNombre);

        contenido.add(nombreField, edadField, numeroField, posicionBox, equipoBox);

        // Configurar Binder
        Binder<Jugador> binder = new Binder<>(Jugador.class);
        binder.forField(nombreField)
            .asRequired("El nombre es requerido")
            .bind(Jugador::getNombre, Jugador::setNombre);
        binder.forField(edadField)
            .asRequired("La edad es requerida")
            .bind(Jugador::getEdad, Jugador::setEdad);
        binder.forField(numeroField)
            .asRequired("El número es requerido")
            .bind(Jugador::getNumero, Jugador::setNumero);
        binder.forField(posicionBox)
            .asRequired("La posición es requerida")
            .bind(Jugador::getPosicion, Jugador::setPosicion);

        // Binding especial para el ComboBox<Equipo> -> equipoId del Jugador
        binder.forField(equipoBox)
            .asRequired("El equipo es requerido")
            .bind(
                j -> {
                    if (j.getEquipoId() != null && !j.getEquipoId().isEmpty()) {
                        return equipoService.obtenerPorId(j.getEquipoId()).orElse(null);
                    }
                    return null;
                },
                (j, equipo) -> {
                    if (equipo != null) {
                        j.setEquipoId(equipo.getId());
                    }
                }
            );

        // Cargar datos en el formulario si es edición
        if (esEdicion) {
            binder.readBean(jugador);
        }

        // Botones del diálogo
        Button guardarButton = new Button("Guardar");
        guardarButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        guardarButton.addClickListener(e -> {
            try {
                if (binder.writeBeanIfValid(jugador)) {
                    jugadorService.guardar(jugador);
                    dialog.close();
                    cargarJugadores();
                    searchField.clear();
                    String mensaje = esEdicion ? "Jugador actualizado correctamente" : "Jugador creado correctamente";
                    Notification notif = new Notification(mensaje, 3000, Notification.Position.BOTTOM_CENTER);
                    notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notif.open();
                } else {
                    Notification notif = new Notification("Por favor, completa todos los campos requeridos", 3000, Notification.Position.BOTTOM_CENTER);
                    notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notif.open();
                }
            } catch (Exception ex) {
                String mensajeError = "Error al guardar jugador: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString());
                Notification notif = new Notification(mensajeError, 5000, Notification.Position.BOTTOM_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notif.open();
                ex.printStackTrace();
            }
        });

        Button cancelarButton = new Button("Cancelar");
        cancelarButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
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
}
