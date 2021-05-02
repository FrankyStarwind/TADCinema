package com.mycompany.interfaces;

import com.mycompany.components.Navegacion;
import com.vaadin.annotations.PreserveOnRefresh;
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
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
@PreserveOnRefresh
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
        final ComboBox sala = new ComboBox("Sala", comboSala());
        sala.setInputPrompt("Elige sala");
        sala.setRequired(true);
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
        
        formCreate.addComponents(titulo, sala, idioma, director, anyo, duracion, btnCrear);
        formCreate.setMargin(true);
        
        layoutCreate.addComponents(new Label("Por favor, rellene todos los campos para añadir una nueva película."), formCreate);
        layoutCreate.setMargin(true);
        layoutCreate.setSpacing(true);
        
        panelCreate.setContent(layoutCreate);
        
        // FALTA FUNCIÓN CREAR PELÍCULA

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

}
