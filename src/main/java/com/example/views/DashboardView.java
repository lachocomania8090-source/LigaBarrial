package com.example.views;

import com.example.logica.EquipoService;
import com.example.logica.PartidoService;
import com.example.logica.TorneoService;
import com.example.modelos.Equipo;
import com.example.modelos.Partido;
import com.example.modelos.Torneo;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

@Route(value = "", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private final EquipoService equipoService;
    private final TorneoService torneoService;
    private final PartidoService partidoService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public DashboardView(EquipoService equipoService, TorneoService torneoService, PartidoService partidoService) {
        this.equipoService = equipoService;
        this.torneoService = torneoService;
        this.partidoService = partidoService;

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
        VerticalLayout cardEquipos = crearCard("Equipos Registrados", String.valueOf(equiposCount));

        // Card 2: Torneos Activos
        long torneosActivos = torneoService.obtenerTodos().stream()
                .filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()))
                .count();
        VerticalLayout cardTorneos = crearCard("Torneos Activos", String.valueOf(torneosActivos));

        // Card 3: Próximo Partido
        String proximoTexto = "No hay partidos programados";
        Partido proximo = partidoService.obtenerTodos().stream()
                .filter(p -> p.getFechaHora() != null && p.getFechaHora().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Partido::getFechaHora))
                .findFirst()
                .orElse(null);

        if (proximo != null) {
            String local = equipoService.obtenerPorId(proximo.getEquipoLocalId()).map(Equipo::getNombre).orElse("-");
            String visitante = equipoService.obtenerPorId(proximo.getEquipoVisitanteId()).map(Equipo::getNombre).orElse("-");
            proximoTexto = local + " vs " + visitante + " — " + (proximo.getFechaHora() != null ? proximo.getFechaHora().format(formatter) : "");
        }

        VerticalLayout cardProximo = crearCard("Próximo Partido", proximoTexto);

        layout.add(cardEquipos, cardTorneos, cardProximo);

        return layout;
    }

    private VerticalLayout crearCard(String titulo, String contenido) {
        // Título de la tarjeta
        H3 t = new H3(titulo);
        t.addClassNames(
            LumoUtility.FontSize.MEDIUM,
            LumoUtility.Margin.Bottom.SMALL,
            LumoUtility.TextColor.SECONDARY
        );

        // Dato principal (número/contenido)
        Paragraph p = new Paragraph(contenido);
        p.addClassNames(
            LumoUtility.FontSize.XXLARGE,
            LumoUtility.FontWeight.BOLD
        );

        // Crear tarjeta con estilos Lumo y CSS inline
        VerticalLayout card = new VerticalLayout(t, p);
        card.setWidth("280px");
        card.setHeight("180px");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);

        // Aplicar estilos CSS para sombra, borde redondeado y fondo
        card.getStyle().set("box-shadow", "0 2px 6px rgba(0, 0, 0, 0.1)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        card.getStyle().set("background", "var(--lumo-base-color)");
        card.getStyle().set("border", "1px solid var(--lumo-border-color)");

        return card;
    }
}