package com.mycompany.interfaces;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.mycompany.utils.BBDD;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")

public class SesionUI extends UI {

    @Override
    protected void init(VaadinRequest request) {

        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");

        final HorizontalLayout horarios = new HorizontalLayout();
        // comprueba si se ha iniciado sesión
        comprobarSesion(rootLayout, session);

        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });

        // panel de navegación
        final Navegacion navbar = new Navegacion();

        Label pelicula = new Label("Sesiones de " + session.getAttribute("nombrePeli").toString());
        rootLayout.addComponents(btnLogout, navbar, pelicula, horarios);
        horarios.setSpacing(true);
        mostrarSesiones(horarios, session);
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/sessionMovie/*", name = "SesionUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SesionUI.class, productionMode = false)
    public static class SesionUIServlet extends VaadinServlet {
    }

    /**
     * Método encargado de comprobar si la sesión existe o no Si no existe,
     * redirecciona al login
     */
    private static void comprobarSesion(final VerticalLayout rootLayout, final WrappedSession session) {
        if (session.getAttribute("usuario") == null) {
            Page.getCurrent().setLocation("/");
        } else {
            final Label bienvenido = new Label("Bienvenido, " + session.getAttribute("usuario"));
            rootLayout.addComponent(bienvenido);
        }
    }

    private static void mostrarSesiones(HorizontalLayout layout, WrappedSession session) {
        // obtengo la colección de los sesiones
        BBDD bbdd = null;
        try {
            bbdd = new BBDD("sesiones");
        } catch (UnknownHostException ex) {
            Logger.getLogger(SesionUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DBCollection sesiones = bbdd.getColeccion();

        // cursor para iterar la lista de sesiones
        String nomSesion = (String) session.getAttribute("nombrePeli");

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("pelicula", nomSesion);

        DBCursor cursor = sesiones.find(whereQuery);

        DBObject sesion;
        // recorre la lista y si lo encuentra, sale del bucle
        while (cursor.hasNext()) {
            sesion = cursor.next();

            String hora=sesion.get("hora").toString();
            String sala = sesion.get("sala").toString();
            Button b = new Button(hora);
            b.addClickListener(e -> {
                //Metemos la hora y la sala
                
                session.setAttribute("hora", hora);
                session.setAttribute("sala", sala);
                
                Page.getCurrent().setLocation("/sala");
            });
            layout.addComponent(b);
        }
    }

}
