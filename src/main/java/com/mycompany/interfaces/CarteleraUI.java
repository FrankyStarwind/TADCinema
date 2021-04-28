package com.mycompany.interfaces;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
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
@PreserveOnRefresh
public class CarteleraUI extends UI {

    public static WrappedSession session = null; //Definimos el elemento de sesión

    @Override
    protected void init(VaadinRequest request) {
        // layout vertical y label
        final VerticalLayout verticalLayout = new VerticalLayout();
        final Label nombreUsuario = new Label();

        // se obtiene la sesión
        session = getSession().getSession();

        if (session.getAttribute("usuario") == null) {
            session.invalidate();
            Page.getCurrent().setLocation("/login");
        } else {
            nombreUsuario.setValue("Bienvenido, " + session.getAttribute("usuario"));
        }
        
        // botón para cerrar sesión
        final Button btnLogout = new Button("Cerrar sesión");
        
        // al pulsar el botón de cerrar sesión, sale de la sesión
        // y redirecciona al login
        btnLogout.addClickListener(e -> {
            Page.getCurrent().setLocation("/");
        });
        
        final Panel userPanel = cargarMenu();
        
        // tabla con el registro de películas
        final Table tablePeliculas = new Table();
        definirCabeceraTabla(tablePeliculas);
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient("localhost", 27017);
        } catch (UnknownHostException ex) {
            Logger.getLogger(CarteleraUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        DB db = mongoClient.getDB("TADCinemaDB");
        cargarPeliculas(db, tablePeliculas);

        tablePeliculas.addItemClickListener(
                new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                session = getSession().getSession();
                String nomPeli = event.getItem().getItemProperty("Película").getValue().toString();
                session.setAttribute("sessionNombrePelicula", nomPeli);
                Notification.show("Entrando en las sesiones de " + nomPeli, "Entrando, espere por favor",
                        Notification.Type.HUMANIZED_MESSAGE);
                //Page.getCurrent().setLocation("/"+"session");
            }
        });
        
        verticalLayout.addComponents(nombreUsuario, btnLogout, userPanel, tablePeliculas);
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);

        setContent(verticalLayout);
    }

    @WebServlet(urlPatterns = "/cartelera/*", name = "CarteleraUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CarteleraUI.class, productionMode = false)
    public static class CarteleraUIServlet extends VaadinServlet {
    }

    public void definirCabeceraTabla(Table table) {
        table.addContainerProperty("Película", String.class, null);
        table.addContainerProperty("Sala", String.class, null);
        table.addContainerProperty("Sesión 1", String.class, null);
        table.addContainerProperty("Sesión 2", String.class, null);
        table.addContainerProperty("Sesión 3", String.class, null);

        table.setSelectable(true); //Para poder seleccionar los registros
        table.setSizeFull();
    }

    public static void cargarPeliculas(DB db, Table tableP) {
        // obtengo la colección de los usuarios
        DBCollection peliculas = db.getCollection("movies");

        // cursor para iterar la lista de usuarios
        final DBCursor cursor = peliculas.find();

        DBObject pelicula;
        // recorre la lista y si lo encuentra, sale del bucle
        while (cursor.hasNext()) {
            pelicula = cursor.next();
            tableP.addItem(new Object[]{pelicula.get("name"), pelicula.get("numSala"), "16:00", " 18:00", "20:00"}, tableP.getItemIds().size() + 1);

        }

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
