package com.example.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.icon.VaadinIcon; // <-- Cambiamos a los íconos nativos de Vaadin

public class MainLayout extends AppLayout {

    public MainLayout() {
        crearBarraSuperior();
        crearMenuLateral();
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

        // Usamos VaadinIcon en lugar de LineAwesomeIcon
        nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.CHART_LINE.create()));

        nav.addItem(new SideNavItem("Equipos", EquiposView.class, VaadinIcon.USERS.create()));
        nav.addItem(new SideNavItem("Jugadores", JugadoresView.class, VaadinIcon.USER.create()));
        nav.addItem(new SideNavItem("Partidos", PartidosView.class, VaadinIcon.PRESENTATION.create()));
        // nav.addItem(new SideNavItem("Posiciones", PosicionesView.class, VaadinIcon.TROPHY.create()));

        Scroller scroller = new Scroller(nav);
        addToDrawer(scroller);
    }
}