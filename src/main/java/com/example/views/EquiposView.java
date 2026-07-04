package com.example.views;

import com.example.logica.EquipoService;
import com.example.modelos.Equipo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "equipos", layout = MainLayout.class)
public class EquiposView extends VerticalLayout {

    private final EquipoService equipoService;
    private Grid<Equipo> grid;
    private TextField searchField;

    @Autowired
    public EquiposView(EquipoService equipoService) {
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
        cargarEquipos();
    }

    private H2 crearTitulo() {
        H2 titulo = new H2("Gestión de Equipos");
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
        searchField.setPlaceholder("Ingrese nombre del equipo");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addValueChangeListener(e -> filtrarEquipos(e.getValue()));

        // Botón Nuevo Equipo
        Button nuevoButton = new Button("Nuevo Equipo");
        nuevoButton.setIcon(VaadinIcon.PLUS.create());
        nuevoButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        nuevoButton.addClickListener(e -> abrirDialogoCrear());

        toolbar.add(searchField, nuevoButton);
        return toolbar;
    }

    private Grid<Equipo> crearGrid() {
        grid = new Grid<>(Equipo.class, false);
        grid.addClassNames(LumoUtility.Border.ALL);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Columna: Nombre
        grid.addColumn(Equipo::getNombre)
            .setHeader("Nombre")
            .setSortable(true)
            .setAutoWidth(true);

        // Columna: Ciudad
        grid.addColumn(Equipo::getCiudad)
            .setHeader("Ciudad")
            .setSortable(true)
            .setAutoWidth(true);

        // Columna: Entrenador
        grid.addColumn(Equipo::getEntrenador)
            .setHeader("Entrenador")
            .setSortable(true)
            .setAutoWidth(true);

        // Evento de doble clic para editar
        grid.addItemDoubleClickListener(event -> {
            Equipo equipo = event.getItem();
            abrirDialogoEditar(equipo);
        });

        // Hacer que el grid ocupe todo el espacio disponible
        grid.setSizeFull();
        return grid;
    }

    private void cargarEquipos() {
        grid.setItems(equipoService.obtenerTodos());
    }

    private void filtrarEquipos(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            cargarEquipos();
        } else {
            grid.setItems(equipoService.obtenerTodos().stream()
                .filter(equipo -> equipo.getNombre().toLowerCase().contains(filtro.toLowerCase()))
                .toList());
        }
    }

    private void abrirDialogoCrear() {
        Equipo nuevoEquipo = new Equipo();
        abrirDialogo(nuevoEquipo, false);
    }

    private void abrirDialogoEditar(Equipo equipo) {
        abrirDialogo(equipo, true);
    }

    private void abrirDialogo(Equipo equipo, boolean esEdicion) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(esEdicion ? "Editar Equipo" : "Nuevo Equipo");
        dialog.setWidth("500px");

        // Crear formulario
        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(true);
        contenido.setSpacing(true);

        // Campos del formulario
        TextField nombreField = new TextField("Nombre");
        nombreField.setWidthFull();
        nombreField.setRequiredIndicatorVisible(true);

        TextField ciudadField = new TextField("Ciudad");
        ciudadField.setWidthFull();
        ciudadField.setRequiredIndicatorVisible(true);

        TextField entrenadorField = new TextField("Entrenador");
        entrenadorField.setWidthFull();
        entrenadorField.setRequiredIndicatorVisible(true);

        contenido.add(nombreField, ciudadField, entrenadorField);

        // Configurar Binder
        Binder<Equipo> binder = new Binder<>(Equipo.class);
        binder.forField(nombreField)
            .asRequired("El nombre es requerido")
            .bind(Equipo::getNombre, Equipo::setNombre);
        binder.forField(ciudadField)
            .asRequired("La ciudad es requerida")
            .bind(Equipo::getCiudad, Equipo::setCiudad);
        binder.forField(entrenadorField)
            .asRequired("El entrenador es requerido")
            .bind(Equipo::getEntrenador, Equipo::setEntrenador);

        // Cargar datos en el formulario si es edición
        if (esEdicion) {
            binder.readBean(equipo);
        }

        // Botones del diálogo
        Button guardarButton = new Button("Guardar");
        guardarButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        guardarButton.addClickListener(e -> {
            if (binder.writeBeanIfValid(equipo)) {
                equipoService.guardar(equipo);
                dialog.close();
                cargarEquipos();
                searchField.clear();
                String mensaje = esEdicion ? "Equipo actualizado correctamente" : "Equipo creado correctamente";
                Notification.show(mensaje, 3000, Notification.Position.BOTTOM_CENTER);
            } else {
                Notification.show("Por favor, completa todos los campos requeridos", 3000, Notification.Position.BOTTOM_CENTER);
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
