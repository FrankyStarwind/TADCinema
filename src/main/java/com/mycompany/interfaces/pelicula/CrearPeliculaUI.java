package com.mycompany.interfaces.pelicula;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class CrearPeliculaUI extends UI {

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

        // panel de crear película
        final Panel panelCreate = new Panel("Crear nueva película");
        final VerticalLayout layoutCreate = new VerticalLayout();
        
        // formulario crear película
        final FormLayout formCreate = new FormLayout();
        final TextField titulo = new TextField("Título");
        titulo.setRequired(true);
        titulo.setInputPrompt("Título de la película");
        final ComboBox idioma = new ComboBox("Idioma", comboIdioma());
        idioma.setRequired(true);
        idioma.setInputPrompt("Elige idioma");
        final TextField director = new TextField("Director");
        director.setRequired(true);
        director.setInputPrompt("Director de la película");
        final TextField anyo = new TextField("Año");
        anyo.setRequired(true);
        anyo.setInputPrompt("Año de la película");
        final TextField duracion = new TextField("Duración (minutos)");
        duracion.setRequired(true);
        duracion.setInputPrompt("Duración de la película");
        
        // botón para crear película
        final Button btnCrear = new Button("Crear película");
        btnCrear.setStyleName("primary");
        
        formCreate.addComponents(titulo, idioma, director, anyo, duracion, btnCrear);
        formCreate.setMargin(true);
        
        layoutCreate.addComponents(new Label("Por favor, rellene todos los campos para añadir una nueva película."), formCreate);
        layoutCreate.setMargin(true);
        layoutCreate.setSpacing(true);
        
        panelCreate.setContent(layoutCreate);
        
        // crea una película nueva 
        btnCrear.addClickListener(e -> {
            if(validarCampos(titulo, idioma, director, anyo, duracion)) {
                try {
                    BBDD bbdd = new BBDD("movies");
                    
                    DBCollection movies = bbdd.getColeccion();
                    
                    BasicDBObject pelicula = new BasicDBObject();
                    pelicula.append("titulo", titulo.getValue());
                    pelicula.append("idioma", idioma.getValue());
                    pelicula.append("director", director.getValue());
                    pelicula.append("año", anyo.getValue());
                    pelicula.append("duracion", duracion.getValue());
                    
                    DBObject movie = movies.findOne(pelicula);
                    
                    if (movie == null) {
                        movies.insert(pelicula);
                        resetarCampos(titulo, idioma, director, anyo, duracion);
                        Notification.show("Se ha creado la película correctamente", Notification.Type.TRAY_NOTIFICATION);
                        Page.getCurrent().setLocation("/peliculas");
                    } else {
                        Notification.show("La película ya existe", Notification.Type.ERROR_MESSAGE);
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CrearPeliculaUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // ESTRUCTURA DE LA INTERFAZ
        rootLayout.addComponents(btnLogout, navbar, panelCreate);

        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/crear-pelicula/*", name = "CrearPeliculaUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CrearPeliculaUI.class, productionMode = false)
    public static class CrearPeliculaUIServlet extends VaadinServlet {
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
    
    /**
     * Método encargado de devolver la lista de salas del cine
     * @return listado salas cine
     */
    private static List<String> comboSala() {
        List<String> lista = new ArrayList<>();
        for(int i = 1; i <= 12; i++) {
            lista.add(String.valueOf(i));
        }
        return lista;
    }
    
    /**
     * Método encargado de devolver la lista de idiomas
     * @return listado idiomas película
     */
    private static List<String> comboIdioma() {
        List<String> lista = new ArrayList<>();
        lista.add("Castellano");
        lista.add("VOSE");
        lista.add("Español latino");
        return lista;
    }
    
    /**
     * Método encargado de validar los campos del formulario
     * @param titulo Título de la película
     * @param sala Sala del cine
     * @param idioma Idioma de la película
     * @param director Director de la película
     * @param año Año de estreno de la película
     * @param duracion Duración en minutos de la película
     * @return TRUE/FALSE
     */
    private static boolean validarCampos(TextField titulo, ComboBox idioma, TextField director, TextField anyo, TextField duracion) {
        boolean validos = true;
        String errores = "";
        
        if (titulo.getValue() == "") {
            errores += "El campo 'Título' es obligatorio\n";
            validos = false;
        }
        if (idioma.getValue() == null) {
            errores += "El campo 'Idioma' no puede estar vacío\n";
            validos = false;
        }
        if (director.getValue() == "") {
            errores += "El campo 'Director' es obligatorio\n";
            validos = false;
        }
        if (anyo.getValue() == "") {
            errores += "El campo 'Año' es obligatorio\n";
            validos = false;
        }
        if (duracion.getValue() == "") {
            errores += "El campo 'Duración' es obligatorio";
            validos = false;
        }
        
        if (!validos) {
            Notification.show("Error", errores, Notification.Type.ERROR_MESSAGE);
        }
        
        return validos;
    }
    
    /**
     * Método encargado de resetear los campos del formulario
     * @param titulo Título de la película
     * @param sala Sala del cine
     * @param idioma Idioma de la película
     * @param director Director de la película
     * @param año Año de estreno de la película
     * @param duracion Duración en minutos de la película
     */
    private static void resetarCampos(TextField titulo, ComboBox idioma, TextField director, TextField anyo, TextField duracion) {
        titulo.setValue("");
        idioma.setValue(null);
        director.setValue("");
        anyo.setValue("");
        duracion.setValue("");
    }

}
