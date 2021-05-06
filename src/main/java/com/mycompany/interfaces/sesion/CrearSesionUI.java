package com.mycompany.interfaces.sesion;

import com.mycompany.interfaces.sala.CrearSalaUI;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class CrearSesionUI extends UI {

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

        // panel de crear sesión
        final Panel panelCreate = new Panel("Crear nueva sesión");
        final VerticalLayout layoutCreate = new VerticalLayout();
        
        // formulario crear sesión
        final FormLayout formCreate = new FormLayout();
        final ComboBox pelicula = new ComboBox("Película", comboPeliculas());
        pelicula.setRequired(true);
        pelicula.setInputPrompt("Selecciona película");
        final ComboBox sala = new ComboBox("Nº sala", comboSalas());
        sala.setRequired(true);
        sala.setInputPrompt("Selecciona la sala");
        final ComboBox hora = new ComboBox("Horario", comboHoras());
        hora.setRequired(true);
        hora.setInputPrompt("Selecciona una hora");
        
        // botón para crear sesión
        final Button btnCrear = new Button("Crear sesión");
        btnCrear.setStyleName("primary");
        
        formCreate.addComponents(pelicula, sala, hora, btnCrear);
        formCreate.setMargin(true);
        
        layoutCreate.addComponents(new Label("Por favor, rellene todos los campos para añadir una nueva sesión."), formCreate);
        layoutCreate.setMargin(true);
        layoutCreate.setSpacing(true);
        
        panelCreate.setContent(layoutCreate);
        
        // crea una sesión nueva 
        btnCrear.addClickListener(e -> {
            if(validarCampos(pelicula, sala, hora)) {
                try {
                    BBDD bbdd = new BBDD("sesiones");
                    
                    DBCollection sesiones = bbdd.getColeccion();
                    
                    BasicDBObject sesion = new BasicDBObject();
                    sesion.append("pelicula", pelicula.getValue());
                    sesion.append("sala", sala.getValue());
                    sesion.append("hora", hora.getValue());
                    
                    DBObject data = sesiones.findOne(sesion);
                    
                    if (data == null) {
                        sesiones.insert(sesion);
                        resetarCampos(pelicula, sala, hora);
                        Notification.show("Se ha creado la sesión correctamente", Notification.Type.TRAY_NOTIFICATION);
                    } else {
                        Notification.show("La sesión ya existe", Notification.Type.ERROR_MESSAGE);
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CrearSalaUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // ESTRUCTURA DE LA INTERFAZ
        rootLayout.addComponents(btnLogout, navbar, panelCreate);

        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }
    
    @WebServlet(urlPatterns = "/crear-sesion/*", name = "CrearSesionUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CrearSesionUI.class, productionMode = false)
    public static class CrearSesionUIServlet extends VaadinServlet {
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
     * Método encargado de devolver la lista de películas existentes
     * @return Listado de películas en bbdd
     */
    private static List<String> comboPeliculas() {
        final List<String> peliculas = new ArrayList<>();
        BBDD bbdd = null;
        try {
            bbdd = new BBDD("movies");
        } catch (UnknownHostException ex) {
            Logger.getLogger(CrearSesionUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        final DBCollection movies = bbdd.getColeccion();
        final DBCursor cursor = movies.find();
        
        DBObject pelicula = null;
        while(cursor.hasNext()) {
            pelicula = cursor.next();
            peliculas.add(pelicula.get("titulo").toString());
        }
        
        return peliculas;
    }
    
    /**
     * Método encargado de devolver la lista de salas existentes
     * @return Listado de salas en bbdd
     */
    private static List<Integer> comboSalas() {
        final List<Integer> salas = new ArrayList<>();
        BBDD bbdd = null;
        try {
            bbdd = new BBDD("salas");
        } catch (UnknownHostException ex) {
            Logger.getLogger(CrearSesionUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        final DBCollection data = bbdd.getColeccion();
        final DBCursor cursor = data.find();
        
        DBObject sala = null;
        while(cursor.hasNext()) {
            sala = cursor.next();
            salas.add(Integer.valueOf(sala.get("_id").toString()));
        }
        
        salas.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        
        return salas;
    }
    
    /**
     * Método encargado de devolver una lista de horarios disponibles
     * @return Listado horarios
     */
    private static List<String> comboHoras() {
        List<String> horas = new ArrayList<>();
        horas.add("12:00");
        horas.add("12:30");
        horas.add("13:15");
        horas.add("14:10");
        horas.add("16:00");
        horas.add("16:50");
        horas.add("17:30");
        horas.add("18:10");
        horas.add("19:00");
        horas.add("19:45");
        horas.add("20:30");
        horas.add("21:00");
        horas.add("21:35");
        horas.add("22:15");
        horas.add("22:45");
        horas.add("23:10");
        horas.add("23:50");
        horas.add("00:35");
        horas.add("01:15");
        return horas;
    }
    
    /**
     * Método encargado de validar los campos del formulario
     * @param pelicula Película de la sesión
     * @param sala Sala de la sesión
     * @param horario Horario de la sesión
     * @return 
     */
    private static boolean validarCampos(ComboBox pelicula, ComboBox sala, ComboBox horario) {
        boolean validos = true;
        String errores = "";
        
        if (pelicula.getValue() == null) {
            errores += "El campo 'Película' no puede estar vacío\n";
            validos = false;
        }
        if (sala.getValue() == null) {
            errores += "El campo 'Nº sala' no puede estar vacío\n";
            validos = false;
        }
        if (horario.getValue() == null) {
            errores += "El campo 'Horario' no puede estar vacío";
            validos = false;
        }
        
        if (!validos) {
            Notification.show("Error", errores, Notification.Type.ERROR_MESSAGE);
        }
        
        return validos;
    }
    
    /**
     * Método encargado de resetear los campos del formulario
     * @param pelicula Película
     * @param sala Sala
     * @param hora Hora
     */
    private static void resetarCampos(ComboBox pelicula, ComboBox sala, ComboBox hora) {
        pelicula.setValue(null);
        sala.setValue(null);
        hora.setValue(null);
    }
    
}
