package com.mycompany.interfaces;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import javax.servlet.annotation.WebServlet;

@Theme("mytheme")
@PreserveOnRefresh
public class PerfilUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        final WrappedSession session = getSession().getSession();
        final VerticalLayout rootLayout = new VerticalLayout();
        final Button btnLogout = new Button("Cerrar sesión");
        
        comprobarSesion(rootLayout, session);
        
        // invalida la sesion y redirecciona a login
        btnLogout.addClickListener(e -> {
            session.invalidate();
            Page.getCurrent().setLocation("/");
        });
        
        final Panel userPanel = cargarMenu();
        final Panel datosPanel = new Panel("Mis datos");
        
        final FormLayout formDatos = new FormLayout();
        final TextField nombre = new TextField("Nombre");
        final TextField apellidos = new TextField("Apellidos");
        final TextField dni = new TextField("Dni");
        final TextField telefono = new TextField("Telefono");
        final TextField username = new TextField("Usuario");
        final PasswordField password = new PasswordField("Contraseña");
        final PasswordField newPassword = new PasswordField("Nueva contraseña");
        final Button btnGuardar = new Button("Guardar datos");
        formDatos.addComponents(nombre, apellidos, dni, telefono, username, password, newPassword, btnGuardar);
        formDatos.setMargin(true);
        formDatos.setSpacing(true);
        datosPanel.setContent(formDatos);
        
        rootLayout.addComponents(btnLogout, userPanel, datosPanel);
        
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);
        
        setContent(rootLayout);
    }
    
    @WebServlet(urlPatterns = "/perfil/*", name = "PerfilUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = PerfilUI.class, productionMode = false)
    public static class PerfilUIServlet extends VaadinServlet {
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
