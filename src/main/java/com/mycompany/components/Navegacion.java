package com.mycompany.components;

import com.vaadin.server.Page;
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
    private static Panel cargarMenu() {
        final Panel userPanel = new Panel();
        final HorizontalLayout hLayout = new HorizontalLayout();
        final Button btnInicio = new Button("Inicio");
        final Button btnCartelera = new Button("Cartelera");
        final Button btnPerfil = new Button("Perfil");
        
        btnInicio.addClickListener(e -> {
            Page.getCurrent().setLocation("/home");
        });
        
        btnCartelera.addClickListener(e -> {
            Page.getCurrent().setLocation("/cartelera");
        });
        
        btnPerfil.addClickListener(e -> {
            Page.getCurrent().setLocation("/perfil");
        });
        
        hLayout.addComponents(btnInicio, btnCartelera, btnPerfil);
        hLayout.setMargin(true);
        hLayout.setSpacing(true);
        userPanel.setContent(hLayout);
        
        return userPanel;
    }

}