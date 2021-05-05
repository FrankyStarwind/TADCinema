package com.mycompany.interfaces;

import com.mycompany.components.Navegacion;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
public class SalaUI extends UI {

    public String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

    public static List<Integer> filas = new ArrayList<Integer>();
    public static List<Integer> asientos= new ArrayList<Integer>();
    
    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");
        
        String nombreSesion =session.getAttribute("nombrePeli").toString();
        String sesHora =session.getAttribute("hora").toString();
        
        final Label nomPeli = new Label(nombreSesion+" "+sesHora);
        
        nomPeli.setCaption(nombreSesion);
        // comprueba si se ha iniciado sesión
        comprobarSesion(rootLayout, session);
        
        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });
        
        // panel de navegación
        final Navegacion navbar = new Navegacion();

        rellenarAsientos(9, 14);
        final ComboBox comboFilas = new ComboBox("Fila", filas);
        comboFilas.setRequired(true);
        comboFilas.setInputPrompt("Selecciona la fila");
        
        final ComboBox comboAsientos = new ComboBox("Tipo de asiento", asientos);
        comboAsientos.setRequired(true);
        comboAsientos.setInputPrompt("Selecciona el asiento");
  
        
        final Image image = new Image(null, new FileResource(new File(basepath + "/WEB-INF/images/sala.JPG")));
        
        rootLayout.addComponents(btnLogout, 
                navbar, nomPeli,image,comboFilas,comboAsientos);
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setContent(rootLayout);
    }

    @WebServlet(urlPatterns = "/sala/*", name = "SalaUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SalaUI.class, productionMode = false)
    public static class SalaUIServlet extends VaadinServlet {
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
    
    //Rellenado por filas y asientos
    public static void rellenarAsientos(int numFilas, int numAsientos){
        
        for (int i = 0; i < numAsientos; i++) {
            asientos.add(i+1);
        }
        for (int i = 0; i < numFilas; i++) {
            filas.add(i+1);
        }
        
    }
    
    
}
