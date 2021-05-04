package com.mycompany.interfaces;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mycompany.components.Navegacion;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")

public class SesionUI extends UI {

    public static WrappedSession session = null; //Definimos el elemento de sesión

    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");
        
        // comprueba si se ha iniciado sesión
        comprobarSesion(rootLayout, session);
        
        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });
        
        // panel de navegación
        final Navegacion navbar = new Navegacion();
        
        
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient("localhost", 27017);
        } catch (UnknownHostException ex) {
            Logger.getLogger(SesionUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        DB db = mongoClient.getDB("TADCinemaDB");
        

        
        
        Label pelicula = new Label("Sesiones de "+session.getAttribute("nombrePeli").toString());
        rootLayout.addComponents(btnLogout, navbar,pelicula);
        mostrarSesiones(rootLayout,db);
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/sessionMovie/*", name = "SesionUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SesionUI.class, productionMode = false)
    public static class CarteleraUIServlet extends VaadinServlet {
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


    public static void mostrarSesiones(VerticalLayout layout,DB db) {
        
        // obtengo la colección de los usuarios
        DBCollection sesiones = db.getCollection("sessions");

        // cursor para iterar la lista de usuarios
        final DBCursor cursor = sesiones.find();

        DBObject sesion;
        // recorre la lista y si lo encuentra, sale del bucle
        while (cursor.hasNext()) {
            sesion = cursor.next();
            
            Button b = new Button(sesion.get("hora").toString());
            b.addClickListener(e->{
                Notification.show(DESIGN_ATTR_PLAIN_TEXT, DESIGN_ATTR_PLAIN_TEXT, Notification.Type.ERROR_MESSAGE);
            });
            layout.addComponent(b);
        }

    }
    
}
