package com.mycompany.components;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import java.net.UnknownHostException;

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
        
        hLayout.addComponents(btnInicio, 
                //btnCartelera,
                
                btnPerfil);
        hLayout.setMargin(true);
        hLayout.setSpacing(true);
        userPanel.setContent(hLayout);
        
        return userPanel;
    }
    private static boolean comprobarAdmin(String username, String role) throws UnknownHostException {
        boolean res = false;
        MongoClient mongoClient;

        mongoClient = new MongoClient("localhost", 27017);
        DB db = mongoClient.getDB("TADCinemaDB");
        DBCollection usuarios = db.getCollection("usuarios");

        //Creamos el filtro de query
        DBObject query = new BasicDBObject("username", username)
                .append("role", role);
        DBObject d1 = usuarios.findOne(query);
        if (d1 != null) {
            res = true;
        }
        return res;
    }
}