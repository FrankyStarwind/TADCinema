package com.mycompany.components;

import com.mycompany.interfaces.HomeUI;
import com.vaadin.server.Page;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;

public class Navegacion extends CustomComponent {

    public Navegacion() {
        final Panel userPanel = cargarMenu();
        setCompositionRoot(userPanel);
    }
    
    /**
     * Método encargado de cargar el menú de navegación
     * @return Panel
     */
    private Panel cargarMenu() {
        final Panel userPanel = new Panel();
        final HorizontalLayout hLayout = new HorizontalLayout();
        final Button btnInicio = new Button("Inicio");
        final Button btnPerfil = new Button("Perfil");
        final WrappedSession session = HomeUI.getCurrent().getSession().getSession();
        btnInicio.addClickListener(e -> {
            Page.getCurrent().setLocation("/home");
        });
        
        
        
        btnPerfil.addClickListener(e -> {
            Page.getCurrent().setLocation("/perfil");
        });
        
        hLayout.addComponents(btnInicio, btnPerfil);
        String rolName=session.getAttribute("rol").toString();
        
        //Si es admin muestra botones con funcionalidades mejoradas
        if(rolName.equals("admin")){
            final Button bPelis = new Button("Peliculas");
            final Button bSesiones = new Button("Sesiones");
            final Button bSalas = new Button("Salas");
            final Button bCompras = new Button("Compras");
            hLayout.addComponents(bPelis, bSesiones, bSalas,bCompras);
        }
        
        hLayout.setMargin(true);
        hLayout.setSpacing(true);
        userPanel.setContent(hLayout);
        
        return userPanel;
    }
    
}