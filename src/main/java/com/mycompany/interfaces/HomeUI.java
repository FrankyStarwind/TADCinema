package com.mycompany.interfaces;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
@PreserveOnRefresh
public class HomeUI extends UI {

    public static List<Panel> lista;

    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");
        final HorizontalLayout layout = new HorizontalLayout();
        lista = new ArrayList<>();
        // comprueba si se ha iniciado sesión
        comprobarSesion(rootLayout, session);

        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });

        // panel de navegación
        final Navegacion navbar = new Navegacion();

        // prueba cartelera---------------------
        final Panel carteleraPanel = new Panel();

        BBDD bbdd = null;
        try {
            bbdd = new BBDD("movies");
        } catch (UnknownHostException ex) {
            Logger.getLogger(HomeUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        final DBCollection movies = bbdd.getColeccion();
        cargarPeliculas(movies, carteleraPanel,layout);
       
        for(int i=0; i<layout.getComponentCount();i++){
            Component c = layout.getComponent(i);
            if(c instanceof Panel){
                ((Panel) c).addClickListener(e->{
                    
                    Notification.show("Accediendo a la sesion",
                            c.getCaption(), Notification.Type.HUMANIZED_MESSAGE);
                    session.setAttribute("nombrePeli",c.getCaption());
                    Page.getCurrent().setLocation("/cartelera");
                }
                );
            }
        }
        
        //--------------------------------------

        // ESTRUCTURA DE LA INTERFAZ
        rootLayout.addComponents(btnLogout, navbar, carteleraPanel);

        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/home/*", name = "HomeUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = HomeUI.class, productionMode = false)
    public static class HomeUIServlet extends VaadinServlet {
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

    private static void cargarPeliculas(final DBCollection movies, final Panel panel, final HorizontalLayout layout) {
        final DBCursor cursor = movies.find();

        layout.setMargin(true);
        layout.setSpacing(true);

        DBObject movie = null;
        while (cursor.hasNext()) {
            movie = cursor.next();
            final Panel panelInner = new Panel(movie.get("titulo").toString());
            final VerticalLayout layoutInner = new VerticalLayout();
            final Label titulo = new Label("<strong>Título:</strong> " + movie.get("titulo"), ContentMode.HTML);
            final Label sala = new Label("<strong>Sala:</strong> " + movie.get("sala"), ContentMode.HTML);
            final Label idioma = new Label("<strong>Idioma:</strong> " + movie.get("idioma"), ContentMode.HTML);
            Double anyo = Double.parseDouble(movie.get("año").toString());
            final Label year = new Label("<strong>Año:</strong> " + anyo.intValue(), ContentMode.HTML);
            final Label director = new Label("<strong>Director:</strong> " + movie.get("director"), ContentMode.HTML);
            Double time = Double.parseDouble(movie.get("duracion").toString());
            final Label duracion = new Label("<strong>Duración:</strong> " + time.intValue() + " minutos", ContentMode.HTML);
//            final Button b = new Button("Acceder");
//            
//            b.addClickListener(e->{
//                Notification.show(DESIGN_ATTR_PLAIN_TEXT, DESIGN_ATTR_PLAIN_TEXT, Notification.Type.ERROR_MESSAGE);
//            });
            layoutInner.addComponents(titulo, sala, idioma, year, director, duracion
//                    ,
//                    b
            );
            
            layoutInner.setMargin(true);
            layoutInner.setSpacing(true);
            panelInner.setContent(layoutInner);

            layout.addComponent(panelInner);
            lista.add(panelInner);
        }

        panel.setContent(layout);
    }

    public void mostrarSesion(String nombrePeli) {
        WrappedSession s = getSession().getSession();
        s.setAttribute("nombrePelicula", nombrePeli);
        Page.getCurrent().setLocation("/cartelera");
    }

}
