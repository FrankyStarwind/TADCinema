package com.mycompany.interfaces;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mycompany.components.Navegacion;
import com.mycompany.model.Compra;
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
public class CompraUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");

        //final HorizontalLayout horarios = new HorizontalLayout();
        // comprueba si se ha iniciado sesión
        comprobarSesion(rootLayout, session);

        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });

        // panel de navegación
        final Navegacion navbar = new Navegacion();

        Compra c = (Compra) session.getAttribute("compra");

        Label nombreU = new Label("Gracias por la compra " + c.getUsuario());
        Label pelicula = new Label("Ha comprado la sesion de la película "
                + c.getNomPelicula() + " a las " + c.getHoraSesion() + " en la fila "
                + c.getFila() + " y asiento " + c.getAsiento() + " a " + c.getPrecio() + "");
        try {
            registrarCompra(c);
        } catch (UnknownHostException ex) {
            Logger.getLogger(CompraUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        rootLayout.addComponents(btnLogout, navbar, nombreU, pelicula);
        //horarios.setSpacing(true);
        //mostrarSesiones(horarios, db, session);
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/compraEntrada/*", name = "CompraUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CompraUI.class, productionMode = false)
    public static class CompraUIServlet extends VaadinServlet {
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

    private static void registrarCompra(Compra c) throws UnknownHostException {
        BBDD bbdd = new BBDD("compras");
        DBCollection compras = bbdd.getColeccion();

        BasicDBObject compra = new BasicDBObject();
        compra.append("usuario", c.getUsuario());
        compra.append("nombrePelicula", c.getNomPelicula());
        compra.append("fila", c.getFila());
        compra.append("asiento", c.getAsiento());
        compra.append("horaSesion", c.getHoraSesion());
        compra.append("sala", c.getSala());
        compra.append("precio", c.getPrecio());

        compras.insert(compra);
    }

    private static void mostrarSesiones(HorizontalLayout layout, DB db, WrappedSession session) {
        // obtengo la colección de los usuarios
        DBCollection sesiones = db.getCollection("sessions");

        // cursor para iterar la lista de usuarios
        String nomSesion = (String) session.getAttribute("nombrePeli");

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("movie", nomSesion);

        DBCursor cursor = sesiones.find(whereQuery);

        DBObject sesion;
        // recorre la lista y si lo encuentra, sale del bucle
        while (cursor.hasNext()) {
            sesion = cursor.next();

            String hora = sesion.get("hora").toString();
            Button b = new Button(hora);
            b.addClickListener(e -> {
                //Metemos la hora
                session.setAttribute("hora", hora);
                Page.getCurrent().setLocation("/sala");
            });
            layout.addComponent(b);
        }
    }

}
