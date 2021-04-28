package com.mycompany.interfaces;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
@PreserveOnRefresh
public class HomeUI extends UI {
    
    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");
        
        comprobarSesion(rootLayout, session);
        
        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });
        
        final Panel userPanel = cargarMenu();
        
        rootLayout.addComponents(btnLogout, userPanel);
        
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);
        
        setContent(rootLayout);
    }
 
    @WebServlet(urlPatterns = "/home/*", name = "HomeUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = HomeUI.class, productionMode = false)
    public static class HomeUIServlet extends VaadinServlet {
    }
    
    /**
     * Método encargado de comprobar si la sesión existe o no
     * Si no existe, redirecciona al login
     */
    private static void comprobarSesion(final VerticalLayout rootLayout, final WrappedSession session) {
        if(session.getAttribute("usuario") == null){
            Page.getCurrent().setLocation("/");
        } else {
            final Label bienvenido = new Label("Bienvenido, " + session.getAttribute("usuario"));
            rootLayout.addComponent(bienvenido);
        }
    }
    
    private static Panel cargarMenu() {
        final Panel userPanel = new Panel();
        final HorizontalLayout hLayout = new HorizontalLayout();
        final Button btnInicio = new Button("Inicio");
        final Button btnCartelera = new Button("Cartelera");
        
        btnInicio.addClickListener(e -> {
            Page.getCurrent().setLocation("/home");
        });
        
        btnCartelera.addClickListener(e -> {
            Page.getCurrent().setLocation("/cartelera");
        });
        
        hLayout.addComponents(btnInicio, btnCartelera);
        hLayout.setMargin(true);
        hLayout.setSpacing(true);
        userPanel.setContent(hLayout);
        
        return userPanel;
    }
    
}