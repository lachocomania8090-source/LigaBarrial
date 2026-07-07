package com.example.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout { // <-- Sin anotación @Theme aquí

    public MainLayout() {
        crearBarraSuperior();
        crearMenuLateral();

        // Inyección dinámica de colores al renderizar en el navegador
        addAttachListener(event -> {
            getElement().executeJs(
                    "const el = document.documentElement.style;" +
                            "el.setProperty('--lumo-primary-color', '#CCFF00');" +
                            "el.setProperty('--lumo-primary-text-color', '#CCFF00');" +
                            "el.setProperty('--lumo-primary-contrast-color', '#000000');" + // <-- SOLUCIÓN: Texto negro sobre botones verdes
                            "el.setProperty('--lumo-link-color', '#CCFF00');" +
                            "el.setProperty('--lumo-base-color', '#121212');" +
                            "el.setProperty('--lumo-background-color', '#1a1a1a');" +
                            "el.setProperty('--lumo-contrast-5pct', '#222222');" +
                            "el.setProperty('--lumo-contrast-10pct', '#2a2a2a');" +
                            "el.setProperty('--lumo-contrast-20pct', '#383838');" +
                            "el.setProperty('--lumo-border-color', '#2c2c2c');" +
                            "el.setProperty('--lumo-body-text-color', '#f5f5f5');" +
                            "el.setProperty('--lumo-secondary-text-color', '#b0b0b0');"
            );
        });
    }

    private void crearBarraSuperior() {
        H1 logo = new H1("Liga Barrial");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        Header header = new Header(new DrawerToggle(), logo);
        header.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Width.FULL);

        addToNavbar(header);
    }

    private void crearMenuLateral() {
        SideNav nav = new SideNav();
        nav.setWidthFull();

        // 1. Creamos cada ítem normalmente
        SideNavItem itemDashboard = new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.CHART_LINE.create());
        SideNavItem itemEquipos = new SideNavItem("Equipos", EquiposView.class, VaadinIcon.USERS.create());
        SideNavItem itemJugadores = new SideNavItem("Jugadores", JugadoresView.class, VaadinIcon.USER.create());
        SideNavItem itemPartidos = new SideNavItem("Partidos", PartidosView.class, VaadinIcon.PRESENTATION.create());
        SideNavItem itemPosiciones = new SideNavItem("Posiciones", PosicionesView.class, VaadinIcon.TROPHY.create());

        // 2. Agregamos los ítems al menú
        nav.addItem(itemDashboard, itemEquipos, itemJugadores, itemPartidos, itemPosiciones);

        // 3. Forzamos por JavaScript que cada botón interno se vuelva un bloque gigante, cuadrado y deportivo
        addAttachListener(event -> {
            getElement().executeJs(
                    "document.querySelectorAll('vaadin-side-nav-item').forEach(item => {" +
                            "   const link = item.shadowRoot ? item.shadowRoot.querySelector('a') : item.querySelector('a');" +
                            "   if (link) {" +
                            "       link.style.display = 'flex';" +
                            "       link.style.flexDirection = 'column';" + // Pone el ícono arriba y el texto abajo (estilo app premium)
                            "       link.style.alignItems = 'center';" +
                            "       link.style.justifyContent = 'center';" +
                            "       link.style.height = '100px';" +       // ¡AQUÍ CONTROLAS EL ALTO DEL CUADRADO!
                            "       link.style.margin = '10px';" +         // Separación física entre los cuadros
                            "       link.style.borderRadius = '12px';" +   // Bordes redondeados modernos
                            "       link.style.backgroundColor = '#222222';" + // Fondo gris oscuro para que parezca botón/tarjeta
                            "       link.style.transition = 'all 0.2s';" +
                            "   }" +
                            "});"
            );
        });

        Scroller scroller = new Scroller(nav);
        scroller.setHeight("100%");
        addToDrawer(scroller);
    }

    // Método auxiliar para evitar repetir código y solucionar el error de compilación
    private void applyMenuStyles(SideNavItem item) {
        item.addClassNames(
                LumoUtility.Padding.Vertical.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL,
                LumoUtility.FontSize.MEDIUM,
                LumoUtility.Width.FULL // Asegura que el área interactiva ocupe horizontalmente todo el ancho
        );
    }
}