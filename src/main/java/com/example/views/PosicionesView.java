package com.example.views;

import com.example.logica.EstadisticaService;
import com.example.logica.TorneoService;
import com.example.logica.EquipoService;
import com.example.modelos.EstadisticaEquipo;
import com.example.modelos.Torneo;
import com.example.modelos.Equipo;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "posiciones", layout = MainLayout.class)
public class PosicionesView extends VerticalLayout {

	private final EstadisticaService estadisticaService;
	private final TorneoService torneoService;
	private final EquipoService equipoService;

	private ComboBox<Torneo> torneoBox;
	private Grid<EstadisticaEquipo> grid;

	@Autowired
	public PosicionesView(EstadisticaService estadisticaService, TorneoService torneoService, EquipoService equipoService) {
		this.estadisticaService = estadisticaService;
		this.torneoService = torneoService;
		this.equipoService = equipoService;

		setSizeFull();
		setPadding(true);
		setSpacing(true);

		add(crearTitulo());
		add(crearFiltro());
		add(crearGrid());

		// Seleccionar torneo activo por defecto y cargar datos
		List<Torneo> activos = torneoService.obtenerTodos().stream()
				.filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()))
				.toList();

		if (!activos.isEmpty()) {
			torneoBox.setItems(activos);
			torneoBox.setItemLabelGenerator(Torneo::getNombre);
			torneoBox.setValue(activos.get(0));
			actualizarTabla(activos.get(0).getId());
		} else {
			// Si no hay activos, cargar todos los torneos en el combo
			List<Torneo> todos = torneoService.obtenerTodos();
			torneoBox.setItems(todos);
			torneoBox.setItemLabelGenerator(Torneo::getNombre);
			if (!todos.isEmpty()) {
				torneoBox.setValue(todos.get(0));
				actualizarTabla(todos.get(0).getId());
			}
		}

		// Al cambiar el torneo, actualizar tabla
		torneoBox.addValueChangeListener(e -> {
			Torneo seleccionado = e.getValue();
			if (seleccionado != null) {
				actualizarTabla(seleccionado.getId());
			} else {
				grid.setItems(List.of());
			}
		});
	}

	private H2 crearTitulo() {
		H2 titulo = new H2("Tabla de Posiciones");
		titulo.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
		return titulo;
	}

	private ComboBox<Torneo> crearFiltro() {
		torneoBox = new ComboBox<>("Seleccionar Torneo");
		torneoBox.setWidthFull();
		torneoBox.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
		return torneoBox;
	}

	private Grid<EstadisticaEquipo> crearGrid() {
		grid = new Grid<>(EstadisticaEquipo.class, false);
		grid.addClassNames(LumoUtility.Border.ALL);
		grid.setSizeFull();

		// Columna: Equipo (nombre)
		grid.addColumn(est -> equipoService.obtenerPorId(est.getEquipoId()).map(Equipo::getNombre).orElse("-"))
			.setHeader("Equipo")
			.setAutoWidth(true);

		// PJ
		grid.addColumn(EstadisticaEquipo::getPartidosJugados).setHeader("PJ").setAutoWidth(true);
		// PG
		grid.addColumn(EstadisticaEquipo::getPartidosGanados).setHeader("PG").setAutoWidth(true);
		// PE
		grid.addColumn(EstadisticaEquipo::getPartidosEmpatados).setHeader("PE").setAutoWidth(true);
		// PP
		grid.addColumn(EstadisticaEquipo::getPartidosPerdidos).setHeader("PP").setAutoWidth(true);
		// GF
		grid.addColumn(EstadisticaEquipo::getGolesFavor).setHeader("GF").setAutoWidth(true);
		// GC
		grid.addColumn(EstadisticaEquipo::getGolesContra).setHeader("GC").setAutoWidth(true);
		// DG (diferencia)
		grid.addColumn(est -> est.getGolesFavor() - est.getGolesContra()).setHeader("DG").setAutoWidth(true);
		// PTS - mostrar en negrita
		grid.addColumn(new ComponentRenderer<>(est -> {
			Span s = new Span(String.valueOf(est.getPuntos()));
			s.getStyle().set("font-weight", "bold");
			return s;
		})).setHeader("PTS").setAutoWidth(true);

		return grid;
	}

	private void actualizarTabla(String torneoId) {
		try {
			grid.setItems(estadisticaService.obtenerTablaPosiciones(torneoId));
		} catch (Exception ex) {
			Notification.show("Error al cargar tabla de posiciones: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
			grid.setItems(List.of());
		}
	}
}
