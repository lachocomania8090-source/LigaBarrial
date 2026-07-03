package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

// El layout define dentro de qué "cascarón" se va a mostrar esta vista
@Route(value = "", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        H2 titulo = new H2("Bienvenido al Sistema de Gestión");
        Paragraph descripcion = new Paragraph("Selecciona una opción en el menú lateral para comenzar.");

        // Centramos el contenido
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        add(titulo, descripcion);
    }
}